package com.warfactory.ultimateweight.compat;

import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatPlugin;
import com.warfactory.ultimateweight.logging.WeightLoggers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Discovers, gates, and activates compat plugins. This is the single entry point each loader calls
 * at startup, replacing the old hand-maintained {@code CompatibilityNestedWeightProviderXXXX} lists.
 *
 * <p>Gating reads the plugin's required-mod metadata straight from the build-time index, so a plugin
 * whose mod is absent is never even class-loaded - preserving the laziness of the previous reflective
 * loader. Each plugin is activated inside its own {@code try/catch} so one failure cannot abort the
 * rest.
 */
public final class WeightCompatBootstrap {
    private static final WeightLoggers.WeightLogger LOGGER = WeightLoggers.component("compat");

    private static final Comparator<DiscoveredPlugin> PRIORITY_ORDER = new Comparator<DiscoveredPlugin>() {
        @Override
        public int compare(DiscoveredPlugin left, DiscoveredPlugin right) {
            int byPriority = Integer.compare(right.priority(), left.priority());
            if (byPriority != 0) {
                return byPriority;
            }
            return left.className().compareTo(right.className());
        }
    };

    private WeightCompatBootstrap() {
    }

    /**
     * Reads the index visible to {@code classLoader} and activates every plugin whose mod gating is
     * satisfied.
     */
    public static void run(ClassLoader classLoader, ModPresenceChecker modPresence, WeightCompatContext context) {
        run(CompatPluginIndex.read(classLoader), modPresence, context, classLoader);
    }

    /**
     * Core activation over an explicit plugin list. Exposed for tests so plugins can be injected
     * without an on-disk index.
     */
    public static void run(
        List<DiscoveredPlugin> discovered,
        ModPresenceChecker modPresence,
        WeightCompatContext context,
        ClassLoader classLoader
    ) {
        if (discovered == null || discovered.isEmpty() || modPresence == null || context == null) {
            return;
        }
        ClassLoader loader = classLoader != null ? classLoader : WeightCompatBootstrap.class.getClassLoader();

        // Dedupe by class name: the same plugin can appear in more than one index on the classpath
        // (e.g. a dev run with both the common and platform output dirs present), and registering a
        // weight provider twice would double-count its weight.
        Set<String> seen = new HashSet<String>();
        ArrayList<DiscoveredPlugin> active = new ArrayList<DiscoveredPlugin>();
        for (DiscoveredPlugin plugin : discovered) {
            if (seen.add(plugin.className()) && isGateSatisfied(plugin, modPresence)) {
                active.add(plugin);
            }
        }
        Collections.sort(active, PRIORITY_ORDER);

        int activated = 0;
        for (DiscoveredPlugin plugin : active) {
            if (activate(plugin, context, loader)) {
                activated++;
            }
        }
        LOGGER.debug("Activated {} compat plugin(s) of {} discovered.",
            Integer.valueOf(activated), Integer.valueOf(discovered.size()));
    }

    private static boolean isGateSatisfied(DiscoveredPlugin plugin, ModPresenceChecker modPresence) {
        for (String required : plugin.requiredMods()) {
            if (!modPresence.isModLoaded(required)) {
                return false;
            }
        }
        String[] anyOf = plugin.anyOf();
        if (anyOf.length == 0) {
            return true;
        }
        for (String candidate : anyOf) {
            if (modPresence.isModLoaded(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static boolean activate(DiscoveredPlugin plugin, WeightCompatContext context, ClassLoader loader) {
        try {
            Class<?> pluginClass = Class.forName(plugin.className(), false, loader);
            Object instance = pluginClass.getDeclaredConstructor().newInstance();
            if (instance instanceof WeightCompatPlugin) {
                ((WeightCompatPlugin) instance).register(context);
            } else if (instance instanceof IWeightCompatProvider) {
                context.registerWeightProvider((IWeightCompatProvider) instance);
            } else {
                LOGGER.warn("Compat plugin {} implements neither WeightCompatPlugin nor IWeightCompatProvider; skipped.",
                    plugin.className());
                return false;
            }
            LOGGER.debug("Compat plugin active: {}", plugin.id());
            return true;
        } catch (Throwable error) {
            LOGGER.warn("Failed to activate compat plugin {}: {}", plugin.className(), String.valueOf(error));
            return false;
        }
    }
}

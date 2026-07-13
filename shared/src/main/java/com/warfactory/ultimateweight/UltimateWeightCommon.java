package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.api.WeightCompatRegistry;
import com.warfactory.ultimateweight.config.IConfigLoader;
import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.runtime.UltimateWeightServices;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

public final class UltimateWeightCommon {
    public static final String MOD_ID = "wfweight";
    public static final String MOD_NAME = "WarFactory Ultimate Weight";
    public static final String DEBUG_PROPERTY = MOD_ID + ".debug";

    private static final AtomicReference<UltimateWeightServices> SERVICES =
        new AtomicReference<UltimateWeightServices>();
    private static final AtomicReference<IConfigLoader> CONFIG_LOADER =
        new AtomicReference<IConfigLoader>();
    // The config exactly as loaded from disk / bundled defaults, before any scripting overlay.
    // Kept separate so a script reload (or a disk reload) can re-derive the active config without
    // permanently baking previous script edits in.
    private static final AtomicReference<WeightConfig> BASE_CONFIG =
        new AtomicReference<WeightConfig>();
    // Scripting overlay (GroovyScript on 1.12.2, KubeJS on 1.20.1). Applied on top of BASE_CONFIG to
    // produce the active config. Null means no scripts have overridden anything.
    private static final AtomicReference<UnaryOperator<WeightConfig>> SCRIPT_CONFIGURATOR =
        new AtomicReference<UnaryOperator<WeightConfig>>();
    private static final AtomicBoolean DEBUG_ENABLED = new AtomicBoolean(readDebugProperty());

    private UltimateWeightCommon() {
    }

    public static void installConfigLoader(IConfigLoader loader) {
        CONFIG_LOADER.set(loader);
    }

    public static UltimateWeightServices bootstrap() {
        UltimateWeightServices services = SERVICES.get();
        if (services != null) {
            return services;
        }

        WeightConfig base = loadBundledConfig();
        BASE_CONFIG.compareAndSet(null, base);
        UltimateWeightServices created = new UltimateWeightServices(overlayScripts(BASE_CONFIG.get()));
        if (SERVICES.compareAndSet(null, created)) {
            return created;
        }
        return SERVICES.get();
    }

    /**
     * Installs a config read from the local disk file (or bundled defaults) as the new base, then
     * re-applies any active scripting overlay on top of it. Use this for the authoritative
     * server/single-player config; clients receiving a server sync should use
     * {@link #applySyncedConfig(String)} instead so they do not re-run their own scripts on top of
     * the already-finalized config.
     */
    public static synchronized UltimateWeightServices applyLocalConfig(String yamlText) {
        BASE_CONFIG.set(requireConfigLoader().load(yamlText));
        return rebuildActiveServices();
    }

    /**
     * Installs a config received from the server as the active config verbatim. The server has
     * already applied its own scripting overlay, so the client must not overlay again.
     */
    public static UltimateWeightServices applySyncedConfig(String yamlText) {
        return replaceServices(requireConfigLoader().load(yamlText));
    }

    /**
     * Sets (or replaces) the scripting overlay and recomputes the active config from the current
     * base. Pass {@code null} to remove the overlay entirely.
     */
    public static synchronized UltimateWeightServices setScriptConfigurator(UnaryOperator<WeightConfig> configurator) {
        SCRIPT_CONFIGURATOR.set(configurator);
        return rebuildActiveServices();
    }

    public static UltimateWeightServices clearScriptConfigurator() {
        return setScriptConfigurator(null);
    }

    public static String serializeActiveConfig() {
        return requireConfigLoader().write(bootstrap().config());
    }

    public static boolean isDebugEnabled() {
        return DEBUG_ENABLED.get();
    }

    public static void setDebugEnabled(boolean enabled) {
        DEBUG_ENABLED.set(enabled);
    }

    public static void refreshDebugMode() {
        DEBUG_ENABLED.set(readDebugProperty());
    }

    public static void resetForTests() {
        SERVICES.set(null);
        BASE_CONFIG.set(null);
        SCRIPT_CONFIGURATOR.set(null);
        WeightCompatRegistry.clear();
        refreshDebugMode();
    }

    public static synchronized UltimateWeightServices resetToBundledConfig() {
        BASE_CONFIG.set(loadBundledConfig());
        return rebuildActiveServices();
    }

    private static UltimateWeightServices rebuildActiveServices() {
        WeightConfig base = BASE_CONFIG.get();
        if (base == null) {
            if (CONFIG_LOADER.get() == null) {
                // The config system isn't ready yet - e.g. a scripting load event (GroovyScript's
                // PRE_INIT pass) fired before this mod installed its config loader. The configurator
                // is already stored, so just leave it: bootstrap()/applyLocalConfig() will apply it
                // once the loader is installed and the base config is loaded.
                return SERVICES.get();
            }
            base = loadBundledConfig();
            BASE_CONFIG.set(base);
        }
        return replaceServices(overlayScripts(base));
    }

    private static WeightConfig overlayScripts(WeightConfig base) {
        UnaryOperator<WeightConfig> configurator = SCRIPT_CONFIGURATOR.get();
        if (configurator == null) {
            return base;
        }
        WeightConfig overridden = configurator.apply(base);
        return overridden == null ? base : overridden;
    }

    private static UltimateWeightServices replaceServices(WeightConfig config) {
        UltimateWeightServices services = new UltimateWeightServices(config);
        SERVICES.set(services);
        return services;
    }

    private static WeightConfig loadBundledConfig() {
        return requireConfigLoader().loadBundled();
    }

    private static IConfigLoader requireConfigLoader() {
        IConfigLoader loader = CONFIG_LOADER.get();
        if (loader == null) {
            throw new IllegalStateException("No config loader installed");
        }
        return loader;
    }

    private static boolean readDebugProperty() {
        return Boolean.parseBoolean(System.getProperty(DEBUG_PROPERTY, "false"));
    }
}

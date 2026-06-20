package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatPlugin;
import com.warfactory.ultimateweight.compat.DiscoveredPlugin;
import com.warfactory.ultimateweight.compat.ModPresenceChecker;
import com.warfactory.ultimateweight.compat.WeightCompatBootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;

class WeightCompatBootstrapTest {
    // Shared sinks the nested test plugins write to, so the test can observe activation.
    static final List<String> ACTIVATION_ORDER = new ArrayList<String>();
    static final List<IWeightCompatProvider> REGISTERED_PROVIDERS = new ArrayList<IWeightCompatProvider>();

    @BeforeEach
    void reset() {
        ACTIVATION_ORDER.clear();
        REGISTERED_PROVIDERS.clear();
    }

    @Test
    void requiredModsGateAllMustBePresent() {
        run(present("a"), // only "a" loaded
            plugin(NamedProvider.class, mods("a"), none(), 0),
            plugin(NamedProvider2.class, mods("a", "b"), none(), 0)); // needs b too -> skipped

        Assertions.assertEquals(Collections.singletonList("NamedProvider"), ACTIVATION_ORDER);
    }

    @Test
    void anyOfGateNeedsAtLeastOne() {
        run(present("b"),
            plugin(NamedProvider.class, none(), mods("a", "b"), 0), // b present -> active
            plugin(NamedProvider2.class, none(), mods("x", "y"), 0)); // neither -> skipped

        Assertions.assertEquals(Collections.singletonList("NamedProvider"), ACTIVATION_ORDER);
    }

    @Test
    void higherPriorityActivatesFirst() {
        run(present(),
            plugin(LowPriorityPlugin.class, none(), none(), 1),
            plugin(HighPriorityPlugin.class, none(), none(), 100));

        Assertions.assertEquals(Arrays.asList("HighPriorityPlugin", "LowPriorityPlugin"), ACTIVATION_ORDER);
    }

    @Test
    void providerIsAutoRegistered() {
        run(present(), plugin(NamedProvider.class, none(), none(), 0));

        Assertions.assertEquals(1, REGISTERED_PROVIDERS.size());
        Assertions.assertTrue(REGISTERED_PROVIDERS.get(0) instanceof NamedProvider);
    }

    @Test
    void richPluginRegisterIsInvoked() {
        run(present(), plugin(HighPriorityPlugin.class, none(), none(), 0));

        Assertions.assertEquals(Collections.singletonList("HighPriorityPlugin"), ACTIVATION_ORDER);
    }

    @Test
    void throwingPluginDoesNotAbortOthers() {
        run(present(),
            plugin(ThrowingPlugin.class, none(), none(), 100), // runs first, throws
            plugin(NamedProvider.class, none(), none(), 0));

        // The throwing plugin is skipped; the provider still registers.
        Assertions.assertEquals(1, REGISTERED_PROVIDERS.size());
        Assertions.assertTrue(REGISTERED_PROVIDERS.get(0) instanceof NamedProvider);
    }

    @Test
    void duplicateEntriesActivateOnce() {
        run(present(),
            plugin(NamedProvider.class, none(), none(), 0),
            plugin(NamedProvider.class, none(), none(), 0)); // same class twice (two indexes)

        Assertions.assertEquals(1, REGISTERED_PROVIDERS.size());
    }

    // --- helpers ---------------------------------------------------------

    private static void run(Set<String> loadedMods, DiscoveredPlugin... plugins) {
        ModPresenceChecker mods = loadedMods::contains;
        WeightCompatBootstrap.run(
            Arrays.asList(plugins), mods, new CapturingContext(), WeightCompatBootstrapTest.class.getClassLoader());
    }

    private static DiscoveredPlugin plugin(Class<?> type, String[] requiredMods, String[] anyOf, int priority) {
        return new DiscoveredPlugin(type.getName(), requiredMods, anyOf, priority, type.getSimpleName());
    }

    private static String[] mods(String... ids) {
        return ids;
    }

    private static String[] none() {
        return new String[0];
    }

    private static Set<String> present(String... ids) {
        return new HashSet<String>(Arrays.asList(ids));
    }

    static final class CapturingContext implements WeightCompatContext {
        @Override
        public void registerWeightProvider(IWeightCompatProvider provider) {
            REGISTERED_PROVIDERS.add(provider);
            ACTIVATION_ORDER.add(provider.getClass().getSimpleName());
        }

        @Override
        public boolean isModLoaded(String modId) {
            return false;
        }
    }

    // Nested plugins must be public static with a public no-arg constructor so the bootstrap can
    // Class.forName + instantiate them by name.
    public static class NamedProvider implements IWeightCompatProvider {
        @Override
        public OptionalDouble getUnitWeight(Object stack) {
            return OptionalDouble.empty();
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    public static final class NamedProvider2 extends NamedProvider {
    }

    public static final class HighPriorityPlugin implements WeightCompatPlugin {
        @Override
        public void register(WeightCompatContext context) {
            ACTIVATION_ORDER.add("HighPriorityPlugin");
        }
    }

    public static final class LowPriorityPlugin implements WeightCompatPlugin {
        @Override
        public void register(WeightCompatContext context) {
            ACTIVATION_ORDER.add("LowPriorityPlugin");
        }
    }

    public static final class ThrowingPlugin implements WeightCompatPlugin {
        @Override
        public void register(WeightCompatContext context) {
            throw new IllegalStateException("boom");
        }
    }
}

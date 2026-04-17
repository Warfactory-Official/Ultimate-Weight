package com.warfactory.ultimateweight;

import com.hbm.weight.api.WeightCompatRegistry;
import com.warfactory.ultimateweight.config.IConfigLoader;
import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.runtime.UltimateWeightServices;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class UltimateWeightCommon {
    public static final String MOD_ID = "wfweight";
    public static final String MOD_NAME = "WarFactory Ultimate Weight";
    public static final String DEBUG_PROPERTY = MOD_ID + ".debug";

    private static final AtomicReference<UltimateWeightServices> SERVICES =
        new AtomicReference<UltimateWeightServices>();
    private static final AtomicReference<IConfigLoader> CONFIG_LOADER =
        new AtomicReference<IConfigLoader>();
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

        UltimateWeightServices created = new UltimateWeightServices(loadBundledConfig());
        if (SERVICES.compareAndSet(null, created)) {
            return created;
        }
        return SERVICES.get();
    }

    public static UltimateWeightServices applySyncedConfig(String yamlText) {
        return replaceServices(requireConfigLoader().load(yamlText));
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
        WeightCompatRegistry.clear();
        refreshDebugMode();
    }

    public static UltimateWeightServices resetToBundledConfig() {
        return replaceServices(loadBundledConfig());
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
        return Boolean.parseBoolean(System.getProperty(DEBUG_PROPERTY, "true"));
    }
}

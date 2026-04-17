package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.config.WeightConfigCodec;
import com.warfactory.ultimateweight.runtime.UltimateWeightServices;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class UltimateWeightCommon {
    public static final String MOD_ID = "wfweight";
    public static final String MOD_NAME = "WarFactory Ultimate Weight";
    public static final String DEFAULT_CONFIG_RESOURCE = "weight_config.yml";
    public static final String DEBUG_PROPERTY = MOD_ID + ".debug";

    private static final AtomicReference<UltimateWeightServices> SERVICES =
        new AtomicReference<UltimateWeightServices>();
    private static final AtomicBoolean DEBUG_ENABLED = new AtomicBoolean(readDebugProperty());
    private static final WeightConfigCodec CONFIG_CODEC = new WeightConfigCodec();

    private UltimateWeightCommon() {
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
        return replaceServices(CONFIG_CODEC.read(yamlText));
    }

    public static String serializeActiveConfig() {
        return CONFIG_CODEC.write(bootstrap().config());
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
        InputStream stream = UltimateWeightCommon.class.getClassLoader().getResourceAsStream(
            DEFAULT_CONFIG_RESOURCE
        );
        if (stream == null) {
            return WeightConfig.defaults();
        }

        try {
            return CONFIG_CODEC.read(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load bundled weight config", exception);
        }
    }

    private static boolean readDebugProperty() {
        return Boolean.parseBoolean(System.getProperty(DEBUG_PROPERTY, "true"));
    }
}

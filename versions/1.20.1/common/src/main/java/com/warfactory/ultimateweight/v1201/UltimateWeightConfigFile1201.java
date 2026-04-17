package com.warfactory.ultimateweight.v1201;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UltimateWeightConfigFile1201 {
    private static Path configFile;

    private UltimateWeightConfigFile1201() {
    }

    public static synchronized void configure(Path configRoot) {
        configFile = configRoot.resolve(UltimateWeightCommon.MOD_ID).resolve(UltimateWeightCommon.DEFAULT_CONFIG_RESOURCE);
        ensureExists();
        reloadFromDisk();
    }

    public static synchronized void reloadFromDisk() {
        if (configFile == null) {
            UltimateWeightCommon.resetToBundledConfig();
            return;
        }

        ensureExists();
        try {
            UltimateWeightCommon.applySyncedConfig(Files.readString(configFile, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load config file " + configFile, exception);
        }
    }

    private static void ensureExists() {
        if (configFile == null) {
            return;
        }

        try {
            Files.createDirectories(configFile.getParent());
            if (!Files.exists(configFile)) {
                UltimateWeightCommon.resetToBundledConfig();
                Files.writeString(configFile, UltimateWeightCommon.serializeActiveConfig(), StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to prepare config file " + configFile, exception);
        }
    }
}

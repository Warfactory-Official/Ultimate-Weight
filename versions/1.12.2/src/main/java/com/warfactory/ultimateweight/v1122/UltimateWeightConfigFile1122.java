package com.warfactory.ultimateweight.v1122;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UltimateWeightConfigFile1122 {
    private static Path configFile;
    private static final WeightConfigLoader1122 CONFIG_LOADER = new WeightConfigLoader1122();

    private UltimateWeightConfigFile1122() {
    }

    public static synchronized void configure(Path configRoot) {
        UltimateWeightCommon.installConfigLoader(CONFIG_LOADER);
        configFile = configRoot.resolve(UltimateWeightCommon.MOD_ID).resolve(WeightConfigLoader1122.FILE_NAME);
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
            UltimateWeightCommon.applySyncedConfig(
                new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8)
            );
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
                Files.write(configFile, UltimateWeightCommon.serializeActiveConfig().getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to prepare config file " + configFile, exception);
        }
    }
}

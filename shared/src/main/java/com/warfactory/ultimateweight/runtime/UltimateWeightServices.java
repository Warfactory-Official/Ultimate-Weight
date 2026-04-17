package com.warfactory.ultimateweight.runtime;

import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.core.PlayerWeightTracker;
import com.warfactory.ultimateweight.core.WeightFormatter;
import com.warfactory.ultimateweight.core.WeightInventoryCalculator;
import com.warfactory.ultimateweight.core.WeightResolver;
import com.warfactory.ultimateweight.network.ConfigFragmenter;
import com.warfactory.ultimateweight.network.ConfigReassembler;

public final class UltimateWeightServices {
    private final WeightConfig config;
    private final WeightResolver resolver;
    private final WeightInventoryCalculator inventoryCalculator;
    private final PlayerWeightTracker playerWeightTracker;
    private final ConfigFragmenter configFragmenter;
    private final ConfigReassembler configReassembler;
    private final WeightFormatter formatter;

    public UltimateWeightServices(WeightConfig config) {
        this.config = config;
        this.resolver = new WeightResolver(config);
        this.inventoryCalculator = new WeightInventoryCalculator(resolver);
        this.playerWeightTracker = new PlayerWeightTracker(config, inventoryCalculator);
        this.configFragmenter = new ConfigFragmenter();
        this.configReassembler = new ConfigReassembler();
        this.formatter = new WeightFormatter(config.precision());
    }

    public WeightConfig config() {
        return config;
    }

    public WeightResolver resolver() {
        return resolver;
    }

    public WeightInventoryCalculator inventoryCalculator() {
        return inventoryCalculator;
    }

    public PlayerWeightTracker playerWeightTracker() {
        return playerWeightTracker;
    }

    public ConfigFragmenter configFragmenter() {
        return configFragmenter;
    }

    public ConfigReassembler configReassembler() {
        return configReassembler;
    }

    public WeightFormatter formatter() {
        return formatter;
    }
}

package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.runtime.UltimateWeightServices;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class UltimateWeightFabric261 implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    @Override
    public void onInitialize() {
        UltimateWeightServices services = UltimateWeightCommon.bootstrap();
        LOGGER.info(
            "{} initialized with {} exact rules, {} group rules, {} prefix rules.",
            UltimateWeightCommon.MOD_NAME,
            Integer.valueOf(services.config().exactWeightsKg().size()),
            Integer.valueOf(services.config().groupWeightsKg().size()),
            Integer.valueOf(services.config().prefixWeightsKg().size())
        );
    }
}

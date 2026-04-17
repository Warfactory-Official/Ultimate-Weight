package com.warfactory.ultimateweight.lexforge;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.runtime.UltimateWeightServices;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(UltimateWeightCommon.MOD_ID)
public final class UltimateWeightLexForge {
    private static final Logger LOGGER = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    public UltimateWeightLexForge() {
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

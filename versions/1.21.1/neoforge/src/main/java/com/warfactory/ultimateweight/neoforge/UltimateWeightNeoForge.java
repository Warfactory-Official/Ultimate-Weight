package com.warfactory.ultimateweight.neoforge;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.runtime.UltimateWeightServices;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(UltimateWeightCommon.MOD_ID)
public final class UltimateWeightNeoForge {
    private static final Logger LOGGER = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    public UltimateWeightNeoForge() {
        UltimateWeightServices services = UltimateWeightCommon.bootstrap();
        LOGGER.info(
            "{} initialized with {} exact rules, {} wildcard rules, {} dictionary rules.",
            UltimateWeightCommon.MOD_NAME,
            Integer.valueOf(services.config().resolverRules().exactCount()),
            Integer.valueOf(services.config().resolverRules().wildcardCount()),
            Integer.valueOf(services.config().resolverRules().matchCount())
        );
    }
}

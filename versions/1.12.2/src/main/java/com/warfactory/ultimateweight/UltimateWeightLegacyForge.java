package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.runtime.UltimateWeightServices;
import com.warfactory.ultimateweight.v1122.UltimateWeightCommonProxy1122;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = UltimateWeightCommon.MOD_ID, name = UltimateWeightCommon.MOD_NAME, version = "0.1.0")
public class UltimateWeightLegacyForge {
    private static final Logger LOGGER = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    @SidedProxy(
        clientSide = "com.warfactory.ultimateweight.v1122.client.UltimateWeightClientProxy1122",
        serverSide = "com.warfactory.ultimateweight.v1122.UltimateWeightCommonProxy1122"
    )
    public static UltimateWeightCommonProxy1122 PROXY;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PROXY.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PROXY.init(event);
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

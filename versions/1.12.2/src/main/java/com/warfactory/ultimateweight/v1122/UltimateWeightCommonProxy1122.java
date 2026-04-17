package com.warfactory.ultimateweight.v1122;

import com.hbm.weight.api.WeightCompatRegistry;
import com.warfactory.ultimateweight.v1122.capability.UltimateWeightCapabilities1122;
import com.warfactory.ultimateweight.v1122.compat.CompatibilityNestedWeightProvider1122;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class UltimateWeightCommonProxy1122 {
    public void preInit(FMLPreInitializationEvent event) {
        UltimateWeightConfigFile1122.configure(event.getModConfigurationDirectory().toPath());
        WeightCompatRegistry.registerAll(CompatibilityNestedWeightProvider1122.create());
        UltimateWeightCapabilities1122.register();
        UltimateWeightNetwork1122.register();
        UltimateWeightCommonEvents1122 events = new UltimateWeightCommonEvents1122();
        MinecraftForge.EVENT_BUS.register(events);
        FMLCommonHandler.instance().bus().register(events);
    }

    public void init(FMLInitializationEvent event) {
    }
}

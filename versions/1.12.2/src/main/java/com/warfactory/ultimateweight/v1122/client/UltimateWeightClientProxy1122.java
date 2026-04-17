package com.warfactory.ultimateweight.v1122.client;

import com.warfactory.ultimateweight.v1122.UltimateWeightCommonProxy1122;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public final class UltimateWeightClientProxy1122 extends UltimateWeightCommonProxy1122 {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        UltimateWeightClientEvents1122 clientEvents = new UltimateWeightClientEvents1122();
        MinecraftForge.EVENT_BUS.register(clientEvents);
        FMLCommonHandler.instance().bus().register(clientEvents);
    }
}

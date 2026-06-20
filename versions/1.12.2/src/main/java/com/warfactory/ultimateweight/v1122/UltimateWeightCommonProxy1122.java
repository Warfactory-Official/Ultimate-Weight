package com.warfactory.ultimateweight.v1122;

import com.warfactory.ultimateweight.compat.ModPresenceChecker;
import com.warfactory.ultimateweight.compat.WeightCompatBootstrap;
import com.warfactory.ultimateweight.v1122.capability.UltimateWeightCapabilities1122;
import com.warfactory.ultimateweight.v1122.compat.CompatContext1122;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class UltimateWeightCommonProxy1122 {
    public void preInit(FMLPreInitializationEvent event) {
        UltimateWeightConfigFile1122.configure(event.getModConfigurationDirectory().toPath());
        // Auto-discover every @CompatPlugin on the classpath, gated by mod presence. Replaces the old
        // hand-maintained provider list and wires the Baubles/Traveler's/Retro worn-item plugins.
        ModPresenceChecker mods = Loader::isModLoaded;
        WeightCompatBootstrap.run(getClass().getClassLoader(), mods, new CompatContext1122(mods));
        UltimateWeightCapabilities1122.register();
        UltimateWeightNetwork1122.register();
        UltimateWeightCommonEvents1122 events = new UltimateWeightCommonEvents1122();
        MinecraftForge.EVENT_BUS.register(events);
        FMLCommonHandler.instance().bus().register(events);
    }

    public void init(FMLInitializationEvent event) {
    }
}

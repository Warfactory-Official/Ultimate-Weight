package com.warfactory.ultimateweight.v1122.groovy;

import com.cleanroommc.groovyscript.api.GroovyPlugin;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;
import com.cleanroommc.groovyscript.event.GroovyReloadEvent;
import com.cleanroommc.groovyscript.event.ScriptRunEvent;
import com.cleanroommc.groovyscript.sandbox.LoadStage;
import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.UltimateWeightLegacyForge;
import com.warfactory.ultimateweight.config.ScriptConfigBridge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collection;

/**
 * GroovyScript compatibility plugin. GroovyScript discovers any class implementing
 * {@link GroovyPlugin} automatically (no registration needed), so simply shipping this class wires up
 * the {@code mods.wfweight} container. Config edits made by scripts override the on-disk YAML config.
 *
 * <p>GroovyScript runs scripts in three stages (preInit, init, postInit) on first load and re-runs
 * only the reloadable postInit stage on {@code /reload}. We reset accumulated edits at the very start
 * of a load - the first-load {@code PRE_INIT} pass and the reload event - then let edits accumulate
 * across stages and apply eagerly.</p>
 */

@Optional.Interface(modid = "groovyscript",
        iface = "com.cleanroommc.groovyscript.api.GroovyPlugin",
        striprefs = true)
public final class UltimateWeightGroovyPlugin implements GroovyPlugin {

    private final UltimateWeightGroovyContainer container = new UltimateWeightGroovyContainer();

    @Override
    public String getModId() {
        return UltimateWeightCommon.MOD_ID;
    }

    @Override
    public String getContainerName() {
        return UltimateWeightCommon.MOD_NAME;
    }

    @Override
    public GroovyPropertyContainer createGroovyPropertyContainer() {
        return container;
    }

    @Override
    public void onCompatLoaded(GroovyContainer<?> owner) {
        UltimateWeightLegacyForge.LOGGER.info("Groovy compat loaded!");
    }

    @Override
    public Collection<String> getAliases() {
        Collection<String> info = new ArrayList<>();
        info.add(UltimateWeightCommon.MOD_NAME);
        info.add("wfw");
        info.add("wfweight");
        info.add("UltimateWeight");
        return info;
    }

    @SubscribeEvent
    @Optional.Method(modid = "groovyscript")
    public static void onScriptRunPre(ScriptRunEvent.Pre event) {
        // First load begins with the PRE_INIT stage; reset there so postInit edits accumulate on top.
        if (event.getLoadStage() == LoadStage.PRE_INIT) {
            ScriptConfigBridge.begin();
        }
    }

    @SubscribeEvent
    @Optional.Method(modid = "groovyscript")
    public static void onGroovyReload(GroovyReloadEvent event) {
        // Fired once per /reload, right before the reloadable scripts run again.
        ScriptConfigBridge.begin();
    }
}

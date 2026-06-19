package com.warfactory.ultimateweight.forge.kubejs;

import com.warfactory.ultimateweight.config.ScriptConfigBridge;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;

/**
 * KubeJS integration for Ultimate Weight. Registered via {@code kubejs.plugins.txt}; KubeJS only
 * loads it when KubeJS itself is present, so the dependency stays soft.
 *
 * <p>It binds a global {@code WeightConfig} object into startup scripts. Every method on it overrides
 * the matching value from the on-disk YAML config, and anything left untouched keeps its config
 * value, so scripts can do everything the config does and take precedence over it. Bindings are
 * registered before the startup scripts evaluate, so we reset accumulated edits there and let each
 * call re-apply the overlay eagerly.</p>
 *
 * <pre>{@code
 * // startup_scripts/weight.js
 * WeightConfig.setItemWeight('minecraft:stone', 2.0)
 * WeightConfig.setTagWeight('minecraft:planks', 0.6)
 * WeightConfig.setHardLockWeight(180.0)
 * WeightConfig.disableStamina()
 * }</pre>
 */
public class UltimateWeightKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        if (event.getType() == ScriptType.STARTUP) {
            // Bindings are registered before startup scripts run, so reset here: the scripts are the
            // source of truth for whatever config they set on each (re)load.
            ScriptConfigBridge.begin();
            event.add("WeightConfig", ScriptConfigBridge.overrides());
        }
    }
}

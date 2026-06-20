package com.warfactory.ultimateweight.neoforge.kubejs;

import com.warfactory.ultimateweight.config.ScriptConfigBridge;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

/**
 * KubeJS integration for Ultimate Weight on 1.21.1. Declared via {@code kubejs.plugins.txt}; KubeJS
 * only loads it when KubeJS itself is present, so the dependency stays soft.
 *
 * <p>KubeJS 7.x (1.21) replaced the {@code KubeJSPlugin} abstract class + {@code BindingsEvent} with
 * a {@code KubeJSPlugin} interface whose {@code registerBindings} takes a {@link BindingRegistry}.
 * The behaviour is unchanged from the 1.20.1 plugin: it binds a global {@code WeightConfig} object
 * into startup scripts, where every method overrides the matching on-disk YAML value (and anything
 * left untouched keeps its config value). Bindings are registered before the startup scripts run, so
 * accumulated edits are reset here and each call re-applies the overlay eagerly.
 *
 * <pre>{@code
 * // startup_scripts/weight.js
 * WeightConfig.setItemWeight('minecraft:stone', 2.0)
 * WeightConfig.setTagWeight('minecraft:planks', 0.6)
 * WeightConfig.setHardLockWeight(180.0)
 * WeightConfig.disableStamina()
 * }</pre>
 */
public class UltimateWeightKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerBindings(BindingRegistry bindings) {
        if (bindings.type().isStartup()) {
            // Bindings are registered before startup scripts run, so reset here: the scripts are the
            // source of truth for whatever config they set on each (re)load.
            ScriptConfigBridge.begin();
            bindings.add("WeightConfig", ScriptConfigBridge.overrides());
        }
    }
}

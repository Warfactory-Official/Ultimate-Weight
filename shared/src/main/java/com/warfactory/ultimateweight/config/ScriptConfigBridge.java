package com.warfactory.ultimateweight.config;

import com.warfactory.ultimateweight.UltimateWeightCommon;

/**
 * Loader-agnostic entry point that the scripting integrations drive. GroovyScript (1.12.2) and
 * KubeJS (1.20.1) both funnel their edits into the single shared {@link ScriptConfigOverrides}
 * instance exposed here; this class wires those edits into {@link UltimateWeightCommon} so the
 * active config is rebuilt as an overlay on top of the on-disk config.
 *
 * <p>Lifecycle: a script layer calls {@link #begin()} once before (re)running its scripts to clear
 * any prior edits, the scripts mutate {@link #overrides()}, and each mutation eagerly re-applies the
 * overlay via the change listener so a partial set of edits is always reflected. {@link #end()} is a
 * no-op convenience hook for layers that prefer an explicit commit point.</p>
 */
public final class ScriptConfigBridge {
    private static final ScriptConfigOverrides OVERRIDES = new ScriptConfigOverrides();

    static {
        OVERRIDES.setChangeListener(ScriptConfigBridge::apply);
    }

    private ScriptConfigBridge() {
    }

    public static ScriptConfigOverrides overrides() {
        return OVERRIDES;
    }

    /** Clears all accumulated script edits and resets the active config back to the on-disk base. */
    public static synchronized void begin() {
        OVERRIDES.clear();
        apply();
    }

    /** Explicit commit hook. Edits already apply eagerly, so this only re-asserts the overlay. */
    public static synchronized void end() {
        apply();
    }

    private static synchronized void apply() {
        if (OVERRIDES.isEmpty()) {
            UltimateWeightCommon.clearScriptConfigurator();
        } else {
            UltimateWeightCommon.setScriptConfigurator(OVERRIDES::applyTo);
        }
    }
}

package com.warfactory.ultimateweight.v1122.groovy;

import com.cleanroommc.groovyscript.registry.NamedRegistry;
import com.warfactory.ultimateweight.config.ScriptConfigBridge;
import com.warfactory.ultimateweight.config.ScriptConfigOverrides;

import java.util.Arrays;

/**
 * GroovyScript-facing handle for Ultimate Weight's config, reachable in scripts as
 * {@code mods.wfweight.config} (alias {@code mods.wfweight.weight}). Every method overrides the
 * matching value from the on-disk YAML config; anything left untouched keeps its config value. Edits
 * apply immediately and are reset at the start of each script (re)load, so the scripts are the source
 * of truth for whatever they choose to set.
 *
 * <pre>{@code
 * // postInit_scripts (or any .groovy file)
 * mods.wfweight.config.setItemWeight('minecraft:stone', 2.0)
 * mods.wfweight.config.setTagWeight('ingotIron', 1.5)
 * mods.wfweight.config.setHardLockWeight(180.0)
 * mods.wfweight.config.disableStamina()
 * }</pre>
 */
public class WeightGroovyConfig extends NamedRegistry {

    public WeightGroovyConfig() {
        super(Arrays.asList("config", "weight"));
    }

    private static ScriptConfigOverrides overrides() {
        return ScriptConfigBridge.overrides();
    }

    // item weights
    public ScriptConfigOverrides setItemWeight(String itemId, double weightKg) {
        return overrides().setItemWeight(itemId, weightKg);
    }

    public ScriptConfigOverrides setItemWeight(String itemId, int metadata, double weightKg) {
        return overrides().setItemWeight(itemId, metadata, weightKg);
    }

    public ScriptConfigOverrides setWildcardWeight(String itemId, double weightKg) {
        return overrides().setWildcardWeight(itemId, weightKg);
    }

    public ScriptConfigOverrides setTagWeight(String oreDictName, double weightKg) {
        return overrides().setTagWeight(oreDictName, weightKg);
    }

    // limits & precision
    public ScriptConfigOverrides setDefaultCarryCapacity(double kilograms) {
        return overrides().setDefaultCarryCapacity(kilograms);
    }

    public ScriptConfigOverrides setHardLockWeight(double kilograms) {
        return overrides().setHardLockWeight(kilograms);
    }

    public ScriptConfigOverrides setFailsafeFullScan(boolean enabled) {
        return overrides().setFailsafeFullScan(enabled);
    }

    public ScriptConfigOverrides setFullScanInterval(long ticks) {
        return overrides().setFullScanInterval(ticks);
    }

    public ScriptConfigOverrides setHudDecimals(int decimals) {
        return overrides().setHudDecimals(decimals);
    }

    public ScriptConfigOverrides setTooltipDecimals(int decimals) {
        return overrides().setTooltipDecimals(decimals);
    }

    public ScriptConfigOverrides setStackDecimals(int decimals) {
        return overrides().setStackDecimals(decimals);
    }

    // fall damage
    public ScriptConfigOverrides setFallDamageEnabled(boolean enabled) {
        return overrides().setFallDamageEnabled(enabled);
    }

    public ScriptConfigOverrides setFallDamageStartLoadPercent(double percent) {
        return overrides().setFallDamageStartLoadPercent(percent);
    }

    public ScriptConfigOverrides setFallDamageExtraPerLoadPercent(double multiplier) {
        return overrides().setFallDamageExtraPerLoadPercent(multiplier);
    }

    public ScriptConfigOverrides setFallDamageHardLockBonus(double bonus) {
        return overrides().setFallDamageHardLockBonus(bonus);
    }

    public ScriptConfigOverrides setFallDamageMaxMultiplier(double multiplier) {
        return overrides().setFallDamageMaxMultiplier(multiplier);
    }

    // stamina
    public ScriptConfigOverrides setStaminaEnabled(boolean enabled) {
        return overrides().setStaminaEnabled(enabled);
    }

    public ScriptConfigOverrides disableStamina() {
        return overrides().disableStamina();
    }

    public ScriptConfigOverrides enableStamina() {
        return overrides().enableStamina();
    }

    public ScriptConfigOverrides setStaminaTotal(double total) {
        return overrides().setStaminaTotal(total);
    }

    public ScriptConfigOverrides setSprintStaminaLossRate(double rate) {
        return overrides().setSprintStaminaLossRate(rate);
    }

    public ScriptConfigOverrides setJumpStaminaLoss(double loss) {
        return overrides().setJumpStaminaLoss(loss);
    }

    public ScriptConfigOverrides setStaminaGainRate(double rate) {
        return overrides().setStaminaGainRate(rate);
    }

    public ScriptConfigOverrides setStaminaExhaustionThreshold(double threshold) {
        return overrides().setStaminaExhaustionThreshold(threshold);
    }

    public ScriptConfigOverrides setStaminaRecoveryPercent(double percent) {
        return overrides().setStaminaRecoveryPercent(percent);
    }

    public ScriptConfigOverrides setStaminaDrainWhileRunning(boolean drain) {
        return overrides().setStaminaDrainWhileRunning(drain);
    }

    public ScriptConfigOverrides setStaminaDrainOnJump(boolean drain) {
        return overrides().setStaminaDrainOnJump(drain);
    }

    public ScriptConfigOverrides clearStaminaPenalties() {
        return overrides().clearStaminaPenalties();
    }

    public ScriptConfigOverrides addStaminaPenalty(double loadPercent, double useMultiplier) {
        return overrides().addStaminaPenalty(loadPercent, useMultiplier);
    }

    // movement thresholds
    public ScriptConfigOverrides clearMovementThresholds() {
        return overrides().clearMovementThresholds();
    }

    public ScriptConfigOverrides addMovementThreshold(double loadPercent, double speedMultiplier, double jumpMultiplier) {
        return overrides().addMovementThreshold(loadPercent, speedMultiplier, jumpMultiplier);
    }

    // group limits
    public ScriptConfigOverrides defineGroup(String groupId, String label, int limit) {
        return overrides().defineGroup(groupId, label, limit);
    }

    public ScriptConfigOverrides addGroupItem(String groupId, String itemId) {
        return overrides().addGroupItem(groupId, itemId);
    }

    public ScriptConfigOverrides addGroupItem(String groupId, String itemId, int metadata) {
        return overrides().addGroupItem(groupId, itemId, metadata);
    }

    public ScriptConfigOverrides addGroupWildcard(String groupId, String itemId) {
        return overrides().addGroupWildcard(groupId, itemId);
    }

    public ScriptConfigOverrides addGroupTag(String groupId, String oreDictName) {
        return overrides().addGroupTag(groupId, oreDictName);
    }

    // equipment bonuses
    public ScriptConfigOverrides setEquipmentBonus(String itemId, double carryCapacityKg, double staminaBonus) {
        return overrides().setEquipmentBonus(itemId, carryCapacityKg, staminaBonus);
    }

    public ScriptConfigOverrides setEquipmentCarryBonus(String itemId, double carryCapacityKg) {
        return overrides().setEquipmentCarryBonus(itemId, carryCapacityKg);
    }

    public ScriptConfigOverrides setEquipmentStaminaBonus(String itemId, double staminaBonus) {
        return overrides().setEquipmentStaminaBonus(itemId, staminaBonus);
    }

    public ScriptConfigOverrides addEquipmentGroupLimitBonus(String itemId, String groupId, int amount) {
        return overrides().addEquipmentGroupLimitBonus(itemId, groupId, amount);
    }
}

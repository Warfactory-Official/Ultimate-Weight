package com.warfactory.ultimateweight.config;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A mutable accumulator of config edits gathered from scripting (GroovyScript on 1.12.2, KubeJS on
 * 1.20.1). It mirrors everything the YAML config can express and is applied as an overlay on top of
 * the on-disk config via {@link #applyTo(WeightConfig)}, so scripts can do anything the config does
 * <em>and</em> take precedence over it.
 *
 * <p>Every mutator returns {@code this} for fluent/Groovy-friendly chaining and notifies an optional
 * change listener so the active config can be rebuilt eagerly. Fields left untouched fall through to
 * the base config's value, so a script only needs to set what it wants to change.</p>
 */
public final class ScriptConfigOverrides {
    private Runnable changeListener;

    private Integer hudDecimals;
    private Integer tooltipDecimals;
    private Integer stackDecimals;

    private Boolean enableFailsafeFullScan;
    private Long fullScanIntervalTicks;
    private Double defaultCarryCapacityKg;
    private Double hardLockWeightKg;

    private Boolean fallDamageEnabled;
    private Double fallDamageStartLoadPercent;
    private Double fallDamageExtraPerLoadPercent;
    private Double fallDamageHardLockBonus;
    private Double fallDamageMaxMultiplier;

    private Boolean staminaEnabled;
    private Double staminaTotal;
    private Double staminaSprintLossRate;
    private Double staminaJumpLoss;
    private Double staminaGainRate;
    private Double staminaExhaustionThreshold;
    private Double staminaRecoveryPercent;
    private Boolean staminaDrainWhileRunning;
    private Boolean staminaDrainOnJump;
    private List<WeightConfig.UsagePenaltyRule> staminaPenalties;

    private List<WeightConfig.ThresholdRule> movementThresholds;

    private final Map<String, Double> exactWeights = new LinkedHashMap<String, Double>();
    private final Map<String, Double> wildcardWeights = new LinkedHashMap<String, Double>();
    private final Map<String, Double> tagWeights = new LinkedHashMap<String, Double>();

    private final Map<String, GroupOverride> groups = new LinkedHashMap<String, GroupOverride>();
    private final Map<String, EquipmentBonusOverride> equipmentExact =
        new LinkedHashMap<String, EquipmentBonusOverride>();
    private final Map<String, EquipmentBonusOverride> equipmentWildcard =
        new LinkedHashMap<String, EquipmentBonusOverride>();

    public ScriptConfigOverrides() {
    }

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    private ScriptConfigOverrides changed() {
        if (changeListener != null) {
            changeListener.run();
        }
        return this;
    }

    public boolean isEmpty() {
        return hudDecimals == null && tooltipDecimals == null && stackDecimals == null
            && enableFailsafeFullScan == null && fullScanIntervalTicks == null
            && defaultCarryCapacityKg == null && hardLockWeightKg == null
            && fallDamageEnabled == null && fallDamageStartLoadPercent == null
            && fallDamageExtraPerLoadPercent == null && fallDamageHardLockBonus == null
            && fallDamageMaxMultiplier == null
            && staminaEnabled == null && staminaTotal == null && staminaSprintLossRate == null
            && staminaJumpLoss == null && staminaGainRate == null && staminaExhaustionThreshold == null
            && staminaRecoveryPercent == null && staminaDrainWhileRunning == null
            && staminaDrainOnJump == null && staminaPenalties == null
            && movementThresholds == null
            && exactWeights.isEmpty() && wildcardWeights.isEmpty() && tagWeights.isEmpty()
            && groups.isEmpty() && equipmentExact.isEmpty() && equipmentWildcard.isEmpty();
    }

    public void clear() {
        hudDecimals = null;
        tooltipDecimals = null;
        stackDecimals = null;
        enableFailsafeFullScan = null;
        fullScanIntervalTicks = null;
        defaultCarryCapacityKg = null;
        hardLockWeightKg = null;
        fallDamageEnabled = null;
        fallDamageStartLoadPercent = null;
        fallDamageExtraPerLoadPercent = null;
        fallDamageHardLockBonus = null;
        fallDamageMaxMultiplier = null;
        staminaEnabled = null;
        staminaTotal = null;
        staminaSprintLossRate = null;
        staminaJumpLoss = null;
        staminaGainRate = null;
        staminaExhaustionThreshold = null;
        staminaRecoveryPercent = null;
        staminaDrainWhileRunning = null;
        staminaDrainOnJump = null;
        staminaPenalties = null;
        movementThresholds = null;
        exactWeights.clear();
        wildcardWeights.clear();
        tagWeights.clear();
        groups.clear();
        equipmentExact.clear();
        equipmentWildcard.clear();
    }

    // --- item weights -------------------------------------------------------------------------

    public ScriptConfigOverrides setItemWeight(String itemId, double weightKg) {
        return setItemWeight(itemId, 0, weightKg);
    }

    public ScriptConfigOverrides setItemWeight(String itemId, int metadata, double weightKg) {
        if (notBlank(itemId)) {
            exactWeights.put(WeightResolverRules.exactKey(itemId, metadata), Double.valueOf(weightKg));
        }
        return changed();
    }

    public ScriptConfigOverrides setWildcardWeight(String itemId, double weightKg) {
        if (notBlank(itemId)) {
            wildcardWeights.put(WeightResolverRules.wildcardKey(itemId), Double.valueOf(weightKg));
        }
        return changed();
    }

    /** Tag (1.20.1) / ore dictionary (1.12.2) weight. */
    public ScriptConfigOverrides setTagWeight(String tag, double weightKg) {
        if (notBlank(tag)) {
            tagWeights.put(WeightResolverRules.matchKey(tag), Double.valueOf(weightKg));
        }
        return changed();
    }

    // --- limits / precision -------------------------------------------------------------------

    public ScriptConfigOverrides setDefaultCarryCapacity(double kilograms) {
        defaultCarryCapacityKg = Double.valueOf(kilograms);
        return changed();
    }

    public ScriptConfigOverrides setHardLockWeight(double kilograms) {
        hardLockWeightKg = Double.valueOf(kilograms);
        return changed();
    }

    public ScriptConfigOverrides setFailsafeFullScan(boolean enabled) {
        enableFailsafeFullScan = Boolean.valueOf(enabled);
        return changed();
    }

    public ScriptConfigOverrides setFullScanInterval(long ticks) {
        fullScanIntervalTicks = Long.valueOf(ticks);
        return changed();
    }

    public ScriptConfigOverrides setHudDecimals(int decimals) {
        hudDecimals = Integer.valueOf(decimals);
        return changed();
    }

    public ScriptConfigOverrides setTooltipDecimals(int decimals) {
        tooltipDecimals = Integer.valueOf(decimals);
        return changed();
    }

    public ScriptConfigOverrides setStackDecimals(int decimals) {
        stackDecimals = Integer.valueOf(decimals);
        return changed();
    }

    // --- fall damage --------------------------------------------------------------------------

    public ScriptConfigOverrides setFallDamageEnabled(boolean enabled) {
        fallDamageEnabled = Boolean.valueOf(enabled);
        return changed();
    }

    public ScriptConfigOverrides setFallDamageStartLoadPercent(double percent) {
        fallDamageStartLoadPercent = Double.valueOf(percent);
        return changed();
    }

    public ScriptConfigOverrides setFallDamageExtraPerLoadPercent(double multiplier) {
        fallDamageExtraPerLoadPercent = Double.valueOf(multiplier);
        return changed();
    }

    public ScriptConfigOverrides setFallDamageHardLockBonus(double bonus) {
        fallDamageHardLockBonus = Double.valueOf(bonus);
        return changed();
    }

    public ScriptConfigOverrides setFallDamageMaxMultiplier(double multiplier) {
        fallDamageMaxMultiplier = Double.valueOf(multiplier);
        return changed();
    }

    // --- stamina ------------------------------------------------------------------------------

    public ScriptConfigOverrides setStaminaEnabled(boolean enabled) {
        staminaEnabled = Boolean.valueOf(enabled);
        return changed();
    }

    public ScriptConfigOverrides disableStamina() {
        return setStaminaEnabled(false);
    }

    public ScriptConfigOverrides enableStamina() {
        return setStaminaEnabled(true);
    }

    public ScriptConfigOverrides setStaminaTotal(double total) {
        staminaTotal = Double.valueOf(total);
        return changed();
    }

    public ScriptConfigOverrides setSprintStaminaLossRate(double rate) {
        staminaSprintLossRate = Double.valueOf(rate);
        return changed();
    }

    public ScriptConfigOverrides setJumpStaminaLoss(double loss) {
        staminaJumpLoss = Double.valueOf(loss);
        return changed();
    }

    public ScriptConfigOverrides setStaminaGainRate(double rate) {
        staminaGainRate = Double.valueOf(rate);
        return changed();
    }

    public ScriptConfigOverrides setStaminaExhaustionThreshold(double threshold) {
        staminaExhaustionThreshold = Double.valueOf(threshold);
        return changed();
    }

    public ScriptConfigOverrides setStaminaRecoveryPercent(double percent) {
        staminaRecoveryPercent = Double.valueOf(percent);
        return changed();
    }

    public ScriptConfigOverrides setStaminaDrainWhileRunning(boolean drain) {
        staminaDrainWhileRunning = Boolean.valueOf(drain);
        return changed();
    }

    public ScriptConfigOverrides setStaminaDrainOnJump(boolean drain) {
        staminaDrainOnJump = Boolean.valueOf(drain);
        return changed();
    }

    public ScriptConfigOverrides clearStaminaPenalties() {
        staminaPenalties = new ArrayList<WeightConfig.UsagePenaltyRule>();
        return changed();
    }

    public ScriptConfigOverrides addStaminaPenalty(double loadPercent, double useMultiplier) {
        if (staminaPenalties == null) {
            staminaPenalties = new ArrayList<WeightConfig.UsagePenaltyRule>();
        }
        staminaPenalties.add(new WeightConfig.UsagePenaltyRule(loadPercent, useMultiplier));
        return changed();
    }

    // --- movement thresholds ------------------------------------------------------------------

    public ScriptConfigOverrides clearMovementThresholds() {
        movementThresholds = new ArrayList<WeightConfig.ThresholdRule>();
        return changed();
    }

    public ScriptConfigOverrides addMovementThreshold(double loadPercent, double speedMultiplier, double jumpMultiplier) {
        if (movementThresholds == null) {
            movementThresholds = new ArrayList<WeightConfig.ThresholdRule>();
        }
        movementThresholds.add(new WeightConfig.ThresholdRule(loadPercent, speedMultiplier, jumpMultiplier));
        return changed();
    }

    // --- group limits -------------------------------------------------------------------------

    public ScriptConfigOverrides defineGroup(String groupId, String label, int limit) {
        if (notBlank(groupId)) {
            group(groupId).define(label, limit);
        }
        return changed();
    }

    public ScriptConfigOverrides addGroupItem(String groupId, String itemId) {
        return addGroupItem(groupId, itemId, 0);
    }

    public ScriptConfigOverrides addGroupItem(String groupId, String itemId, int metadata) {
        if (notBlank(groupId) && notBlank(itemId)) {
            group(groupId).exact.add(WeightResolverRules.exactKey(itemId, metadata));
        }
        return changed();
    }

    public ScriptConfigOverrides addGroupWildcard(String groupId, String itemId) {
        if (notBlank(groupId) && notBlank(itemId)) {
            group(groupId).wildcard.add(WeightResolverRules.wildcardKey(itemId));
        }
        return changed();
    }

    public ScriptConfigOverrides addGroupTag(String groupId, String tag) {
        if (notBlank(groupId) && notBlank(tag)) {
            group(groupId).tag.add(WeightResolverRules.matchKey(tag));
        }
        return changed();
    }

    // --- equipment bonuses --------------------------------------------------------------------

    public ScriptConfigOverrides setEquipmentBonus(String itemId, double carryCapacityKg, double staminaBonus) {
        if (notBlank(itemId)) {
            EquipmentBonusOverride bonus = equipmentExact(itemId);
            bonus.carryCapacityKg = carryCapacityKg;
            bonus.stamina = staminaBonus;
        }
        return changed();
    }

    public ScriptConfigOverrides setEquipmentCarryBonus(String itemId, double carryCapacityKg) {
        if (notBlank(itemId)) {
            equipmentExact(itemId).carryCapacityKg = carryCapacityKg;
        }
        return changed();
    }

    public ScriptConfigOverrides setEquipmentStaminaBonus(String itemId, double staminaBonus) {
        if (notBlank(itemId)) {
            equipmentExact(itemId).stamina = staminaBonus;
        }
        return changed();
    }

    public ScriptConfigOverrides addEquipmentGroupLimitBonus(String itemId, String groupId, int amount) {
        if (notBlank(itemId) && notBlank(groupId)) {
            equipmentExact(itemId).groupLimits.put(groupId.trim(), Integer.valueOf(amount));
        }
        return changed();
    }

    // --- overlay ------------------------------------------------------------------------------

    public WeightConfig applyTo(WeightConfig base) {
        if (base == null || isEmpty()) {
            return base;
        }

        WeightConfig.Precision basePrecision = base.precision();
        WeightConfig.Precision precision = new WeightConfig.Precision(
            hudDecimals != null ? hudDecimals.intValue() : basePrecision.hudDecimals(),
            tooltipDecimals != null ? tooltipDecimals.intValue() : basePrecision.tooltipDecimals(),
            stackDecimals != null ? stackDecimals.intValue() : basePrecision.stackDecimals()
        );

        WeightConfig.FallDamage baseFall = base.fallDamage();
        WeightConfig.FallDamage fallDamage = new WeightConfig.FallDamage(
            fallDamageEnabled != null ? fallDamageEnabled.booleanValue() : baseFall.enabled(),
            fallDamageStartLoadPercent != null ? fallDamageStartLoadPercent.doubleValue() : baseFall.startLoadPercent(),
            fallDamageExtraPerLoadPercent != null
                ? fallDamageExtraPerLoadPercent.doubleValue()
                : baseFall.extraDamageMultiplierPerLoadPercent(),
            fallDamageHardLockBonus != null ? fallDamageHardLockBonus.doubleValue() : baseFall.hardLockMultiplierBonus(),
            fallDamageMaxMultiplier != null ? fallDamageMaxMultiplier.doubleValue() : baseFall.maxDamageMultiplier()
        );

        WeightConfig.Stamina baseStamina = base.stamina();
        WeightConfig.Stamina stamina = new WeightConfig.Stamina(
            staminaEnabled != null ? staminaEnabled.booleanValue() : baseStamina.enabled(),
            staminaTotal != null ? staminaTotal.doubleValue() : baseStamina.totalStamina(),
            staminaSprintLossRate != null ? staminaSprintLossRate.doubleValue() : baseStamina.sprintStaminaLossRate(),
            staminaJumpLoss != null ? staminaJumpLoss.doubleValue() : baseStamina.jumpStaminaLoss(),
            staminaGainRate != null ? staminaGainRate.doubleValue() : baseStamina.staminaGainRate(),
            staminaExhaustionThreshold != null
                ? staminaExhaustionThreshold.doubleValue()
                : baseStamina.exhaustionThreshold(),
            staminaRecoveryPercent != null ? staminaRecoveryPercent.doubleValue() : baseStamina.recoveryPercent(),
            staminaDrainWhileRunning != null ? staminaDrainWhileRunning.booleanValue() : baseStamina.drainWhileRunning(),
            staminaDrainOnJump != null ? staminaDrainOnJump.booleanValue() : baseStamina.drainOnJump(),
            staminaPenalties != null ? staminaPenalties : baseStamina.penalties()
        );

        return new WeightConfig(
            precision,
            enableFailsafeFullScan != null ? enableFailsafeFullScan.booleanValue() : base.enableFailsafeFullScan(),
            fullScanIntervalTicks != null ? fullScanIntervalTicks.longValue() : base.fullScanIntervalTicks(),
            defaultCarryCapacityKg != null ? defaultCarryCapacityKg.doubleValue() : base.defaultCarryCapacityKg(),
            hardLockWeightKg != null ? hardLockWeightKg.doubleValue() : base.hardLockWeightKg(),
            buildResolverRules(base.resolverRules()),
            buildGroupRules(base.inventoryGroupRules()),
            buildEquipmentRules(base.equipmentBonusRules()),
            movementThresholds != null ? movementThresholds : base.thresholds(),
            fallDamage,
            stamina
        );
    }

    private WeightResolverRules buildResolverRules(WeightResolverRules base) {
        WeightResolverRules.Builder builder = new WeightResolverRules.Builder();
        copyDoubleMap(base.exactWeights(), builder, RuleTarget.EXACT);
        copyDoubleMap(base.wildcardWeights(), builder, RuleTarget.WILDCARD);
        copyDoubleMap(base.matchWeights(), builder, RuleTarget.MATCH);
        for (Map.Entry<String, Double> entry : exactWeights.entrySet()) {
            builder.putExactKey(entry.getKey(), entry.getValue().doubleValue());
        }
        for (Map.Entry<String, Double> entry : wildcardWeights.entrySet()) {
            builder.putWildcardKey(entry.getKey(), entry.getValue().doubleValue());
        }
        for (Map.Entry<String, Double> entry : tagWeights.entrySet()) {
            builder.putMatchKey(entry.getKey(), entry.getValue().doubleValue());
        }
        return builder.build();
    }

    private InventoryGroupRules buildGroupRules(InventoryGroupRules base) {
        InventoryGroupRules.Builder builder = new InventoryGroupRules.Builder();
        for (InventoryGroupRules.GroupDefinition definition : base.definitions()) {
            builder.define(definition.id(), definition.label(), definition.limit());
        }
        copyGroupMembership(base.exactMatches(), builder, RuleTarget.EXACT);
        copyGroupMembership(base.wildcardMatches(), builder, RuleTarget.WILDCARD);
        copyGroupMembership(base.dictionaryMatches(), builder, RuleTarget.MATCH);

        for (GroupOverride group : groups.values()) {
            if (group.defined) {
                builder.define(group.id, group.label, group.limit);
            }
            for (String exactKey : group.exact) {
                String[] parts = splitExactKey(exactKey);
                builder.addExact(group.id, parts[0], parseMeta(parts[1]));
            }
            for (String wildcard : group.wildcard) {
                builder.addWildcard(group.id, wildcard);
            }
            for (String tag : group.tag) {
                builder.addMatch(group.id, tag);
            }
        }
        return builder.build();
    }

    private EquipmentBonusRules buildEquipmentRules(EquipmentBonusRules base) {
        EquipmentBonusRules.Builder builder = new EquipmentBonusRules.Builder();
        for (Map.Entry<String, EquipmentBonusRules.EquipmentBonus> entry : base.exactBonuses().entrySet()) {
            builder.putExactKey(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, EquipmentBonusRules.EquipmentBonus> entry : base.wildcardBonuses().entrySet()) {
            builder.putWildcardKey(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, EquipmentBonusOverride> entry : equipmentExact.entrySet()) {
            builder.putExactKey(WeightResolverRules.exactKey(entry.getKey(), 0), entry.getValue().toBonus());
        }
        for (Map.Entry<String, EquipmentBonusOverride> entry : equipmentWildcard.entrySet()) {
            builder.putWildcardKey(WeightResolverRules.wildcardKey(entry.getKey()), entry.getValue().toBonus());
        }
        return builder.build();
    }

    private static void copyDoubleMap(Object2DoubleOpenHashMap<String> source, WeightResolverRules.Builder builder, RuleTarget target) {
        for (String key : source.keySet()) {
            double value = source.getDouble(key);
            switch (target) {
                case EXACT:
                    builder.putExactKey(key, value);
                    break;
                case WILDCARD:
                    builder.putWildcardKey(key, value);
                    break;
                default:
                    builder.putMatchKey(key, value);
                    break;
            }
        }
    }

    private static void copyGroupMembership(Map<String, List<String>> source, InventoryGroupRules.Builder builder, RuleTarget target) {
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            for (String groupId : entry.getValue()) {
                switch (target) {
                    case EXACT:
                        String[] parts = splitExactKey(entry.getKey());
                        builder.addExact(groupId, parts[0], parseMeta(parts[1]));
                        break;
                    case WILDCARD:
                        builder.addWildcard(groupId, entry.getKey());
                        break;
                    default:
                        builder.addMatch(groupId, entry.getKey());
                        break;
                }
            }
        }
    }

    private GroupOverride group(String groupId) {
        String id = groupId.trim();
        GroupOverride override = groups.get(id);
        if (override == null) {
            override = new GroupOverride(id);
            groups.put(id, override);
        }
        return override;
    }

    private EquipmentBonusOverride equipmentExact(String itemId) {
        String id = itemId.trim();
        EquipmentBonusOverride override = equipmentExact.get(id);
        if (override == null) {
            override = new EquipmentBonusOverride();
            equipmentExact.put(id, override);
        }
        return override;
    }

    private static String[] splitExactKey(String exactKey) {
        int separator = exactKey.lastIndexOf('@');
        if (separator < 0) {
            return new String[] {exactKey, "0"};
        }
        return new String[] {exactKey.substring(0, separator), exactKey.substring(separator + 1)};
    }

    private static int parseMeta(String token) {
        try {
            return Integer.parseInt(token.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private enum RuleTarget {
        EXACT,
        WILDCARD,
        MATCH
    }

    private static final class GroupOverride {
        private final String id;
        private boolean defined;
        private String label;
        private int limit;
        private final List<String> exact = new ArrayList<String>();
        private final List<String> wildcard = new ArrayList<String>();
        private final List<String> tag = new ArrayList<String>();

        private GroupOverride(String id) {
            this.id = id;
            this.label = id;
        }

        private void define(String label, int limit) {
            this.defined = true;
            this.label = label == null || label.trim().isEmpty() ? id : label.trim();
            this.limit = limit;
        }
    }

    private static final class EquipmentBonusOverride {
        private double carryCapacityKg;
        private double stamina;
        private final Map<String, Integer> groupLimits = new LinkedHashMap<String, Integer>();

        private EquipmentBonusRules.EquipmentBonus toBonus() {
            return new EquipmentBonusRules.EquipmentBonus(carryCapacityKg, stamina, groupLimits);
        }
    }
}

package com.warfactory.ultimateweight.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WeightConfig {
    private final Precision precision;
    private final boolean enableFailsafeFullScan;
    private final long fullScanIntervalTicks;
    private final double defaultCarryCapacityKg;
    private final double hardLockWeightKg;
    private final String componentOverrideKey;
    private final Map<String, Double> exactWeightsKg;
    private final Map<String, Double> groupWeightsKg;
    private final Map<String, Double> prefixWeightsKg;
    private final List<ThresholdRule> thresholds;
    private final FallDamage fallDamage;
    private final Stamina stamina;

    public WeightConfig(
        Precision precision,
        boolean enableFailsafeFullScan,
        long fullScanIntervalTicks,
        double defaultCarryCapacityKg,
        double hardLockWeightKg,
        String componentOverrideKey,
        Map<String, Double> exactWeightsKg,
        Map<String, Double> groupWeightsKg,
        Map<String, Double> prefixWeightsKg,
        Collection<ThresholdRule> thresholds
    ) {
        this(
            precision,
            enableFailsafeFullScan,
            fullScanIntervalTicks,
            defaultCarryCapacityKg,
            hardLockWeightKg,
            componentOverrideKey,
            exactWeightsKg,
            groupWeightsKg,
            prefixWeightsKg,
            thresholds,
            FallDamage.defaults(),
            Stamina.defaults()
        );
    }

    public WeightConfig(
        Precision precision,
        boolean enableFailsafeFullScan,
        long fullScanIntervalTicks,
        double defaultCarryCapacityKg,
        double hardLockWeightKg,
        String componentOverrideKey,
        Map<String, Double> exactWeightsKg,
        Map<String, Double> groupWeightsKg,
        Map<String, Double> prefixWeightsKg,
        Collection<ThresholdRule> thresholds,
        FallDamage fallDamage
    ) {
        this(
            precision,
            enableFailsafeFullScan,
            fullScanIntervalTicks,
            defaultCarryCapacityKg,
            hardLockWeightKg,
            componentOverrideKey,
            exactWeightsKg,
            groupWeightsKg,
            prefixWeightsKg,
            thresholds,
            fallDamage,
            Stamina.defaults()
        );
    }

    public WeightConfig(
        Precision precision,
        boolean enableFailsafeFullScan,
        long fullScanIntervalTicks,
        double defaultCarryCapacityKg,
        double hardLockWeightKg,
        String componentOverrideKey,
        Map<String, Double> exactWeightsKg,
        Map<String, Double> groupWeightsKg,
        Map<String, Double> prefixWeightsKg,
        Collection<ThresholdRule> thresholds,
        FallDamage fallDamage,
        Stamina stamina
    ) {
        this.precision = precision == null ? Precision.defaults() : precision;
        this.enableFailsafeFullScan = enableFailsafeFullScan;
        this.fullScanIntervalTicks = fullScanIntervalTicks <= 0L ? 600L : fullScanIntervalTicks;
        this.defaultCarryCapacityKg = defaultCarryCapacityKg <= 0.0D ? 120.0D : defaultCarryCapacityKg;
        this.hardLockWeightKg = hardLockWeightKg <= 0.0D ? 220.0D : hardLockWeightKg;
        this.componentOverrideKey = isBlank(componentOverrideKey) ? "uWeight" : componentOverrideKey;
        this.exactWeightsKg = immutableWeights(exactWeightsKg);
        this.groupWeightsKg = immutableWeights(groupWeightsKg);
        this.prefixWeightsKg = immutableWeights(prefixWeightsKg);
        this.thresholds = immutableThresholds(thresholds);
        this.fallDamage = fallDamage == null ? FallDamage.defaults() : fallDamage;
        this.stamina = stamina == null ? Stamina.defaults() : stamina;
    }

    public static WeightConfig defaults() {
        LinkedHashMap<String, Double> exact = new LinkedHashMap<String, Double>();
        exact.put("minecraft:water_bucket", 4.5D);
        exact.put("minecraft:lava_bucket", 4.8D);
        exact.put("minecraft:shield", 5.0D);
        exact.put("minecraft:elytra", 6.0D);
        exact.put("minecraft:shulker_box", 8.0D);

        LinkedHashMap<String, Double> groups = new LinkedHashMap<String, Double>();
        groups.put("cobblestone", 1.4D);
        groups.put("stone", 1.5D);
        groups.put("logWood", 2.4D);
        groups.put("plankWood", 0.45D);
        groups.put("stickWood", 0.08D);
        groups.put("oreIron", 3.8D);
        groups.put("ingotIron", 0.9D);
        groups.put("ingotCopper", 0.85D);
        groups.put("ingotGold", 1.3D);
        groups.put("ingotSilver", 1.1D);
        groups.put("gemEmerald", 0.32D);
        groups.put("gemDiamond", 0.35D);
        groups.put("dustRedstone", 0.05D);
        groups.put("dustGlowstone", 0.07D);
        groups.put("coal", 0.25D);
        groups.put("paper", 0.03D);
        groups.put("string", 0.02D);

        LinkedHashMap<String, Double> prefixes = new LinkedHashMap<String, Double>();
        prefixes.put("ingot", 0.9D);
        prefixes.put("dust", 0.08D);
        prefixes.put("plate", 1.1D);
        prefixes.put("gear", 3.6D);

        ArrayList<ThresholdRule> thresholds = new ArrayList<ThresholdRule>();
        thresholds.add(new ThresholdRule(0.50D, 0.92D, 0.96D));
        thresholds.add(new ThresholdRule(0.75D, 0.78D, 0.86D));
        thresholds.add(new ThresholdRule(1.00D, 0.55D, 0.65D));
        thresholds.add(new ThresholdRule(1.20D, 0.18D, 0.25D));

        return new WeightConfig(
            Precision.defaults(),
            true,
            600L,
            120.0D,
            220.0D,
            "uWeight",
            exact,
            groups,
            prefixes,
            thresholds,
            FallDamage.defaults(),
            Stamina.defaults()
        );
    }

    public Precision precision() {
        return precision;
    }

    public boolean enableFailsafeFullScan() {
        return enableFailsafeFullScan;
    }

    public long fullScanIntervalTicks() {
        return fullScanIntervalTicks;
    }

    public double defaultCarryCapacityKg() {
        return defaultCarryCapacityKg;
    }

    public double hardLockWeightKg() {
        return hardLockWeightKg;
    }

    public String componentOverrideKey() {
        return componentOverrideKey;
    }

    public Map<String, Double> exactWeightsKg() {
        return exactWeightsKg;
    }

    public Map<String, Double> groupWeightsKg() {
        return groupWeightsKg;
    }

    public Map<String, Double> prefixWeightsKg() {
        return prefixWeightsKg;
    }

    public List<ThresholdRule> thresholds() {
        return thresholds;
    }

    public FallDamage fallDamage() {
        return fallDamage;
    }

    public Stamina stamina() {
        return stamina;
    }

    private static Map<String, Double> immutableWeights(Map<String, Double> weights) {
        LinkedHashMap<String, Double> copy = new LinkedHashMap<String, Double>();
        if (weights != null) {
            for (Map.Entry<String, Double> entry : weights.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    copy.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return Collections.unmodifiableMap(copy);
    }

    private static List<ThresholdRule> immutableThresholds(Collection<ThresholdRule> thresholds) {
        ArrayList<ThresholdRule> copy = new ArrayList<ThresholdRule>();
        if (thresholds != null) {
            for (ThresholdRule threshold : thresholds) {
                if (threshold != null) {
                    copy.add(threshold);
                }
            }
        }
        if (copy.isEmpty()) {
            copy.addAll(defaults().thresholds());
        }
        Collections.sort(copy, new Comparator<ThresholdRule>() {
            @Override
            public int compare(ThresholdRule left, ThresholdRule right) {
                return Double.compare(left.percent(), right.percent());
            }
        });
        return Collections.unmodifiableList(copy);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static final class Precision {
        private final int hudDecimals;
        private final int tooltipDecimals;
        private final int stackDecimals;

        public Precision(int hudDecimals, int tooltipDecimals, int stackDecimals) {
            this.hudDecimals = Math.max(0, hudDecimals);
            this.tooltipDecimals = Math.max(0, tooltipDecimals);
            this.stackDecimals = Math.max(0, stackDecimals);
        }

        public static Precision defaults() {
            return new Precision(1, 1, 2);
        }

        public int hudDecimals() {
            return hudDecimals;
        }

        public int tooltipDecimals() {
            return tooltipDecimals;
        }

        public int stackDecimals() {
            return stackDecimals;
        }
    }

    public static final class ThresholdRule {
        private final double percent;
        private final double speedMultiplier;
        private final double jumpMultiplier;

        public ThresholdRule(double percent, double speedMultiplier, double jumpMultiplier) {
            this.percent = percent;
            this.speedMultiplier = speedMultiplier;
            this.jumpMultiplier = jumpMultiplier;
        }

        public double percent() {
            return percent;
        }

        public double speedMultiplier() {
            return speedMultiplier;
        }

        public double jumpMultiplier() {
            return jumpMultiplier;
        }
    }

    public static final class FallDamage {
        private final boolean enabled;
        private final double startLoadPercent;
        private final double extraDamageMultiplierPerLoadPercent;
        private final double hardLockMultiplierBonus;
        private final double maxDamageMultiplier;

        public FallDamage(
            boolean enabled,
            double startLoadPercent,
            double extraDamageMultiplierPerLoadPercent,
            double hardLockMultiplierBonus,
            double maxDamageMultiplier
        ) {
            this.enabled = enabled;
            this.startLoadPercent = startLoadPercent < 0.0D ? 0.0D : startLoadPercent;
            this.extraDamageMultiplierPerLoadPercent = extraDamageMultiplierPerLoadPercent < 0.0D
                ? 0.0D
                : extraDamageMultiplierPerLoadPercent;
            this.hardLockMultiplierBonus = hardLockMultiplierBonus < 0.0D ? 0.0D : hardLockMultiplierBonus;
            this.maxDamageMultiplier = maxDamageMultiplier < 1.0D ? 1.0D : maxDamageMultiplier;
        }

        public static FallDamage defaults() {
            return new FallDamage(true, 0.85D, 1.2D, 0.75D, 3.5D);
        }

        public boolean enabled() {
            return enabled;
        }

        public double startLoadPercent() {
            return startLoadPercent;
        }

        public double extraDamageMultiplierPerLoadPercent() {
            return extraDamageMultiplierPerLoadPercent;
        }

        public double hardLockMultiplierBonus() {
            return hardLockMultiplierBonus;
        }

        public double maxDamageMultiplier() {
            return maxDamageMultiplier;
        }
    }

    public static final class Stamina {
        private final double totalStamina;
        private final double sprintStaminaLossRate;
        private final double jumpStaminaLoss;
        private final double staminaGainRate;
        private final boolean drainWhileRunning;
        private final boolean drainOnJump;
        private final List<UsagePenaltyRule> penalties;

        public Stamina(
            double totalStamina,
            double sprintStaminaLossRate,
            double jumpStaminaLoss,
            double staminaGainRate,
            boolean drainWhileRunning,
            boolean drainOnJump,
            Collection<UsagePenaltyRule> penalties
        ) {
            this.totalStamina = totalStamina <= 0.0D ? 100.0D : totalStamina;
            this.sprintStaminaLossRate = sprintStaminaLossRate < 0.0D ? 0.0D : sprintStaminaLossRate;
            this.jumpStaminaLoss = jumpStaminaLoss < 0.0D ? 0.0D : jumpStaminaLoss;
            this.staminaGainRate = staminaGainRate < 0.0D ? 0.0D : staminaGainRate;
            this.drainWhileRunning = drainWhileRunning;
            this.drainOnJump = drainOnJump;
            this.penalties = immutablePenaltyRules(penalties);
        }

        public static Stamina defaults() {
            ArrayList<UsagePenaltyRule> penalties = new ArrayList<UsagePenaltyRule>();
            penalties.add(new UsagePenaltyRule(0.50D, 1.10D));
            penalties.add(new UsagePenaltyRule(0.75D, 1.35D));
            penalties.add(new UsagePenaltyRule(1.00D, 1.70D));
            penalties.add(new UsagePenaltyRule(1.20D, 2.20D));
            return new Stamina(100.0D, 0.10D, 2.0D, 0.08D, true, true, penalties);
        }

        public double totalStamina() {
            return totalStamina;
        }

        public double sprintStaminaLossRate() {
            return sprintStaminaLossRate;
        }

        public double jumpStaminaLoss() {
            return jumpStaminaLoss;
        }

        public double staminaGainRate() {
            return staminaGainRate;
        }

        public boolean drainWhileRunning() {
            return drainWhileRunning;
        }

        public boolean drainOnJump() {
            return drainOnJump;
        }

        public List<UsagePenaltyRule> penalties() {
            return penalties;
        }

        private static List<UsagePenaltyRule> immutablePenaltyRules(Collection<UsagePenaltyRule> penalties) {
            ArrayList<UsagePenaltyRule> copy = new ArrayList<UsagePenaltyRule>();
            if (penalties != null) {
                for (UsagePenaltyRule penalty : penalties) {
                    if (penalty != null) {
                        copy.add(penalty);
                    }
                }
            }
            if (copy.isEmpty()) {
                copy.addAll(defaults().penalties());
            }
            Collections.sort(copy, new Comparator<UsagePenaltyRule>() {
                @Override
                public int compare(UsagePenaltyRule left, UsagePenaltyRule right) {
                    return Double.compare(left.percent(), right.percent());
                }
            });
            return Collections.unmodifiableList(copy);
        }
    }

    public static final class UsagePenaltyRule {
        private final double percent;
        private final double useMultiplier;

        public UsagePenaltyRule(double percent, double useMultiplier) {
            this.percent = percent;
            this.useMultiplier = useMultiplier < 0.0D ? 0.0D : useMultiplier;
        }

        public double percent() {
            return percent;
        }

        public double useMultiplier() {
            return useMultiplier;
        }
    }
}

package com.warfactory.ultimateweight.config;

import java.util.*;

public final class WeightConfig {
    private final Precision precision;
    private final boolean enableFailsafeFullScan;
    private final long fullScanIntervalTicks;
    private final double defaultCarryCapacityKg;
    private final double hardLockWeightKg;
    private final WeightResolverRules resolverRules;
    private final InventoryGroupRules inventoryGroupRules;
    private final EquipmentBonusRules equipmentBonusRules;
    private final List<ThresholdRule> thresholds;
    private final FallDamage fallDamage;
    private final OverweightDamage overweightDamage;
    private final Stamina stamina;

    public WeightConfig(
        Precision precision,
        boolean enableFailsafeFullScan,
        long fullScanIntervalTicks,
        double defaultCarryCapacityKg,
        double hardLockWeightKg,
        WeightResolverRules resolverRules,
        InventoryGroupRules inventoryGroupRules,
        EquipmentBonusRules equipmentBonusRules,
        Collection<ThresholdRule> thresholds,
        FallDamage fallDamage,
        OverweightDamage overweightDamage,
        Stamina stamina
    ) {
        this.precision = precision == null ? Precision.defaults() : precision;
        this.enableFailsafeFullScan = enableFailsafeFullScan;
        this.fullScanIntervalTicks = fullScanIntervalTicks <= 0L ? 600L : fullScanIntervalTicks;
        this.defaultCarryCapacityKg = defaultCarryCapacityKg <= 0.0D ? 120.0D : defaultCarryCapacityKg;
        this.hardLockWeightKg = hardLockWeightKg <= 0.0D ? 220.0D : hardLockWeightKg;
        this.resolverRules = resolverRules == null ? WeightResolverRules.empty() : resolverRules;
        this.inventoryGroupRules = inventoryGroupRules == null ? InventoryGroupRules.empty() : inventoryGroupRules;
        this.equipmentBonusRules = equipmentBonusRules == null ? EquipmentBonusRules.empty() : equipmentBonusRules;
        this.thresholds = immutableThresholds(thresholds);
        this.fallDamage = fallDamage == null ? FallDamage.defaults() : fallDamage;
        this.overweightDamage = overweightDamage == null ? OverweightDamage.defaults() : overweightDamage;
        this.stamina = stamina == null ? Stamina.defaults() : stamina;
    }

    public static WeightConfig defaults() {
        return defaults(WeightResolverRules.empty());
    }

    public static WeightConfig defaults(WeightResolverRules resolverRules) {
        ArrayList<ThresholdRule> thresholds = new ArrayList<ThresholdRule>();
        thresholds.add(new ThresholdRule(0.50D, 0.92D, 0.96D));
        thresholds.add(new ThresholdRule(0.75D, 0.78D, 0.86D));
        thresholds.add(new ThresholdRule(1.00D, 0.55D, 0.65D));
        thresholds.add(new ThresholdRule(1.20D, 0.18D, 0.25D));

        return new WeightConfig(
            Precision.defaults(),
            false,
            600L,
            120.0D,
            220.0D,
            resolverRules,
            InventoryGroupRules.empty(),
            EquipmentBonusRules.empty(),
            thresholds,
            FallDamage.defaults(),
            OverweightDamage.defaults(),
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

    public WeightResolverRules resolverRules() {
        return resolverRules;
    }

    public InventoryGroupRules inventoryGroupRules() {
        return inventoryGroupRules;
    }

    public EquipmentBonusRules equipmentBonusRules() {
        return equipmentBonusRules;
    }

    public List<ThresholdRule> thresholds() {
        return thresholds;
    }

    public FallDamage fallDamage() {
        return fallDamage;
    }

    public OverweightDamage overweightDamage() {
        return overweightDamage;
    }

    public Stamina stamina() {
        return stamina;
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
            this.startLoadPercent = startLoadPercent;
            this.extraDamageMultiplierPerLoadPercent = extraDamageMultiplierPerLoadPercent;
            this.hardLockMultiplierBonus = hardLockMultiplierBonus;
            this.maxDamageMultiplier = maxDamageMultiplier;
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


    public static final class OverweightDamage {
        private final boolean enabled;
        private final double startLoadPercent;
        private final double damagePerInterval;
        private final double extraDamagePerLoadPercent;
        private final double hardLockDamageBonus;
        private final double maxDamagePerInterval;
        private final long intervalTicks;
        private final double minHealth;

        public OverweightDamage(
            boolean enabled,
            double startLoadPercent,
            double damagePerInterval,
            double extraDamagePerLoadPercent,
            double hardLockDamageBonus,
            double maxDamagePerInterval,
            long intervalTicks,
            double minHealth
        ) {
            this.enabled = enabled;
            this.startLoadPercent = startLoadPercent;
            this.damagePerInterval = damagePerInterval;
            this.extraDamagePerLoadPercent = extraDamagePerLoadPercent;
            this.hardLockDamageBonus = hardLockDamageBonus;
            this.maxDamagePerInterval = maxDamagePerInterval;
            this.intervalTicks = intervalTicks <= 0L ? 1L : intervalTicks;
            this.minHealth = Math.max(0.0D, minHealth);
        }

        public static OverweightDamage defaults() {
            return new OverweightDamage(false, 1.20D, 1.0D, 2.0D, 1.0D, 6.0D, 40L, 0.0D);
        }

        /** Master switch. When false no overweight damage is ever dealt. */
        public boolean enabled() {
            return enabled;
        }

        /** Damage starts only above this load percentage (weight / effective carry capacity). */
        public double startLoadPercent() {
            return startLoadPercent;
        }

        /** Base damage dealt once per interval while above {@link #startLoadPercent()}. */
        public double damagePerInterval() {
            return damagePerInterval;
        }

        /** Extra damage added per {@code 1.0} load above the start threshold. */
        public double extraDamagePerLoadPercent() {
            return extraDamagePerLoadPercent;
        }

        /** Extra damage added while hard-locked. */
        public double hardLockDamageBonus() {
            return hardLockDamageBonus;
        }

        /** Hard cap for a single damage tick. */
        public double maxDamagePerInterval() {
            return maxDamagePerInterval;
        }

        /** Ticks between damage applications. Clamped to at least {@code 1}. */
        public long intervalTicks() {
            return intervalTicks;
        }

        /**
         * Health floor: damage is reduced so it never takes a player below this value. {@code 0}
         * (the default) lets overweight damage kill.
         */
        public double minHealth() {
            return minHealth;
        }
    }

    public static final class Stamina {
        private final boolean enabled;
        private final double totalStamina;
        private final double sprintStaminaLossRate;
        private final double jumpStaminaLoss;
        private final double staminaGainRate;
        private final double exhaustionThreshold;
        private final double recoveryPercent;
        private final boolean drainWhileRunning;
        private final boolean drainOnJump;
        private final List<UsagePenaltyRule> penalties;

        public Stamina(
            boolean enabled,
            double totalStamina,
            double sprintStaminaLossRate,
            double jumpStaminaLoss,
            double staminaGainRate,
            double exhaustionThreshold,
            double recoveryPercent,
            boolean drainWhileRunning,
            boolean drainOnJump,
            Collection<UsagePenaltyRule> penalties
        ) {
            this.enabled = enabled;
            this.totalStamina = totalStamina;
            this.sprintStaminaLossRate = sprintStaminaLossRate;
            this.jumpStaminaLoss = jumpStaminaLoss;
            this.staminaGainRate = staminaGainRate;
            this.exhaustionThreshold = exhaustionThreshold;
            this.recoveryPercent = recoveryPercent;
            this.drainWhileRunning = drainWhileRunning;
            this.drainOnJump = drainOnJump;
            this.penalties = immutablePenalties(penalties);
        }

        public static Stamina defaults() {
            ArrayList<UsagePenaltyRule> penalties = new ArrayList<UsagePenaltyRule>();
            penalties.add(new UsagePenaltyRule(0.50D, 1.1D));
            penalties.add(new UsagePenaltyRule(0.75D, 1.35D));
            penalties.add(new UsagePenaltyRule(1.00D, 1.7D));
            penalties.add(new UsagePenaltyRule(1.20D, 2.2D));
            return new Stamina(true, 100.0D, 0.1D, 2.0D, 0.08D, 1.0D, 0.3D, true, true, penalties);
        }

        /**
         * Master switch for the whole stamina system. When false the mod keeps every player's
         * stamina full and never drains it - the HUD bar hides and sprint is never blocked. Exposed
         * to config ({@code stamina.enabled}) and to scripting (GroovyScript / KubeJS).
         */
        public boolean enabled() {
            return enabled;
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

        public double exhaustionThreshold() {
            return exhaustionThreshold;
        }

        public double recoveryPercent() {
            return recoveryPercent;
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
    }

    public static final class UsagePenaltyRule {
        private final double percent;
        private final double useMultiplier;

        public UsagePenaltyRule(double percent, double useMultiplier) {
            this.percent = percent;
            this.useMultiplier = useMultiplier;
        }

        public double percent() {
            return percent;
        }

        public double useMultiplier() {
            return useMultiplier;
        }
    }

    private static List<UsagePenaltyRule> immutablePenalties(Collection<UsagePenaltyRule> penalties) {
        ArrayList<UsagePenaltyRule> copy = new ArrayList<UsagePenaltyRule>();
        if (penalties != null) {
            for (UsagePenaltyRule penalty : penalties) {
                if (penalty != null) {
                    copy.add(penalty);
                }
            }
        }
        if (copy.isEmpty()) {
            copy.addAll(Stamina.defaults().penalties());
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

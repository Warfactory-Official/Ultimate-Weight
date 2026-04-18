package com.warfactory.ultimateweight.config;

import com.warfactory.ultimateweight.api.WeightStackView;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EquipmentBonusRules {
    private final Map<String, EquipmentBonus> exactBonuses;
    private final Map<String, EquipmentBonus> wildcardBonuses;

    private EquipmentBonusRules(Map<String, EquipmentBonus> exactBonuses, Map<String, EquipmentBonus> wildcardBonuses) {
        this.exactBonuses = immutableBonuses(exactBonuses);
        this.wildcardBonuses = immutableBonuses(wildcardBonuses);
    }

    public static EquipmentBonusRules empty() {
        return new Builder().build();
    }

    public Map<String, EquipmentBonus> exactBonuses() {
        return exactBonuses;
    }

    public Map<String, EquipmentBonus> wildcardBonuses() {
        return wildcardBonuses;
    }

    public EquipmentBonus resolve(WeightStackView stack) {
        if (stack == null || stack.item() == null) {
            return EquipmentBonus.empty();
        }

        EquipmentBonus exact = exactBonuses.get(WeightResolverRules.exactKey(stack.item().itemId(), stack.metadata()));
        if (exact != null) {
            return exact;
        }

        EquipmentBonus wildcard = wildcardBonuses.get(WeightResolverRules.wildcardKey(stack.item().itemId()));
        return wildcard == null ? EquipmentBonus.empty() : wildcard;
    }

    private static Map<String, EquipmentBonus> immutableBonuses(Map<String, EquipmentBonus> source) {
        LinkedHashMap<String, EquipmentBonus> copy = new LinkedHashMap<String, EquipmentBonus>();
        if (source != null) {
            for (Map.Entry<String, EquipmentBonus> entry : source.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                    copy.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return Collections.unmodifiableMap(copy);
    }

    public static final class EquipmentBonus {
        private final double carryCapacityKg;
        private final double stamina;
        private final Map<String, Integer> groupLimitBonuses;

        public EquipmentBonus(double carryCapacityKg, double stamina, Map<String, Integer> groupLimitBonuses) {
            this.carryCapacityKg = carryCapacityKg;
            this.stamina = stamina;
            this.groupLimitBonuses = immutableGroupBonuses(groupLimitBonuses);
        }

        public static EquipmentBonus empty() {
            return new EquipmentBonus(0.0D, 0.0D, Collections.<String, Integer>emptyMap());
        }

        public double carryCapacityKg() {
            return carryCapacityKg;
        }

        public double stamina() {
            return stamina;
        }

        public Map<String, Integer> groupLimitBonuses() {
            return groupLimitBonuses;
        }

        public boolean isEmpty() {
            return Math.abs(carryCapacityKg) <= 0.000001D
                && Math.abs(stamina) <= 0.000001D
                && groupLimitBonuses.isEmpty();
        }

        public EquipmentBonus merge(EquipmentBonus other) {
            if (other == null || other.isEmpty()) {
                return this;
            }
            if (this.isEmpty()) {
                return other;
            }

            LinkedHashMap<String, Integer> bonuses = new LinkedHashMap<String, Integer>(groupLimitBonuses);
            for (Map.Entry<String, Integer> entry : other.groupLimitBonuses.entrySet()) {
                bonuses.put(entry.getKey(), Integer.valueOf(bonuses.getOrDefault(entry.getKey(), Integer.valueOf(0)).intValue() + entry.getValue().intValue()));
            }
            return new EquipmentBonus(carryCapacityKg + other.carryCapacityKg, stamina + other.stamina, bonuses);
        }

        private static Map<String, Integer> immutableGroupBonuses(Map<String, Integer> source) {
            LinkedHashMap<String, Integer> copy = new LinkedHashMap<String, Integer>();
            if (source != null) {
                for (Map.Entry<String, Integer> entry : source.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null && entry.getValue().intValue() != 0) {
                        copy.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            return Collections.unmodifiableMap(copy);
        }
    }

    public static final class Builder {
        private final Map<String, EquipmentBonus> exactBonuses = new LinkedHashMap<String, EquipmentBonus>();
        private final Map<String, EquipmentBonus> wildcardBonuses = new LinkedHashMap<String, EquipmentBonus>();

        public Builder putExact(String itemId, int metadata, EquipmentBonus bonus) {
            return put(exactBonuses, WeightResolverRules.exactKey(itemId, metadata), bonus);
        }

        public Builder putWildcard(String itemId, EquipmentBonus bonus) {
            return put(wildcardBonuses, WeightResolverRules.wildcardKey(itemId), bonus);
        }

        public Builder putExactKey(String key, EquipmentBonus bonus) {
            return put(exactBonuses, key, bonus);
        }

        public Builder putWildcardKey(String key, EquipmentBonus bonus) {
            return put(wildcardBonuses, key, bonus);
        }

        public EquipmentBonusRules build() {
            return new EquipmentBonusRules(exactBonuses, wildcardBonuses);
        }

        private Builder put(Map<String, EquipmentBonus> target, String key, EquipmentBonus bonus) {
            if (key == null || key.isEmpty() || bonus == null || bonus.isEmpty()) {
                return this;
            }
            target.put(key, bonus);
            return this;
        }
    }
}

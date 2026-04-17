package com.warfactory.ultimateweight.core;

import com.warfactory.ultimateweight.api.WeightPlayerView;
import com.warfactory.ultimateweight.api.WeightStackView;
import com.warfactory.ultimateweight.config.EquipmentBonusRules;
import com.warfactory.ultimateweight.config.InventoryGroupRules;
import com.warfactory.ultimateweight.config.WeightConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InventoryConstraintEvaluator {
    private final WeightConfig config;

    public InventoryConstraintEvaluator(WeightConfig config) {
        this.config = config;
    }

    public double resolveCarryCapacityKg(WeightPlayerView player) {
        return Math.max(0.0D, config.defaultCarryCapacityKg() + resolveEquipmentStats(player).carryCapacityKgBonus());
    }

    public double resolveMaxStamina(WeightPlayerView player) {
        return Math.max(0.0D, config.stamina().totalStamina() + resolveEquipmentStats(player).staminaBonus());
    }

    public EquipmentStats resolveEquipmentStats(WeightPlayerView player) {
        EquipmentBonusRules rules = config.equipmentBonusRules();
        if (player == null || rules == null) {
            return EquipmentStats.empty();
        }

        double carryCapacity = 0.0D;
        double stamina = 0.0D;
        LinkedHashMap<String, Integer> groupBonuses = new LinkedHashMap<String, Integer>();
        for (WeightStackView stack : player.equipped()) {
            if (stack == null) {
                continue;
            }
            EquipmentBonusRules.EquipmentBonus bonus = rules.resolve(stack);
            carryCapacity += bonus.carryCapacityKg();
            stamina += bonus.stamina();
            for (Map.Entry<String, Integer> entry : bonus.groupLimitBonuses().entrySet()) {
                groupBonuses.put(
                    entry.getKey(),
                    Integer.valueOf(groupBonuses.getOrDefault(entry.getKey(), Integer.valueOf(0)).intValue() + entry.getValue().intValue())
                );
            }
        }
        return new EquipmentStats(carryCapacity, stamina, groupBonuses);
    }

    public GroupLimitState resolveGroupLimitState(WeightPlayerView player) {
        InventoryGroupRules rules = config.inventoryGroupRules();
        if (player == null || rules == null || rules.isEmpty()) {
            return GroupLimitState.empty();
        }

        EquipmentStats equipmentStats = resolveEquipmentStats(player);
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Integer> limits = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, String> labels = new LinkedHashMap<String, String>();

        for (InventoryGroupRules.GroupDefinition definition : rules.definitions()) {
            labels.put(definition.id(), definition.label());
            limits.put(
                definition.id(),
                Integer.valueOf(definition.limit() + equipmentStats.groupLimitBonus(definition.id()))
            );
            counts.put(definition.id(), Integer.valueOf(0));
        }

        for (WeightStackView stack : player.inventory()) {
            if (stack == null || stack.count() <= 0) {
                continue;
            }
            for (InventoryGroupRules.GroupDefinition definition : rules.resolve(stack)) {
                counts.put(
                    definition.id(),
                    Integer.valueOf(counts.getOrDefault(definition.id(), Integer.valueOf(0)).intValue() + stack.count())
                );
            }
        }

        return new GroupLimitState(counts, limits, labels);
    }

    public GroupLimitViolation findAddedStackViolation(WeightPlayerView player, WeightStackView addedStack) {
        return findDeltaViolation(player, null, addedStack);
    }

    public GroupLimitViolation findDeltaViolation(
        WeightPlayerView player,
        WeightStackView removedStack,
        WeightStackView addedStack
    ) {
        if ((removedStack == null || removedStack.count() <= 0) && (addedStack == null || addedStack.count() <= 0)) {
            return null;
        }

        GroupLimitState state = resolveGroupLimitState(player);
        if (state.isEmpty()) {
            return null;
        }

        LinkedHashMap<String, Integer> deltas = new LinkedHashMap<String, Integer>();
        applyDelta(deltas, removedStack, -1);
        applyDelta(deltas, addedStack, 1);
        for (Map.Entry<String, Integer> entry : deltas.entrySet()) {
            String groupId = entry.getKey();
            int nextCount = state.count(groupId) + entry.getValue().intValue();
            int limit = state.limit(groupId);
            if (nextCount > limit) {
                return new GroupLimitViolation(groupId, state.label(groupId), state.count(groupId), nextCount, limit);
            }
        }
        return null;
    }

    private void applyDelta(Map<String, Integer> deltas, WeightStackView stack, int direction) {
        if (stack == null || stack.count() <= 0) {
            return;
        }
        for (InventoryGroupRules.GroupDefinition definition : config.inventoryGroupRules().resolve(stack)) {
            deltas.put(
                definition.id(),
                Integer.valueOf(deltas.getOrDefault(definition.id(), Integer.valueOf(0)).intValue() + (stack.count() * direction))
            );
        }
    }

    public GroupLimitViolation findWorsenedViolation(GroupLimitState before, GroupLimitState after) {
        if (after == null || after.isEmpty()) {
            return null;
        }

        for (String groupId : after.groupIds()) {
            int afterExcess = Math.max(0, after.count(groupId) - after.limit(groupId));
            int beforeExcess = before == null ? 0 : Math.max(0, before.count(groupId) - before.limit(groupId));
            if (afterExcess > beforeExcess) {
                return new GroupLimitViolation(groupId, after.label(groupId), after.count(groupId), after.count(groupId), after.limit(groupId));
            }
        }
        return null;
    }

    public List<GroupLimitDescription> describeStackGroups(WeightStackView stack) {
        return describeStackGroups(null, stack);
    }

    public List<GroupLimitDescription> describeStackGroups(WeightPlayerView player, WeightStackView stack) {
        if (stack == null || config.inventoryGroupRules().isEmpty()) {
            return Collections.emptyList();
        }

        EquipmentStats equipmentStats = player == null ? EquipmentStats.empty() : resolveEquipmentStats(player);
        List<InventoryGroupRules.GroupDefinition> groups = config.inventoryGroupRules().resolve(stack);
        if (groups.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<GroupLimitDescription> descriptions = new ArrayList<GroupLimitDescription>(groups.size());
        for (InventoryGroupRules.GroupDefinition group : groups) {
            descriptions.add(
                new GroupLimitDescription(
                    group.id(),
                    group.label(),
                    group.limit() + equipmentStats.groupLimitBonus(group.id())
                )
            );
        }
        return descriptions;
    }

    public EquipmentBonusRules.EquipmentBonus equipmentBonus(WeightStackView stack) {
        return config.equipmentBonusRules().resolve(stack);
    }

    public static final class EquipmentStats {
        private final double carryCapacityKgBonus;
        private final double staminaBonus;
        private final Map<String, Integer> groupLimitBonuses;

        public EquipmentStats(double carryCapacityKgBonus, double staminaBonus, Map<String, Integer> groupLimitBonuses) {
            this.carryCapacityKgBonus = carryCapacityKgBonus;
            this.staminaBonus = staminaBonus;
            this.groupLimitBonuses = groupLimitBonuses == null
                ? Collections.<String, Integer>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<String, Integer>(groupLimitBonuses));
        }

        public static EquipmentStats empty() {
            return new EquipmentStats(0.0D, 0.0D, Collections.<String, Integer>emptyMap());
        }

        public double carryCapacityKgBonus() {
            return carryCapacityKgBonus;
        }

        public double staminaBonus() {
            return staminaBonus;
        }

        public int groupLimitBonus(String groupId) {
            Integer value = groupLimitBonuses.get(groupId);
            return value == null ? 0 : value.intValue();
        }
    }

    public static final class GroupLimitState {
        private final Map<String, Integer> counts;
        private final Map<String, Integer> limits;
        private final Map<String, String> labels;

        public GroupLimitState(Map<String, Integer> counts, Map<String, Integer> limits, Map<String, String> labels) {
            this.counts = counts == null ? Collections.<String, Integer>emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<String, Integer>(counts));
            this.limits = limits == null ? Collections.<String, Integer>emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<String, Integer>(limits));
            this.labels = labels == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<String, String>(labels));
        }

        public static GroupLimitState empty() {
            return new GroupLimitState(null, null, null);
        }

        public boolean isEmpty() {
            return limits.isEmpty();
        }

        public int count(String groupId) {
            Integer value = counts.get(groupId);
            return value == null ? 0 : value.intValue();
        }

        public int limit(String groupId) {
            Integer value = limits.get(groupId);
            return value == null ? 0 : value.intValue();
        }

        public String label(String groupId) {
            String value = labels.get(groupId);
            return value == null ? groupId : value;
        }

        public Iterable<String> groupIds() {
            return limits.keySet();
        }
    }

    public static final class GroupLimitViolation {
        private final String groupId;
        private final String label;
        private final int currentCount;
        private final int nextCount;
        private final int limit;

        public GroupLimitViolation(String groupId, String label, int currentCount, int nextCount, int limit) {
            this.groupId = groupId;
            this.label = label;
            this.currentCount = currentCount;
            this.nextCount = nextCount;
            this.limit = limit;
        }

        public String groupId() {
            return groupId;
        }

        public String label() {
            return label;
        }

        public int currentCount() {
            return currentCount;
        }

        public int nextCount() {
            return nextCount;
        }

        public int limit() {
            return limit;
        }
    }

    public static final class GroupLimitDescription {
        private final String groupId;
        private final String label;
        private final int limit;

        public GroupLimitDescription(String groupId, String label, int limit) {
            this.groupId = groupId;
            this.label = label;
            this.limit = limit;
        }

        public String groupId() {
            return groupId;
        }

        public String label() {
            return label;
        }

        public int limit() {
            return limit;
        }
    }
}

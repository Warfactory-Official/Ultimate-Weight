package com.warfactory.ultimateweight.config;

import com.warfactory.ultimateweight.api.WeightItemView;
import com.warfactory.ultimateweight.api.WeightStackView;

import java.util.*;

public final class InventoryGroupRules {
    private final Map<String, GroupDefinition> definitions;
    private final Map<String, List<String>> exactMatches;
    private final Map<String, List<String>> wildcardMatches;
    private final Map<String, List<String>> dictionaryMatches;

    private InventoryGroupRules(
        Map<String, GroupDefinition> definitions,
        Map<String, List<String>> exactMatches,
        Map<String, List<String>> wildcardMatches,
        Map<String, List<String>> dictionaryMatches
    ) {
        this.definitions = immutableDefinitions(definitions);
        this.exactMatches = immutableMatches(exactMatches);
        this.wildcardMatches = immutableMatches(wildcardMatches);
        this.dictionaryMatches = immutableMatches(dictionaryMatches);
    }

    public static InventoryGroupRules empty() {
        return new Builder().build();
    }

    public boolean isEmpty() {
        return definitions.isEmpty();
    }

    public Collection<GroupDefinition> definitions() {
        return definitions.values();
    }

    public Map<String, List<String>> exactMatches() {
        return exactMatches;
    }

    public Map<String, List<String>> wildcardMatches() {
        return wildcardMatches;
    }

    public Map<String, List<String>> dictionaryMatches() {
        return dictionaryMatches;
    }

    public GroupDefinition definition(String groupId) {
        return definitions.get(groupId);
    }

    public List<GroupDefinition> resolve(WeightStackView stack) {
        if (stack == null || stack.item() == null || definitions.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> groupIds = new LinkedHashSet<String>();
        WeightItemView item = stack.item();
        addMatches(groupIds, exactMatches.get(WeightResolverRules.exactKey(item.itemId(), stack.metadata())));
        addMatches(groupIds, wildcardMatches.get(WeightResolverRules.wildcardKey(item.itemId())));
        for (String key : safeMatchKeys(item.matchKeys())) {
            addMatches(groupIds, dictionaryMatches.get(WeightResolverRules.matchKey(key)));
        }
        if (groupIds.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<GroupDefinition> resolved = new ArrayList<GroupDefinition>(groupIds.size());
        for (String groupId : groupIds) {
            GroupDefinition definition = definitions.get(groupId);
            if (definition != null) {
                resolved.add(definition);
            }
        }
        return resolved;
    }

    private static void addMatches(LinkedHashSet<String> target, List<String> matches) {
        if (matches != null) {
            target.addAll(matches);
        }
    }

    private static Collection<String> safeMatchKeys(Collection<String> keys) {
        return keys == null ? Collections.<String>emptyList() : keys;
    }

    private static Map<String, GroupDefinition> immutableDefinitions(Map<String, GroupDefinition> source) {
        LinkedHashMap<String, GroupDefinition> copy = new LinkedHashMap<String, GroupDefinition>();
        if (source != null) {
            for (Map.Entry<String, GroupDefinition> entry : source.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    copy.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return Collections.unmodifiableMap(copy);
    }

    private static Map<String, List<String>> immutableMatches(Map<String, List<String>> source) {
        LinkedHashMap<String, List<String>> copy = new LinkedHashMap<String, List<String>>();
        if (source != null) {
            for (Map.Entry<String, List<String>> entry : source.entrySet()) {
                ArrayList<String> groups = new ArrayList<String>();
                if (entry.getValue() != null) {
                    for (String group : entry.getValue()) {
                        if (group != null) {
                            groups.add(group);
                        }
                    }
                }
                copy.put(entry.getKey(), Collections.unmodifiableList(groups));
            }
        }
        return Collections.unmodifiableMap(copy);
    }

    public static final class GroupDefinition {
        private final String id;
        private final String label;
        private final int limit;

        public GroupDefinition(String id, String label, int limit) {
            this.id = id == null ? "" : id.trim();
            this.label = label == null || label.trim().isEmpty() ? this.id : label.trim();
            this.limit = Math.max(0, limit);
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public int limit() {
            return limit;
        }
    }

    public static final class Builder {
        private final Map<String, GroupDefinition> definitions = new LinkedHashMap<String, GroupDefinition>();
        private final Map<String, List<String>> exactMatches = new LinkedHashMap<String, List<String>>();
        private final Map<String, List<String>> wildcardMatches = new LinkedHashMap<String, List<String>>();
        private final Map<String, List<String>> dictionaryMatches = new LinkedHashMap<String, List<String>>();

        public Builder define(String id, String label, int limit) {
            if (id == null || id.trim().isEmpty()) {
                return this;
            }
            definitions.put(id.trim(), new GroupDefinition(id, label, limit));
            return this;
        }

        public Builder addExact(String groupId, String itemId, int metadata) {
            return add(exactMatches, WeightResolverRules.exactKey(itemId, metadata), groupId);
        }

        public Builder addWildcard(String groupId, String itemId) {
            return add(wildcardMatches, WeightResolverRules.wildcardKey(itemId), groupId);
        }

        public Builder addMatch(String groupId, String key) {
            return add(dictionaryMatches, WeightResolverRules.matchKey(key), groupId);
        }

        public InventoryGroupRules build() {
            return new InventoryGroupRules(definitions, exactMatches, wildcardMatches, dictionaryMatches);
        }

        private Builder add(Map<String, List<String>> target, String key, String groupId) {
            if (key == null || key.isEmpty() || groupId == null || groupId.trim().isEmpty()) {
                return this;
            }
            List<String> groups = target.get(key);
            if (groups == null) {
                groups = new ArrayList<String>();
                target.put(key, groups);
            }
            if (!groups.contains(groupId.trim())) {
                groups.add(groupId.trim());
            }
            return this;
        }
    }
}

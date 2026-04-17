package com.warfactory.ultimateweight.config;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public final class WeightResolverRules {
    private final Object2DoubleOpenHashMap<String> exactWeights;
    private final Object2DoubleOpenHashMap<String> wildcardWeights;
    private final Object2DoubleOpenHashMap<String> matchWeights;

    private WeightResolverRules(
        Object2DoubleOpenHashMap<String> exactWeights,
        Object2DoubleOpenHashMap<String> wildcardWeights,
        Object2DoubleOpenHashMap<String> matchWeights
    ) {
        this.exactWeights = copy(exactWeights);
        this.wildcardWeights = copy(wildcardWeights);
        this.matchWeights = copy(matchWeights);
    }

    public static WeightResolverRules empty() {
        return new Builder().build();
    }

    public Object2DoubleOpenHashMap<String> exactWeights() {
        return copy(exactWeights);
    }

    public Object2DoubleOpenHashMap<String> wildcardWeights() {
        return copy(wildcardWeights);
    }

    public Object2DoubleOpenHashMap<String> matchWeights() {
        return copy(matchWeights);
    }

    public int exactCount() {
        return exactWeights.size();
    }

    public int wildcardCount() {
        return wildcardWeights.size();
    }

    public int matchCount() {
        return matchWeights.size();
    }

    public static String exactKey(String itemId, int metadata) {
        return normalize(itemId) + "@" + metadata;
    }

    public static String wildcardKey(String itemId) {
        return normalize(itemId);
    }

    public static String matchKey(String key) {
        return normalize(key);
    }

    private static Object2DoubleOpenHashMap<String> copy(Object2DoubleOpenHashMap<String> source) {
        Object2DoubleOpenHashMap<String> result = new Object2DoubleOpenHashMap<String>();
        result.defaultReturnValue(Double.NaN);
        if (source != null) {
            result.putAll(source);
        }
        return result;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public static final class Builder {
        private final Object2DoubleOpenHashMap<String> exactWeights = new Object2DoubleOpenHashMap<String>();
        private final Object2DoubleOpenHashMap<String> wildcardWeights = new Object2DoubleOpenHashMap<String>();
        private final Object2DoubleOpenHashMap<String> matchWeights = new Object2DoubleOpenHashMap<String>();

        public Builder() {
            exactWeights.defaultReturnValue(Double.NaN);
            wildcardWeights.defaultReturnValue(Double.NaN);
            matchWeights.defaultReturnValue(Double.NaN);
        }

        public Builder putExact(String itemId, int metadata, double weightKg) {
            exactWeights.put(exactKey(itemId, metadata), weightKg);
            return this;
        }

        public Builder putExactKey(String key, double weightKg) {
            exactWeights.put(normalize(key), weightKg);
            return this;
        }

        public Builder putWildcard(String itemId, double weightKg) {
            wildcardWeights.put(wildcardKey(itemId), weightKg);
            return this;
        }

        public Builder putWildcardKey(String key, double weightKg) {
            wildcardWeights.put(normalize(key), weightKg);
            return this;
        }

        public Builder putMatch(String key, double weightKg) {
            matchWeights.put(matchKey(key), weightKg);
            return this;
        }

        public Builder putMatchKey(String key, double weightKg) {
            matchWeights.put(normalize(key), weightKg);
            return this;
        }

        public WeightResolverRules build() {
            return new WeightResolverRules(exactWeights, wildcardWeights, matchWeights);
        }
    }
}

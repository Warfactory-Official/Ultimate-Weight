package com.warfactory.ultimateweight.core;

import com.warfactory.ultimateweight.api.WeightDataView;
import com.warfactory.ultimateweight.api.WeightItemView;
import com.warfactory.ultimateweight.api.WeightStackView;
import com.warfactory.ultimateweight.config.WeightConfig;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class WeightResolver {
    private final String overrideKey;
    private final Object2DoubleOpenHashMap<String> exactWeights;
    private final Object2DoubleOpenHashMap<String> groupWeights;
    private final Object2DoubleOpenHashMap<String> prefixWeights;
    private final List<String> orderedPrefixes;

    public WeightResolver(WeightConfig config) {
        this.overrideKey = config.componentOverrideKey();
        this.exactWeights = toDoubleMap(config.exactWeightsKg(), false);
        this.groupWeights = toDoubleMap(config.groupWeightsKg(), false);
        this.prefixWeights = toPrefixMap(config.prefixWeightsKg());
        this.orderedPrefixes = orderedPrefixes(prefixWeights.keySet());
    }

    public ResolvedWeight resolve(WeightStackView stack) {
        WeightDataView data = stack.data();
        if (data != null) {
            Double override = data.getDouble(overrideKey);
            if (override != null) {
                return new ResolvedWeight(
                    override.doubleValue(),
                    stack.count(),
                    ResolvedWeight.Source.COMPONENT_OVERRIDE,
                    overrideKey
                );
            }
        }

        WeightItemView item = stack.item();
        String exactKey = normalizeKey(item.itemId(), false);
        if (exactWeights.containsKey(exactKey)) {
            return new ResolvedWeight(
                exactWeights.getDouble(exactKey),
                stack.count(),
                ResolvedWeight.Source.EXACT_ITEM,
                exactKey
            );
        }

        for (String key : safeMatchKeys(item.matchKeys())) {
            String normalized = normalizeKey(key, false);
            if (groupWeights.containsKey(normalized)) {
                return new ResolvedWeight(
                    groupWeights.getDouble(normalized),
                    stack.count(),
                    ResolvedWeight.Source.GROUP_MATCH,
                    normalized
                );
            }
        }

        String prefixMatch = findPrefix(item);
        if (prefixMatch != null) {
            return new ResolvedWeight(
                prefixWeights.getDouble(prefixMatch),
                stack.count(),
                ResolvedWeight.Source.PREFIX_MATCH,
                prefixMatch
            );
        }

        return new ResolvedWeight(0.0D, stack.count(), ResolvedWeight.Source.DEFAULT, null);
    }

    private String findPrefix(WeightItemView item) {
        for (String key : safeMatchKeys(item.matchKeys())) {
            String matched = matchPrefix(key);
            if (matched != null) {
                return matched;
            }
        }
        return matchPrefix(item.itemPath());
    }

    private String matchPrefix(String rawCandidate) {
        String candidate = normalizeKey(rawCandidate, true);
        if (candidate.isEmpty()) {
            return null;
        }
        for (String prefix : orderedPrefixes) {
            if (candidate.startsWith(prefix)) {
                return prefix;
            }
        }
        return null;
    }

    private static Object2DoubleOpenHashMap<String> toDoubleMap(
        Map<String, Double> source,
        boolean stripNamespace
    ) {
        Object2DoubleOpenHashMap<String> result = new Object2DoubleOpenHashMap<String>();
        result.defaultReturnValue(Double.NaN);
        for (Map.Entry<String, Double> entry : source.entrySet()) {
            result.put(normalizeKey(entry.getKey(), stripNamespace), entry.getValue().doubleValue());
        }
        return result;
    }

    private static Object2DoubleOpenHashMap<String> toPrefixMap(Map<String, Double> source) {
        Object2DoubleOpenHashMap<String> result = toDoubleMap(source, true);
        double ingotWeight = result.getDouble("ingot");
        if (!Double.isNaN(ingotWeight)) {
            if (!result.containsKey("block")) {
                result.put("block", ingotWeight * 9.0D);
            }
            if (!result.containsKey("nugget")) {
                result.put("nugget", ingotWeight / 9.0D);
            }
        }
        return result;
    }

    private static List<String> orderedPrefixes(Collection<String> prefixes) {
        ArrayList<String> ordered = new ArrayList<String>(prefixes);
        Collections.sort(ordered, new Comparator<String>() {
            @Override
            public int compare(String left, String right) {
                return Integer.compare(right.length(), left.length());
            }
        });
        return ordered;
    }

    private static Collection<String> safeMatchKeys(Collection<String> keys) {
        return keys == null ? Collections.<String>emptyList() : keys;
    }

    private static String normalizeKey(String value, boolean stripNamespace) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().toLowerCase();
        if (stripNamespace) {
            int separator = normalized.indexOf(':');
            if (separator >= 0 && separator + 1 < normalized.length()) {
                normalized = normalized.substring(separator + 1);
            }
        }

        StringBuilder builder = new StringBuilder(normalized.length());
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            if (stripNamespace) {
                if ((character >= 'a' && character <= 'z') || (character >= '0' && character <= '9')) {
                    builder.append(character);
                }
            } else {
                if (character != ' ') {
                    builder.append(character);
                }
            }
        }
        return builder.toString();
    }
}

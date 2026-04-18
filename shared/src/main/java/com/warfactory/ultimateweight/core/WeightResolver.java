package com.warfactory.ultimateweight.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.api.WeightCompatRegistry;
import com.warfactory.ultimateweight.api.WeightDataView;
import com.warfactory.ultimateweight.api.WeightItemView;
import com.warfactory.ultimateweight.api.WeightStackView;
import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.config.WeightResolverRules;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.OptionalDouble;

public final class WeightResolver {
    private static final String STATIC_OVERRIDE_KEY = "uWeight";
    private static final double COMPAT_MISS = Double.NaN;

    private final Object2DoubleOpenHashMap<String> exactWeights;
    private final Object2DoubleOpenHashMap<String> wildcardWeights;
    private final Object2DoubleOpenHashMap<String> matchWeights;
    private final Cache<String, Double> complexCache;

    public WeightResolver(WeightConfig config) {
        WeightResolverRules rules = config == null ? WeightResolverRules.empty() : config.resolverRules();
        this.exactWeights = rules.exactWeights();
        this.wildcardWeights = rules.wildcardWeights();
        this.matchWeights = rules.matchWeights();
        this.complexCache = Caffeine.newBuilder().maximumSize(1024L).build();
    }

    public ResolvedWeight resolve(WeightStackView stack) {
        Integer previousDepth = WeightResolutionContext.setDepth(stack == null ? 0 : stack.resolutionDepth());
        try {
            if (stack == null) {
                return defaultWeight(0);
            }

            Double override = staticOverride(stack.data());
            if (override != null) {
                return new ResolvedWeight(
                    override.doubleValue(),
                    stack.count(),
                    ResolvedWeight.Source.STATIC_NBT_BYPASS,
                    STATIC_OVERRIDE_KEY
                );
            }

            String complexKey = stack.complexCacheKey();
            if (complexKey != null && !complexKey.isEmpty()) {
                Double cached = complexCache.getIfPresent(complexKey);
                if (cached != null) {
                    if (!Double.isNaN(cached.doubleValue())) {
                        return new ResolvedWeight(
                            cached.doubleValue(),
                            stack.count(),
                            ResolvedWeight.Source.COMPLEX_NBT_CACHE,
                            complexKey
                        );
                    }
                    return resolveConfigured(stack);
                }
            }

            Object nativeStack = stack.unwrap();
            if (nativeStack != null) {
                for (IWeightCompatProvider provider : WeightCompatRegistry.providers()) {
                    OptionalDouble resolved = resolveCompat(provider, nativeStack);
                    if (resolved.isPresent()) {
                        double weight = resolved.getAsDouble();
                        if (complexKey != null && !complexKey.isEmpty()) {
                            complexCache.put(complexKey, Double.valueOf(weight));
                        }
                        return new ResolvedWeight(
                            weight,
                            stack.count(),
                            ResolvedWeight.Source.COMPAT_API,
                            provider.getClass().getName()
                        );
                    }
                }
            }

            if (complexKey != null && !complexKey.isEmpty()) {
                complexCache.put(complexKey, Double.valueOf(COMPAT_MISS));
            }
            return resolveConfigured(stack);
        } finally {
            WeightResolutionContext.restoreDepth(previousDepth);
        }
    }

    public ResolvedWeight resolveConfigured(WeightStackView stack) {
        if (stack == null) {
            return defaultWeight(0);
        }

        WeightItemView item = stack.item();
        String exactKey = WeightResolverRules.exactKey(item.itemId(), stack.metadata());
        double exactWeight = exactWeights.getDouble(exactKey);
        if (!Double.isNaN(exactWeight)) {
            return new ResolvedWeight(
                exactWeight,
                stack.count(),
                ResolvedWeight.Source.EXACT_ITEM,
                exactKey
            );
        }

        String wildcardKey = WeightResolverRules.wildcardKey(item.itemId());
        double wildcardWeight = wildcardWeights.getDouble(wildcardKey);
        if (!Double.isNaN(wildcardWeight)) {
            return new ResolvedWeight(
                wildcardWeight,
                stack.count(),
                ResolvedWeight.Source.WILDCARD_ITEM,
                wildcardKey
            );
        }

        for (String key : safeMatchKeys(item.matchKeys())) {
            String matchKey = WeightResolverRules.matchKey(key);
            double matchWeight = matchWeights.getDouble(matchKey);
            if (!Double.isNaN(matchWeight)) {
                return new ResolvedWeight(
                    matchWeight,
                    stack.count(),
                    ResolvedWeight.Source.DICTIONARY_MATCH,
                    matchKey
                );
            }
        }

        return defaultWeight(stack.count());
    }

    private static OptionalDouble resolveCompat(IWeightCompatProvider provider, Object stack) {
        try {
            return provider == null ? OptionalDouble.empty() : provider.getUnitWeight(stack);
        } catch (Throwable ignored) {
            return OptionalDouble.empty();
        }
    }

    private static Double staticOverride(WeightDataView data) {
        if (data == null) {
            return null;
        }
        return data.getDouble(STATIC_OVERRIDE_KEY);
    }

    private static Collection<String> safeMatchKeys(Collection<String> keys) {
        return keys == null ? Collections.<String>emptyList() : keys;
    }

    private static ResolvedWeight defaultWeight(int count) {
        return new ResolvedWeight(0.0D, count, ResolvedWeight.Source.DEFAULT, null);
    }
}

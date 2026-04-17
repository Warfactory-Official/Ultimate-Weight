package com.warfactory.ultimateweight;

import com.hbm.weight.api.IWeightCompatProvider;
import com.hbm.weight.api.WeightCompatRegistry;
import com.warfactory.ultimateweight.api.WeightDataView;
import com.warfactory.ultimateweight.api.WeightItemView;
import com.warfactory.ultimateweight.api.WeightStackView;
import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.config.WeightResolverRules;
import com.warfactory.ultimateweight.core.ResolvedWeight;
import com.warfactory.ultimateweight.core.WeightResolver;
import java.util.Collection;
import java.util.Collections;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WeightResolverTest {
    @Test
    void staticOverrideBeatsCompatAndConfiguredRules() {
        WeightCompatRegistry.clear();
        AtomicInteger calls = new AtomicInteger();
        WeightCompatRegistry.register(new CountingProvider(calls, 7.0D));

        WeightResolver resolver = new WeightResolver(configWithRules(new WeightResolverRules.Builder()
            .putExact("minecraft:iron_ingot", 0, 2.0D)
            .putWildcard("minecraft:iron_ingot", 1.5D)
            .putMatch("ingotIron", 0.9D)
            .build()));

        ResolvedWeight resolved = resolver.resolve(
            new TestStack(
                new TestItem("minecraft:iron_ingot", Collections.singleton("ingotIron")),
                3,
                0,
                "complex",
                new Object(),
                new TestData(3.5D)
            )
        );

        Assertions.assertEquals(ResolvedWeight.Source.STATIC_NBT_BYPASS, resolved.source());
        Assertions.assertEquals(3.5D, resolved.singleItemWeightKg());
        Assertions.assertEquals(10.5D, resolved.stackWeightKg());
        Assertions.assertEquals(0, calls.get());
    }

    @Test
    void compatResultsAreCached() {
        WeightCompatRegistry.clear();
        AtomicInteger calls = new AtomicInteger();
        WeightCompatRegistry.register(new CountingProvider(calls, 6.25D));

        WeightResolver resolver = new WeightResolver(configWithRules(WeightResolverRules.empty()));
        TestStack stack = new TestStack(
            new TestItem("minecraft:backpack", Collections.<String>emptySet()),
            1,
            0,
            "backpack|0|0|123",
            new Object(),
            null
        );

        ResolvedWeight first = resolver.resolve(stack);
        ResolvedWeight second = resolver.resolve(stack);

        Assertions.assertEquals(ResolvedWeight.Source.COMPAT_API, first.source());
        Assertions.assertEquals(ResolvedWeight.Source.COMPLEX_NBT_CACHE, second.source());
        Assertions.assertEquals(6.25D, second.singleItemWeightKg());
        Assertions.assertEquals(1, calls.get());
    }

    @Test
    void configuredRulesResolveExactWildcardAndDictionaryInOrder() {
        WeightCompatRegistry.clear();

        WeightResolver resolver = new WeightResolver(configWithRules(new WeightResolverRules.Builder()
            .putExact("minecraft:iron_ingot", 2, 2.0D)
            .putWildcard("minecraft:iron_ingot", 1.5D)
            .putMatch("ingotIron", 0.9D)
            .build()));

        ResolvedWeight exactResolved = resolver.resolve(
            new TestStack(
                new TestItem("minecraft:iron_ingot", Collections.singleton("ingotIron")),
                2,
                2,
                "exact",
                new Object(),
                null
            )
        );
        Assertions.assertEquals(ResolvedWeight.Source.EXACT_ITEM, exactResolved.source());
        Assertions.assertEquals(4.0D, exactResolved.stackWeightKg());

        ResolvedWeight wildcardResolved = resolver.resolve(
            new TestStack(
                new TestItem("minecraft:iron_ingot", Collections.singleton("ingotIron")),
                2,
                5,
                "wildcard",
                new Object(),
                null
            )
        );
        Assertions.assertEquals(ResolvedWeight.Source.WILDCARD_ITEM, wildcardResolved.source());
        Assertions.assertEquals(3.0D, wildcardResolved.stackWeightKg());

        ResolvedWeight dictionaryResolved = resolver.resolve(
            new TestStack(
                new TestItem("minecraft:raw_iron", Collections.singleton("ingotIron")),
                2,
                0,
                "dictionary",
                new Object(),
                null
            )
        );
        Assertions.assertEquals(ResolvedWeight.Source.DICTIONARY_MATCH, dictionaryResolved.source());
        Assertions.assertEquals(1.8D, dictionaryResolved.stackWeightKg(), 0.000001D);
    }

    private static WeightConfig configWithRules(WeightResolverRules rules) {
        return new WeightConfig(
            WeightConfig.Precision.defaults(),
            true,
            600L,
            120.0D,
            220.0D,
            rules,
            WeightConfig.defaults().thresholds(),
            WeightConfig.FallDamage.defaults(),
            WeightConfig.Stamina.defaults()
        );
    }

    private static final class CountingProvider implements IWeightCompatProvider {
        private final AtomicInteger calls;
        private final double weight;

        private CountingProvider(AtomicInteger calls, double weight) {
            this.calls = calls;
            this.weight = weight;
        }

        @Override
        public OptionalDouble getUnitWeight(Object stack) {
            calls.incrementAndGet();
            return OptionalDouble.of(weight);
        }

        @Override
        public int getPriority() {
            return 100;
        }
    }

    private static final class TestItem implements WeightItemView {
        private final String itemId;
        private final Collection<String> keys;

        private TestItem(String itemId, Collection<String> keys) {
            this.itemId = itemId;
            this.keys = keys;
        }

        @Override
        public String itemId() {
            return itemId;
        }

        @Override
        public Collection<String> matchKeys() {
            return keys;
        }
    }

    private static final class TestStack implements WeightStackView {
        private final WeightItemView item;
        private final int count;
        private final int metadata;
        private final String cacheKey;
        private final Object rawStack;
        private final WeightDataView data;

        private TestStack(
            WeightItemView item,
            int count,
            int metadata,
            String cacheKey,
            Object rawStack,
            WeightDataView data
        ) {
            this.item = item;
            this.count = count;
            this.metadata = metadata;
            this.cacheKey = cacheKey;
            this.rawStack = rawStack;
            this.data = data;
        }

        @Override
        public WeightItemView item() {
            return item;
        }

        @Override
        public int count() {
            return count;
        }

        @Override
        public int metadata() {
            return metadata;
        }

        @Override
        public String complexCacheKey() {
            return cacheKey;
        }

        @Override
        public Object unwrap() {
            return rawStack;
        }

        @Override
        public WeightDataView data() {
            return data == null ? WeightDataView.empty() : data;
        }
    }

    private static final class TestData implements WeightDataView {
        private final double value;

        private TestData(double value) {
            this.value = value;
        }

        @Override
        public Double getDouble(String key) {
            return "uWeight".equals(key) ? Double.valueOf(value) : null;
        }
    }
}

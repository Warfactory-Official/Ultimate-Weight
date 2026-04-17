package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.api.WeightDataView;
import com.warfactory.ultimateweight.api.WeightItemView;
import com.warfactory.ultimateweight.api.WeightStackView;
import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.core.ResolvedWeight;
import com.warfactory.ultimateweight.core.WeightResolver;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WeightResolverTest {
    @Test
    void componentOverrideBeatsAllOtherRules() {
        LinkedHashMap<String, Double> exact = new LinkedHashMap<String, Double>();
        exact.put("minecraft:iron_ingot", 2.0D);

        LinkedHashMap<String, Double> groups = new LinkedHashMap<String, Double>();
        groups.put("ingotIron", 1.0D);

        LinkedHashMap<String, Double> prefixes = new LinkedHashMap<String, Double>();
        prefixes.put("ingot", 0.9D);

        WeightResolver resolver = new WeightResolver(
            new WeightConfig(
                WeightConfig.Precision.defaults(),
                true,
                600L,
                120.0D,
                220.0D,
                "uWeight",
                exact,
                groups,
                prefixes,
                WeightConfig.defaults().thresholds()
            )
        );

        ResolvedWeight resolved = resolver.resolve(
            new TestStack(
                new TestItem("minecraft:iron_ingot", Collections.singleton("ingotIron")),
                3,
                new TestData(3.5D)
            )
        );

        Assertions.assertEquals(ResolvedWeight.Source.COMPONENT_OVERRIDE, resolved.source());
        Assertions.assertEquals(3.5D, resolved.singleItemWeightKg());
        Assertions.assertEquals(10.5D, resolved.stackWeightKg());
    }

    @Test
    void groupAndPrefixFallbackResolveInOrder() {
        WeightResolver resolver = new WeightResolver(WeightConfig.defaults());

        ResolvedWeight groupResolved = resolver.resolve(
            new TestStack(new TestItem("minecraft:raw_iron", Collections.singleton("ingotIron")), 2, null)
        );
        Assertions.assertEquals(ResolvedWeight.Source.GROUP_MATCH, groupResolved.source());
        Assertions.assertEquals(1.8D, groupResolved.stackWeightKg());

        ResolvedWeight prefixResolved = resolver.resolve(
            new TestStack(new TestItem("minecraft:gold_nugget", Collections.singleton("nuggetGold")), 9, null)
        );
        Assertions.assertEquals(ResolvedWeight.Source.PREFIX_MATCH, prefixResolved.source());
        Assertions.assertEquals(0.9D, prefixResolved.stackWeightKg(), 0.000001D);
        Assertions.assertEquals("nugget", prefixResolved.matchedKey());
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
        private final WeightDataView data;

        private TestStack(WeightItemView item, int count, WeightDataView data) {
            this.item = item;
            this.count = count;
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

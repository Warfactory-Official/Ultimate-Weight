package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.api.WeightItemView;
import com.warfactory.ultimateweight.api.WeightPlayerView;
import com.warfactory.ultimateweight.api.WeightStackView;
import com.warfactory.ultimateweight.config.EquipmentBonusRules;
import com.warfactory.ultimateweight.config.InventoryGroupRules;
import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.config.WeightResolverRules;
import com.warfactory.ultimateweight.core.PlayerWeightTracker;
import com.warfactory.ultimateweight.core.WeightInventoryCalculator;
import com.warfactory.ultimateweight.core.WeightResolver;
import com.warfactory.ultimateweight.core.WeightUpdate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PlayerWeightTrackerTest {
    @Test
    void trackerUsesDirtyFlagThresholdsAndHardLockChecks() {
        WeightResolverRules.Builder rules = new WeightResolverRules.Builder();
        rules.putExact("minecraft:stone", 0, 6.0D);

        WeightConfig config = new WeightConfig(
            WeightConfig.Precision.defaults(),
            true,
            600L,
            100.0D,
            125.0D,
            rules.build(),
            InventoryGroupRules.empty(),
            EquipmentBonusRules.empty(),
            Arrays.asList(
                new WeightConfig.ThresholdRule(0.50D, 0.9D, 0.95D),
                new WeightConfig.ThresholdRule(1.00D, 0.5D, 0.6D)
            ),
            WeightConfig.FallDamage.defaults(),
            WeightConfig.Stamina.defaults()
        );

        PlayerWeightTracker tracker = new PlayerWeightTracker(
            config,
            new WeightInventoryCalculator(new WeightResolver(config))
        );

        TestPlayer player = new TestPlayer(
            "player-one",
            100.0D,
            Arrays.<WeightStackView>asList(new TestStack("minecraft:stone", 10))
        );

        WeightUpdate first = tracker.refresh(player, 0L);
        Assertions.assertTrue(first.recalculated());
        Assertions.assertTrue(first.weightChanged());
        Assertions.assertTrue(first.thresholdChanged());
        Assertions.assertEquals(60.0D, first.snapshot().totalWeightKg());
        Assertions.assertEquals(0, first.snapshot().thresholdEffect().thresholdIndex());

        WeightUpdate second = tracker.refresh(player, 1L);
        Assertions.assertFalse(second.recalculated());

        tracker.markDirty(player.playerId());
        player.inventory = Arrays.<WeightStackView>asList(new TestStack("minecraft:stone", 20));
        WeightUpdate third = tracker.refresh(player, 2L);
        Assertions.assertTrue(third.recalculated());
        Assertions.assertEquals(120.0D, third.snapshot().totalWeightKg());
        Assertions.assertEquals(1, third.snapshot().thresholdEffect().thresholdIndex());
        Assertions.assertTrue(tracker.canAcceptAdditionalWeight(third.snapshot(), 1.0D));
        Assertions.assertFalse(tracker.canAcceptAdditionalWeight(third.snapshot(), 6.0D));

        WeightUpdate failsafe = tracker.refresh(player, 602L);
        Assertions.assertTrue(failsafe.recalculated());
    }

    private static final class TestPlayer implements WeightPlayerView {
        private final String playerId;
        private final double capacity;
        private Iterable<? extends WeightStackView> inventory;

        private TestPlayer(String playerId, double capacity, Iterable<? extends WeightStackView> inventory) {
            this.playerId = playerId;
            this.capacity = capacity;
            this.inventory = inventory;
        }

        @Override
        public String playerId() {
            return playerId;
        }

        @Override
        public Iterable<? extends WeightStackView> inventory() {
            return inventory;
        }

        @Override
        public double carryCapacityKg() {
            return capacity;
        }
    }

    private static final class TestStack implements WeightStackView {
        private final WeightItemView item;
        private final int count;

        private TestStack(String itemId, int count) {
            this.item = new TestItem(itemId);
            this.count = count;
        }

        @Override
        public WeightItemView item() {
            return item;
        }

        @Override
        public int count() {
            return count;
        }
    }

    private static final class TestItem implements WeightItemView {
        private final String itemId;

        private TestItem(String itemId) {
            this.itemId = itemId;
        }

        @Override
        public String itemId() {
            return itemId;
        }

        @Override
        public Collection<String> matchKeys() {
            return Collections.emptyList();
        }
    }
}

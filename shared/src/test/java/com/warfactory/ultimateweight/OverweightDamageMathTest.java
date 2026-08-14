package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.core.OverweightDamageMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OverweightDamageMathTest {
    private static final double DELTA = 1.0e-9D;

    private static WeightConfig.OverweightDamage enabled() {
        return new WeightConfig.OverweightDamage(true, 1.2D, 1.0D, 2.0D, 1.0D, 6.0D, 40L, 0.0D);
    }

    @Test
    void disabledSystemNeverDamages() {
        WeightConfig.OverweightDamage config = new WeightConfig.OverweightDamage(
            false, 1.2D, 1.0D, 2.0D, 1.0D, 6.0D, 40L, 0.0D
        );

        Assertions.assertFalse(OverweightDamageMath.isOverweight(config, 500.0D, 100.0D));
        Assertions.assertFalse(OverweightDamageMath.isDue(config, 1000L, 0L));
        Assertions.assertEquals(0.0D, OverweightDamageMath.resolveDamage(config, 500.0D, 100.0D, true), DELTA);
    }

    @Test
    void loadAtOrBelowStartThresholdDoesNotDamage() {
        WeightConfig.OverweightDamage config = enabled();

        Assertions.assertFalse(OverweightDamageMath.isOverweight(config, 120.0D, 100.0D));
        Assertions.assertEquals(0.0D, OverweightDamageMath.resolveDamage(config, 120.0D, 100.0D, false), DELTA);
        Assertions.assertTrue(OverweightDamageMath.isOverweight(config, 130.0D, 100.0D));
    }

    @Test
    void damageScalesWithLoadAndHardLock() {
        WeightConfig.OverweightDamage config = enabled();

        // 1.5 load -> base 1.0 + (0.3 * 2.0)
        Assertions.assertEquals(1.6D, OverweightDamageMath.resolveDamage(config, 150.0D, 100.0D, false), DELTA);
        // same load, hard-locked -> + 1.0 bonus
        Assertions.assertEquals(2.6D, OverweightDamageMath.resolveDamage(config, 150.0D, 100.0D, true), DELTA);
    }

    @Test
    void damageIsCappedAtMaxPerInterval() {
        WeightConfig.OverweightDamage config = enabled();

        Assertions.assertEquals(6.0D, OverweightDamageMath.resolveDamage(config, 1000.0D, 100.0D, true), DELTA);
    }

    @Test
    void zeroCarryCapacityIsTreatedAsNotOverweight() {
        WeightConfig.OverweightDamage config = enabled();

        Assertions.assertFalse(OverweightDamageMath.isOverweight(config, 500.0D, 0.0D));
        Assertions.assertEquals(0.0D, OverweightDamageMath.resolveDamage(config, 500.0D, 0.0D, false), DELTA);
    }

    @Test
    void damageIsDueOnlyOncePerInterval() {
        WeightConfig.OverweightDamage config = enabled();

        Assertions.assertFalse(OverweightDamageMath.isDue(config, 139L, 100L));
        Assertions.assertTrue(OverweightDamageMath.isDue(config, 140L, 100L));
    }

    @Test
    void nonPositiveIntervalFallsBackToEveryTick() {
        WeightConfig.OverweightDamage config = new WeightConfig.OverweightDamage(
            true, 1.2D, 1.0D, 2.0D, 1.0D, 6.0D, 0L, 0.0D
        );

        Assertions.assertEquals(1L, config.intervalTicks());
        Assertions.assertTrue(OverweightDamageMath.isDue(config, 101L, 100L));
    }

    @Test
    void minHealthFloorCapsAndThenBlocksDamage() {
        WeightConfig.OverweightDamage config = new WeightConfig.OverweightDamage(
            true, 1.2D, 4.0D, 2.0D, 1.0D, 6.0D, 40L, 2.0D
        );

        Assertions.assertEquals(3.0D, OverweightDamageMath.clampToMinHealth(config, 4.0D, 5.0D), DELTA);
        Assertions.assertEquals(0.0D, OverweightDamageMath.clampToMinHealth(config, 4.0D, 2.0D), DELTA);
    }

    @Test
    void zeroMinHealthLetsDamageKill() {
        WeightConfig.OverweightDamage config = enabled();

        Assertions.assertEquals(4.0D, OverweightDamageMath.clampToMinHealth(config, 4.0D, 1.0D), DELTA);
    }
}

package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.config.ScriptConfigOverrides;
import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.config.WeightResolverRules;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ScriptConfigOverridesTest {

    @Test
    void emptyOverlayReturnsBaseUnchanged() {
        WeightConfig base = WeightConfig.defaults();
        Assertions.assertSame(base, new ScriptConfigOverrides().applyTo(base));
    }

    @Test
    void overridesTakePrecedenceWhileUntouchedValuesFallThrough() {
        WeightConfig base = WeightConfig.defaults();

        ScriptConfigOverrides overrides = new ScriptConfigOverrides();
        overrides.setItemWeight("minecraft:stone", 2.0D);
        overrides.setHardLockWeight(180.0D);

        WeightConfig result = overrides.applyTo(base);

        Assertions.assertEquals(
            2.0D,
            result.resolverRules().exactWeights().getDouble(WeightResolverRules.exactKey("minecraft:stone", 0)),
            1.0e-9D
        );
        Assertions.assertEquals(180.0D, result.hardLockWeightKg(), 1.0e-9D);
        // Untouched: capacity falls through to the base value.
        Assertions.assertEquals(base.defaultCarryCapacityKg(), result.defaultCarryCapacityKg(), 1.0e-9D);
        // Base config is not mutated.
        Assertions.assertTrue(Double.isNaN(base.resolverRules().exactWeights().getDouble(WeightResolverRules.exactKey("minecraft:stone", 0))));
    }

    @Test
    void disableStaminaTurnsOffTheSystem() {
        WeightConfig base = WeightConfig.defaults();
        Assertions.assertTrue(base.stamina().enabled());

        WeightConfig result = new ScriptConfigOverrides().disableStamina().applyTo(base);

        Assertions.assertFalse(result.stamina().enabled());
        // Other stamina values are preserved.
        Assertions.assertEquals(base.stamina().totalStamina(), result.stamina().totalStamina(), 1.0e-9D);
        Assertions.assertEquals(base.stamina().sprintStaminaLossRate(), result.stamina().sprintStaminaLossRate(), 1.0e-9D);
    }

    @Test
    void changeListenerFiresOnEachMutation() {
        int[] count = {0};
        ScriptConfigOverrides overrides = new ScriptConfigOverrides();
        overrides.setChangeListener(() -> count[0]++);

        overrides.setItemWeight("minecraft:dirt", 1.0D);
        overrides.disableStamina();

        Assertions.assertEquals(2, count[0]);
    }
}

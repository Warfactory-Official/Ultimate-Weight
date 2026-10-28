package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.api.StaminaModifiers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StaminaModifiersTest {
    @AfterEach
    void reset() {
        StaminaModifiers.clear();
    }

    @Test
    void modifiersAddUpPerPlayerAndNoneMeansNoChange() {
        Object alice = new Object();
        Object bob = new Object();
        assertEquals(0.0D, StaminaModifiers.recoveryPerTick(alice));
        StaminaModifiers.register(player -> player == alice ? 0.004D : 0.0D);
        StaminaModifiers.register(player -> -0.001D);
        StaminaModifiers.register(null);
        assertEquals(0.003D, StaminaModifiers.recoveryPerTick(alice), 1e-12);
        assertEquals(-0.001D, StaminaModifiers.recoveryPerTick(bob), 1e-12);
    }
}

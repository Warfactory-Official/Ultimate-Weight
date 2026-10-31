package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.api.CarryModifiers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CarryModifiersTest {
    @AfterEach
    void reset() {
        CarryModifiers.clear();
    }

    @Test
    void modifiersAddUpPerPlayerAndNoneMeansNoChange() {
        Object alice = new Object();
        Object bob = new Object();
        assertEquals(0.0D, CarryModifiers.bonusKg(alice));
        CarryModifiers.register(player -> player == alice ? 14.0D : 0.0D);
        CarryModifiers.register(player -> -4.0D);
        CarryModifiers.register(null);
        assertEquals(10.0D, CarryModifiers.bonusKg(alice), 1e-12);
        assertEquals(-4.0D, CarryModifiers.bonusKg(bob), 1e-12);
    }
}

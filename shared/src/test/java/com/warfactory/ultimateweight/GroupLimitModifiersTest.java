package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.api.GroupLimitModifiers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupLimitModifiersTest {
    @AfterEach
    void reset() {
        GroupLimitModifiers.clear();
    }

    @Test
    void bonusesSumPerPlayerAndGroup() {
        Object alice = new Object();
        assertEquals(0, GroupLimitModifiers.bonus(alice, "grenades"));
        GroupLimitModifiers.register((p, g) -> p == alice && g.equals("grenades") ? 4 : 0);
        GroupLimitModifiers.register((p, g) -> -1);
        GroupLimitModifiers.register(null);
        assertEquals(3, GroupLimitModifiers.bonus(alice, "grenades"));
        assertEquals(-1, GroupLimitModifiers.bonus(alice, "mines"));
        assertEquals(0, GroupLimitModifiers.bonus(null, "mines"));
    }
}

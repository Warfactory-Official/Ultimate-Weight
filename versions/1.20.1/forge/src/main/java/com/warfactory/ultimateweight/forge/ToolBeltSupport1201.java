package com.warfactory.ultimateweight.forge;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;


public final class ToolBeltSupport1201 {
    private static final String BELT_EXTENSION_SLOT_CLASS = "dev.gigaherz.toolbelt.slot.BeltExtensionSlot";
    private static final String EXTENSION_SLOT_CLASS = "dev.gigaherz.toolbelt.customslots.IExtensionSlot";

    private static volatile boolean resolved;
    private static Method beltSlotGet;
    private static Method beltSlotGetSlots;
    private static Method extensionSlotGetContents;

    private ToolBeltSupport1201() {
    }

    public static void collectWorn(Player player, List<ItemStack> out) {
        if (player == null || !resolve()) {
            return;
        }

        try {
            Object lazy = beltSlotGet.invoke(null, player);
            if (!(lazy instanceof LazyOptional<?> lazyOptional)) {
                return;
            }
            Optional<?> extensionSlot = lazyOptional.resolve();
            if (extensionSlot.isEmpty()) {
                return;
            }

            Object slots = beltSlotGetSlots.invoke(extensionSlot.get());
            if (!(slots instanceof Iterable<?> iterable)) {
                return;
            }

            for (Object slot : iterable) {
                if (slot == null) {
                    continue;
                }
                Object contents = extensionSlotGetContents.invoke(slot);
                if (!(contents instanceof ItemStack stack) || stack.isEmpty()) {
                    continue;
                }
                if (!containsSame(out, stack)) {
                    out.add(stack);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static boolean containsSame(List<ItemStack> out, ItemStack stack) {
        for (ItemStack existing : out) {
            if (existing == stack) {
                return true;
            }
        }
        return false;
    }

    private static boolean resolve() {
        if (resolved) {
            return beltSlotGet != null;
        }
        synchronized (ToolBeltSupport1201.class) {
            if (!resolved) {
                try {
                    Class<?> beltSlot = Class.forName(BELT_EXTENSION_SLOT_CLASS);
                    Class<?> extensionSlot = Class.forName(EXTENSION_SLOT_CLASS);
                    beltSlotGet = beltSlot.getMethod("get", LivingEntity.class);
                    beltSlotGetSlots = beltSlot.getMethod("getSlots");
                    extensionSlotGetContents = extensionSlot.getMethod("getContents");
                } catch (Throwable ignored) {
                    beltSlotGet = null;
                    beltSlotGetSlots = null;
                    extensionSlotGetContents = null;
                }
                resolved = true;
            }
        }
        return beltSlotGet != null;
    }
}

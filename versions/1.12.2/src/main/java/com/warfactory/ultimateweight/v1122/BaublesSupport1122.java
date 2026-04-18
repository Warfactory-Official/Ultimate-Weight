package com.warfactory.ultimateweight.v1122;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BaublesSupport1122 {
    private static final String MOD_ID = "baubles";
    private static volatile boolean initialized;
    private static volatile boolean available;
    private static Class<?> apiClass;
    private static Method getHandlerMethod;
    private static Method getSlotsMethod;
    private static Method getStackMethod;

    private BaublesSupport1122() {
    }

    public static List<ItemStack> equipped(EntityPlayer player) {
        if (player == null || !ensureAvailable()) {
            return Collections.emptyList();
        }

        try {
            Object handler = getHandlerMethod.invoke(null, player);
            if (handler == null) {
                return Collections.emptyList();
            }
            int slots = ((Number) getSlotsMethod.invoke(handler)).intValue();
            ArrayList<ItemStack> stacks = new ArrayList<ItemStack>(slots);
            for (int index = 0; index < slots; index++) {
                Object raw = getStackMethod.invoke(handler, Integer.valueOf(index));
                if (raw instanceof ItemStack) {
                    ItemStack stack = (ItemStack) raw;
                    if (!stack.isEmpty()) {
                        stacks.add(stack);
                    }
                }
            }
            return stacks;
        } catch (Throwable ignored) {
            return Collections.emptyList();
        }
    }

    private static boolean ensureAvailable() {
        if (initialized) {
            return available;
        }
        initialized = true;
        if (!Loader.isModLoaded(MOD_ID)) {
            available = false;
            return false;
        }
        try {
            apiClass = Class.forName("baubles.api.BaublesApi", false, BaublesSupport1122.class.getClassLoader());
            getHandlerMethod = apiClass.getMethod("getBaublesHandler", EntityPlayer.class);
            Class<?> handlerClass = Class.forName("baubles.api.cap.IBaublesItemHandler", false, BaublesSupport1122.class.getClassLoader());
            getSlotsMethod = handlerClass.getMethod("getSlots");
            getStackMethod = handlerClass.getMethod("getStackInSlot", int.class);
            available = true;
        } catch (Throwable ignored) {
            available = false;
        }
        return available;
    }
}

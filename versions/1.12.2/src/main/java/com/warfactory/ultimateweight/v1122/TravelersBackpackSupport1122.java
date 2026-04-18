package com.warfactory.ultimateweight.v1122;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TravelersBackpackSupport1122 {
    private static final String MOD_ID = "travelersbackpack";
    private static volatile boolean initialized;
    private static volatile boolean available;
    private static Method getWearingBackpackMethod;
    private static Method getBackpackInvMethod;
    private static Method getInventoryMethod;
    private static Method getCraftingGridInventoryMethod;

    private TravelersBackpackSupport1122() {
    }

    public static ItemStack equippedBackpack(EntityPlayer player) {
        if (player == null || !ensureAvailable()) {
            return ItemStack.EMPTY;
        }

        try {
            Object raw = getWearingBackpackMethod.invoke(null, player);
            return raw instanceof ItemStack ? (ItemStack) raw : ItemStack.EMPTY;
        } catch (Throwable ignored) {
            return ItemStack.EMPTY;
        }
    }

    public static List<ItemStack> contents(EntityPlayer player) {
        if (player == null || !ensureAvailable()) {
            return Collections.emptyList();
        }

        try {
            Object inventory = getBackpackInvMethod.invoke(null, player);
            if (inventory == null) {
                return Collections.emptyList();
            }

            ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
            addStacks(stacks, getInventoryMethod.invoke(inventory));
            addStacks(stacks, getCraftingGridInventoryMethod.invoke(inventory));
            return stacks;
        } catch (Throwable ignored) {
            return Collections.emptyList();
        }
    }

    private static void addStacks(List<ItemStack> target, Object rawInventory) {
        if (!(rawInventory instanceof Iterable<?>)) {
            return;
        }
        Iterable<?> iterable = (Iterable<?>) rawInventory;
        for (Object rawStack : iterable) {
            if (rawStack instanceof ItemStack) {
                ItemStack stack = (ItemStack) rawStack;
                if (!stack.isEmpty()) {
                target.add(stack);
                }
            }
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
            Class<?> capabilityUtilsClass = Class.forName(
                "com.tiviacz.travelersbackpack.capability.CapabilityUtils",
                false,
                TravelersBackpackSupport1122.class.getClassLoader()
            );
            Class<?> inventoryClass = Class.forName(
                "com.tiviacz.travelersbackpack.gui.inventory.InventoryTravelersBackpack",
                false,
                TravelersBackpackSupport1122.class.getClassLoader()
            );
            getWearingBackpackMethod = capabilityUtilsClass.getMethod("getWearingBackpack", EntityPlayer.class);
            getBackpackInvMethod = capabilityUtilsClass.getMethod("getBackpackInv", EntityPlayer.class);
            getInventoryMethod = inventoryClass.getMethod("getInventory");
            getCraftingGridInventoryMethod = inventoryClass.getMethod("getCraftingGridInventory");
            available = true;
        } catch (Throwable ignored) {
            available = false;
        }
        return available;
    }
}

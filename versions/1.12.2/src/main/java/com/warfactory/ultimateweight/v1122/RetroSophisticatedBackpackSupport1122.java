package com.warfactory.ultimateweight.v1122;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Method;

/**
 * Detection and access helper for Retro Sophisticated Backpacks items.
 *
 * <p>The detection check is a pure registry-name comparison and the upgrade-handler access is pure
 * reflection, so this class is safe to load and call even when the mod is absent (no Retro
 * Sophisticated Backpacks classes are referenced). All of that mod's backpack items are registered
 * as {@code retro_sophisticated_backpacks:backpack_*}.
 *
 * <p>Unlike a Traveler's Backpack, a Retro Sophisticated Backpack stores its contents in a forge
 * capability ({@code BackpackWrapper}) rather than in the item's regular NBT tag. That capability
 * is read live through the standard item-handler capability, but it means the regular tag hash
 * never changes when the contents change - which is why these stacks must bypass the weight cache
 * and force a full rescan on inventory deltas.
 */
public final class RetroSophisticatedBackpackSupport1122 {
    private static final String BACKPACK_ID_PREFIX = "retro_sophisticated_backpacks:backpack";
    private static final String UPGRADE_HANDLER_GETTER = "getUpgradeItemStackHandler";

    private static volatile boolean upgradeLookupResolved;
    private static Method upgradeHandlerMethod;

    private RetroSophisticatedBackpackSupport1122() {
    }

    public static boolean isBackpackStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = stack.getItem().getRegistryName();
        return id != null && id.toString().startsWith(BACKPACK_ID_PREFIX);
    }

    /**
     * The handler holding the backpack's installed upgrade items, read reflectively from the live
     * {@code BackpackWrapper} (which is itself the backpack's item-handler capability). The upgrade
     * slots are NOT exposed through {@code ITEM_HANDLER_CAPABILITY}, so they must be reached this
     * way to weigh the installed upgrades. Returns {@code null} if unavailable.
     */
    public static IItemHandler upgradeHandler(IItemHandler backpackWrapper) {
        if (backpackWrapper == null) {
            return null;
        }
        Method method = resolveUpgradeHandlerMethod(backpackWrapper.getClass());
        if (method == null) {
            return null;
        }
        try {
            Object result = method.invoke(backpackWrapper);
            return result instanceof IItemHandler ? (IItemHandler) result : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method resolveUpgradeHandlerMethod(Class<?> wrapperClass) {
        if (upgradeLookupResolved) {
            return upgradeHandlerMethod;
        }
        synchronized (RetroSophisticatedBackpackSupport1122.class) {
            if (!upgradeLookupResolved) {
                try {
                    upgradeHandlerMethod = wrapperClass.getMethod(UPGRADE_HANDLER_GETTER);
                } catch (Throwable ignored) {
                    upgradeHandlerMethod = null;
                }
                upgradeLookupResolved = true;
            }
        }
        return upgradeHandlerMethod;
    }
}

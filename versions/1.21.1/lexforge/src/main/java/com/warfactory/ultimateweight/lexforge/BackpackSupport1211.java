package com.warfactory.ultimateweight.lexforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * LexForge backpack integration, fully decoupled from the backpack mods: Sophisticated Backpacks is
 * detected by registry id and Traveler's Backpack + Curios are reached through reflection, so this
 * class loads and runs safely whether or not those mods are present (the LexForge build declares no
 * backpack deps, so these paths stay inert unless such a mod is added to a pack). Mirrors the
 * NeoForge support but uses the Forge {@link IItemHandler} type and Forge's reflective LazyOptional.
 */
public final class BackpackSupport1211 {
    private static final String SOPHISTICATED_NAMESPACE = "sophisticatedbackpacks";
    private static final String CURIOS_API_CLASS = "top.theillusivec4.curios.api.CuriosApi";
    private static final String TB_CAPABILITY_UTILS_CLASS = "com.tiviacz.travelersbackpack.capability.CapabilityUtils";
    private static final String TB_ITEM_CLASS = "com.tiviacz.travelersbackpack.items.TravelersBackpackItem";

    private static volatile boolean curiosResolved;
    private static Method curiosGetInventory;
    private static Method curiosGetEquipped;

    private static volatile boolean tbWornResolved;
    private static Method tbGetWearingBackpack;

    private static volatile boolean tbItemResolved;
    private static Class<?> tbItemClass;

    private BackpackSupport1211() {
    }

    public static boolean isBackpack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return isSophisticatedBackpack(stack) || isTravelersBackpack(stack);
    }

    private static boolean isSophisticatedBackpack(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null
            && SOPHISTICATED_NAMESPACE.equals(id.getNamespace())
            && id.getPath().endsWith("backpack");
    }

    private static boolean isTravelersBackpack(ItemStack stack) {
        Class<?> itemClass = travelersBackpackItemClass();
        return itemClass != null && itemClass.isInstance(stack.getItem());
    }

    /** Worn-item source: every equipped curio plus a natively-worn Traveler's Backpack. */
    public static void collectWorn(Player player, List<ItemStack> out) {
        if (player == null) {
            return;
        }
        collectCurios(player, out);
        collectTravelersBackpack(player, out);
    }

    private static void collectCurios(Player player, List<ItemStack> out) {
        Method getInventory = curiosGetInventory();
        if (getInventory == null) {
            return;
        }
        try {
            Object handlerHolder = unwrap(getInventory.invoke(null, player));
            if (handlerHolder == null) {
                return;
            }
            Method getEquipped = curiosGetEquipped(handlerHolder);
            if (getEquipped == null) {
                return;
            }
            Object equipped = getEquipped.invoke(handlerHolder);
            if (!(equipped instanceof IItemHandler itemHandler)) {
                return;
            }
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                ItemStack stack = itemHandler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    out.add(stack);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void collectTravelersBackpack(Player player, List<ItemStack> out) {
        Method getWorn = travelersGetWearingBackpack();
        if (getWorn == null) {
            return;
        }
        try {
            Object raw = getWorn.invoke(null, player);
            if (!(raw instanceof ItemStack worn) || worn.isEmpty()) {
                return;
            }
            for (ItemStack existing : out) {
                if (existing == worn) {
                    return;
                }
            }
            out.add(worn);
        } catch (Throwable ignored) {
        }
    }

    /** Unwrap a Forge LazyOptional or a plain Optional reflectively. */
    private static Object unwrap(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        try {
            Method resolve = value.getClass().getMethod("resolve");
            Object resolved = resolve.invoke(value);
            return resolved instanceof Optional<?> optional ? optional.orElse(null) : resolved;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method curiosGetInventory() {
        if (curiosResolved) {
            return curiosGetInventory;
        }
        synchronized (BackpackSupport1211.class) {
            if (!curiosResolved) {
                try {
                    Class<?> api = Class.forName(CURIOS_API_CLASS);
                    curiosGetInventory = api.getMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
                } catch (Throwable ignored) {
                    curiosGetInventory = null;
                }
                curiosResolved = true;
            }
        }
        return curiosGetInventory;
    }

    private static Method curiosGetEquipped(Object handler) {
        Method cached = curiosGetEquipped;
        if (cached != null) {
            return cached;
        }
        try {
            cached = handler.getClass().getMethod("getEquippedCurios");
            curiosGetEquipped = cached;
        } catch (Throwable ignored) {
            return null;
        }
        return cached;
    }

    private static Method travelersGetWearingBackpack() {
        if (tbWornResolved) {
            return tbGetWearingBackpack;
        }
        synchronized (BackpackSupport1211.class) {
            if (!tbWornResolved) {
                try {
                    Class<?> utils = Class.forName(TB_CAPABILITY_UTILS_CLASS);
                    tbGetWearingBackpack = utils.getMethod("getWearingBackpack", Player.class);
                } catch (Throwable ignored) {
                    tbGetWearingBackpack = null;
                }
                tbWornResolved = true;
            }
        }
        return tbGetWearingBackpack;
    }

    private static Class<?> travelersBackpackItemClass() {
        if (tbItemResolved) {
            return tbItemClass;
        }
        synchronized (BackpackSupport1211.class) {
            if (!tbItemResolved) {
                try {
                    tbItemClass = Class.forName(TB_ITEM_CLASS);
                } catch (Throwable ignored) {
                    tbItemClass = null;
                }
                tbItemResolved = true;
            }
        }
        return tbItemClass;
    }
}

package com.warfactory.ultimateweight.v1211.compat;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatPlugin;
import com.warfactory.ultimateweight.logging.WeightLoggers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.List;


@SuppressWarnings("unused")
@CompatPlugin(requiredMods = "superbwarfare")
public final class SuperbWarfareReserveAmmoPlugin1211 implements WeightCompatPlugin {

    @Override
    public void register(WeightCompatContext context) {
        if (!ReserveAmmo.AVAILABLE) {
            return;
        }
        ((CompatContext1211) context).registerInventorySource(ReserveAmmo::collect);
    }

    private static final class ReserveAmmo {
        static final boolean AVAILABLE;
        private static final WeightLoggers.WeightLogger LOGGER = WeightLoggers.component("compat");
        private static final Object[] AMMO_TYPES;
        private static final Method GET_COUNT;       // Ammo.get(Player) -> int
        private static final Method GET_ITEM_STACK;  // Ammo.getItemStack(int) -> ItemStack

        static {
            Object[] types = null;
            Method getCount = null;
            Method getItemStack = null;
            try {
                Class<?> ammo = Class.forName("com.atsuishio.superbwarfare.data.gun.Ammo");
                types = (Object[]) ammo.getMethod("values").invoke(null);
                getCount = ammo.getMethod("get", Player.class);
                getItemStack = ammo.getMethod("getItemStack", int.class);
            } catch (Throwable error) {
                LOGGER.debug("Superb Warfare reserve-ammo source disabled: {}", String.valueOf(error));
            }
            AMMO_TYPES = types;
            GET_COUNT = getCount;
            GET_ITEM_STACK = getItemStack;
            AVAILABLE = types != null && getCount != null && getItemStack != null;
        }

        private ReserveAmmo() {
        }

        static void collect(Player player, List<ItemStack> out) {
            if (!AVAILABLE || player == null) {
                return;
            }
            for (Object type : AMMO_TYPES) {
                try {
                    int count = (Integer) GET_COUNT.invoke(type, player);
                    if (count <= 0) {
                        continue;
                    }
                    ItemStack stack = (ItemStack) GET_ITEM_STACK.invoke(type, count);
                    if (stack != null && !stack.isEmpty()) {
                        out.add(stack);
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }
}

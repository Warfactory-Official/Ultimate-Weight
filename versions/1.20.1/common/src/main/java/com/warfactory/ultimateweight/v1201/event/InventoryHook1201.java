package com.warfactory.ultimateweight.v1201.event;

import com.warfactory.ultimateweight.logging.WeightLoggers;
import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class InventoryHook1201 {
    private static final WeightLoggers.WeightLogger LOGGER = WeightLoggers.component("inventory_hook");

    private InventoryHook1201() {
    }

    public static void onClientSlotChange(Inventory inventory, int slotIndex, ItemStack newStack) {
        if (inventory == null || slotIndex < 0 || slotIndex >= inventory.getContainerSize()) {
            return;
        }

        Player player = inventory.player;
        if (player == null || !player.level().isClientSide()) {
            return;
        }

        ItemStack oldStack = inventory.getItem(slotIndex);
        if (sameStack(oldStack, newStack)) {
            return;
        }

        LOGGER.debug("Client slot change detected with slotIndex {}", Integer.valueOf(slotIndex));
        UltimateWeight1201.onClientInventoryDelta(player, slotIndex, oldStack, newStack);
    }

    public static void onSelectedDrop(Inventory inventory, int slotIndex, ItemStack oldStack, ItemStack newStack) {
        if (inventory == null || slotIndex < 0 || slotIndex >= inventory.getContainerSize() || sameStack(oldStack, newStack)) {
            return;
        }

        Player player = inventory.player;
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        LOGGER.debug("Server selected-slot drop detected for player {} in slot {}", serverPlayer.getName().getString(), Integer.valueOf(slotIndex));
        UltimateWeight1201.onServerInventoryDelta(serverPlayer, slotIndex, oldStack, newStack);
    }

    public static List<ItemStack> captureMenuSlots(AbstractContainerMenu menu) {
        ArrayList<ItemStack> slotItems = new ArrayList<ItemStack>(menu.slots.size());
        for (int index = 0; index < menu.slots.size(); index++) {
            slotItems.add(menu.getSlot(index).getItem().copy());
        }
        return slotItems;
    }

    public static void onContainerChange(
        AbstractContainerMenu menu,
        List<ContainerListener> listeners,
        List<ItemStack> previousItems
    ) {
        if (menu == null || listeners == null || listeners.isEmpty() || previousItems == null) {
            return;
        }

        int size = Math.min(previousItems.size(), menu.slots.size());
        for (int slotIndex = 0; slotIndex < size; slotIndex++) {
            ItemStack oldStack = previousItems.get(slotIndex);
            ItemStack newStack = menu.getSlot(slotIndex).getItem();
            if (sameStack(oldStack, newStack)) {
                continue;
            }

            Slot slot = menu.getSlot(slotIndex);
            for (ContainerListener listener : listeners) {
                if (!(listener instanceof ServerPlayer player) || slot.container != player.getInventory()) {
                    continue;
                }

                LOGGER.debug(
                    "Container change detected for player {} in slot {}. Old: {}, New: {}",
                    player.getName().getString(),
                    Integer.valueOf(slotIndex),
                    oldStack,
                    newStack
                );
                UltimateWeight1201.onServerInventoryDelta(player, slotIndex, oldStack, newStack);
            }
        }
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        if (first == second) {
            return true;
        }
        if (first == null || first.isEmpty()) {
            return second == null || second.isEmpty();
        }
        if (second == null || second.isEmpty()) {
            return false;
        }
        return first.getCount() == second.getCount() && ItemStack.isSameItemSameTags(first, second);
    }
}

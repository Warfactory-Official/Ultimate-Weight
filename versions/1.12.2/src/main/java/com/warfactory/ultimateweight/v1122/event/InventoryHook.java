package com.warfactory.ultimateweight.v1122.event;

import com.warfactory.ultimateweight.logging.WeightLoggers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public final class InventoryHook {
    private static final WeightLoggers.WeightLogger LOGGER = WeightLoggers.component("inventory_hook");

    private InventoryHook() {
    }

    public static void onClientSlotChange(InventoryPlayer inventory, int slotIndex, ItemStack newStack) {
        if (inventory == null || slotIndex < 0 || slotIndex >= inventory.getSizeInventory()) {
            return;
        }

        EntityPlayer player = inventory.player;
        if (player == null || !player.world.isRemote) {
            return;
        }

        ItemStack oldStack = inventory.getStackInSlot(slotIndex);
        if (ItemStack.areItemStacksEqual(oldStack, newStack)) {
            return;
        }

        LOGGER.debug("Client slot change detected with slotIndex {}", Integer.valueOf(slotIndex));
        MinecraftForge.EVENT_BUS.post(
                WeightInventoryChangeEvent.clientDelta(player, slotIndex, oldStack, newStack)
        );
    }

    public static void onContainerChange(
            Container container,
            List<IContainerListener> listeners,
            int slotIndex,
            ItemStack oldStack,
            ItemStack newStack
    ) {
        if (!(container instanceof ContainerPlayer) || listeners == null || slotIndex < 0 || slotIndex >= container.inventorySlots.size()) {
            return;
        }
        if (ItemStack.areItemStacksEqual(oldStack, newStack)) {
            return;
        }
        if ((oldStack == null || oldStack.isEmpty()) && (newStack == null || newStack.isEmpty())) {
            return;
        }

        Slot slot = container.getSlot(slotIndex);
        for (IContainerListener listener : listeners) {
            if (!(listener instanceof EntityPlayerMP)) {
                continue;
            }

            EntityPlayerMP player = (EntityPlayerMP) listener;
            if (slot.inventory != player.inventory) {
                continue;
            }

            LOGGER.debug(
                    "Container change detected for player {} in slot {}. Old: {}, New: {}",
                    player.getName(),
                    Integer.valueOf(slotIndex),
                    oldStack,
                    newStack
            );
            MinecraftForge.EVENT_BUS.post(
                    WeightInventoryChangeEvent.serverDelta(player, slotIndex, oldStack, newStack)
            );
        }
    }
}

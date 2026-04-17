package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.v1122.UltimateWeightState1122;
import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

public final class WeightManager {
    private static final double EPSILON = 0.000001D;

    private WeightManager() {
    }

    public static void markInventoryDirty(EntityPlayerMP player) {
        UltimateWeightState1122.markDirty(player);
    }

    public static boolean isTransferAllowedClient(EntityPlayer player, int slotId, ClickType clickType, int dragType) {
        if (player == null || slotId < 0 || player.openContainer == null) {
            return true;
        }
        if (slotId >= player.openContainer.inventorySlots.size()) {
            return true;
        }

        Slot slot = player.openContainer.inventorySlots.get(slotId);
        return isTransferAllowed(player, slot, clickType, dragType);
    }

    public static boolean isTransferAllowed(EntityPlayer player, Slot slot, ClickType clickType) {
        return isTransferAllowed(player, slot, clickType, -1);
    }

    public static boolean isTransferAllowed(EntityPlayer player, Slot slot, ClickType clickType, int dragType) {
        if (player == null || player.capabilities.isCreativeMode || slot == null) {
            return true;
        }

        double additionalWeightKg = additionalWeightKg(player, slot, clickType, dragType);
        if (additionalWeightKg <= EPSILON) {
            return true;
        }

        double totalWeightKg = currentWeightKg(player);
        boolean allowed = totalWeightKg + additionalWeightKg < UltimateWeightCommon.bootstrap().config().hardLockWeightKg() - EPSILON;
        if (!allowed) {
            player.sendStatusMessage(new TextComponentTranslation("message.wfweight.transfer_blocked"), true);
        }
        return allowed;
    }

    private static double additionalWeightKg(EntityPlayer player, Slot slot, ClickType clickType, int dragType) {
        switch (clickType) {
            case PICKUP:
                return pickupAdditionalWeightKg(player, slot, dragType);
            case QUICK_MOVE:
                return quickMoveAdditionalWeightKg(player, slot);
            case SWAP:
                return swapAdditionalWeightKg(player, slot, dragType);
            default:
                return 0.0D;
        }
    }

    private static double pickupAdditionalWeightKg(EntityPlayer player, Slot slot, int dragType) {
        if (!isPlayerInventorySlot(player, slot)) {
            return 0.0D;
        }

        ItemStack carried = player.inventory.getItemStack();
        if (carried.isEmpty() || !slot.isItemValid(carried)) {
            return 0.0D;
        }

        ItemStack existing = slot.getStack();
        if (existing.isEmpty()) {
            return weightOf(placedStack(slot, carried, dragType));
        }

        if (ItemStack.areItemsEqual(existing, carried) && ItemStack.areItemStackTagsEqual(existing, carried)) {
            return weightOf(mergedStack(slot, existing, carried, dragType));
        }

        ItemStack placed = placedStack(slot, carried, -1);
        if (placed.isEmpty()) {
            return 0.0D;
        }
        return positiveDelta(weightOf(placed) - weightOf(existing));
    }

    private static double quickMoveAdditionalWeightKg(EntityPlayer player, Slot slot) {
        if (isPlayerInventorySlot(player, slot) || !slot.getHasStack() || !slot.canTakeStack(player)) {
            return 0.0D;
        }
        return weightOf(slot.getStack());
    }

    private static double swapAdditionalWeightKg(EntityPlayer player, Slot slot, int dragType) {
        if (isPlayerInventorySlot(player, slot)
            || dragType < 0
            || dragType >= InventoryPlayer.getHotbarSize()
            || !slot.getHasStack()
            || !slot.canTakeStack(player)) {
            return 0.0D;
        }

        ItemStack outgoing = player.inventory.getStackInSlot(dragType);
        if (!outgoing.isEmpty() && !slot.isItemValid(outgoing)) {
            return 0.0D;
        }
        return positiveDelta(weightOf(slot.getStack()) - weightOf(outgoing));
    }

    private static ItemStack placedStack(Slot slot, ItemStack carried, int dragType) {
        if (carried.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int limit = Math.min(slot.getItemStackLimit(carried), carried.getMaxStackSize());
        if (limit <= 0) {
            return ItemStack.EMPTY;
        }

        int count = Math.min(carried.getCount(), limit);
        if (dragType == 1) {
            count = 1;
        }
        return count <= 0 ? ItemStack.EMPTY : copyWithCount(carried, count);
    }

    private static ItemStack mergedStack(Slot slot, ItemStack existing, ItemStack carried, int dragType) {
        int limit = Math.min(slot.getItemStackLimit(carried), carried.getMaxStackSize());
        int space = limit - existing.getCount();
        if (space <= 0) {
            return ItemStack.EMPTY;
        }

        int count = dragType == 1 ? 1 : Math.min(space, carried.getCount());
        return count <= 0 ? ItemStack.EMPTY : copyWithCount(carried, count);
    }

    private static boolean isPlayerInventorySlot(EntityPlayer player, Slot slot) {
        return slot.inventory == player.inventory;
    }

    private static ItemStack copyWithCount(ItemStack stack, int count) {
        if (stack.isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    private static double weightOf(ItemStack stack) {
        return stack.isEmpty() ? 0.0D : WeightViews1122.weightOf(stack);
    }

    private static double currentWeightKg(EntityPlayer player) {
        if (player.world.isRemote) {
            return UltimateWeightState1122.hudWeightKg(player);
        }
        return WeightViews1122.totalWeight(player);
    }

    private static double positiveDelta(double delta) {
        return delta > EPSILON ? delta : 0.0D;
    }
}

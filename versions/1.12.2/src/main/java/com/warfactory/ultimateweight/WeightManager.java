package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.core.InventoryConstraintEvaluator;
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
        ItemStack removedStack = removedStack(player, slot, clickType, dragType);
        ItemStack addedStack = addedStack(player, slot, clickType, dragType);
        InventoryConstraintEvaluator.GroupLimitViolation violation =
            UltimateWeightCommon.bootstrap().constraintEvaluator().findDeltaViolation(
                WeightViews1122.player(player),
                removedStack.isEmpty() ? null : WeightViews1122.stack(removedStack),
                addedStack.isEmpty() ? null : WeightViews1122.stack(addedStack)
            );
        if (violation != null) {
            player.sendStatusMessage(
                new TextComponentTranslation(
                    "message.wfweight.group_limit_transfer_blocked",
                    violation.label(),
                    Integer.valueOf(violation.limit())
                ),
                true
            );
            return false;
        }

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

    private static ItemStack addedStack(EntityPlayer player, Slot slot, ClickType clickType, int dragType) {
        switch (clickType) {
            case PICKUP:
                return pickupAddedStack(player, slot, dragType);
            case QUICK_MOVE:
                return quickMoveAddedStack(player, slot);
            case SWAP:
                return swapAddedStack(player, slot, dragType);
            default:
                return ItemStack.EMPTY;
        }
    }

    private static ItemStack removedStack(EntityPlayer player, Slot slot, ClickType clickType, int dragType) {
        switch (clickType) {
            case PICKUP:
                return pickupRemovedStack(player, slot, dragType);
            case SWAP:
                return swapRemovedStack(player, slot, dragType);
            default:
                return ItemStack.EMPTY;
        }
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
        return weightOf(pickupAddedStack(player, slot, dragType));
    }

    private static ItemStack pickupAddedStack(EntityPlayer player, Slot slot, int dragType) {
        if (!isPlayerInventorySlot(player, slot)) {
            return ItemStack.EMPTY;
        }

        ItemStack carried = player.inventory.getItemStack();
        if (carried.isEmpty() || !slot.isItemValid(carried)) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = slot.getStack();
        if (existing.isEmpty()) {
            return placedStack(slot, carried, dragType);
        }

        if (ItemStack.areItemsEqual(existing, carried) && ItemStack.areItemStackTagsEqual(existing, carried)) {
            return mergedStack(slot, existing, carried, dragType);
        }

        ItemStack placed = placedStack(slot, carried, -1);
        if (placed.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return placed;
    }

    private static ItemStack pickupRemovedStack(EntityPlayer player, Slot slot, int dragType) {
        if (!isPlayerInventorySlot(player, slot)) {
            return ItemStack.EMPTY;
        }

        ItemStack carried = player.inventory.getItemStack();
        if (carried.isEmpty() || !slot.isItemValid(carried)) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = slot.getStack();
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (ItemStack.areItemsEqual(existing, carried) && ItemStack.areItemStackTagsEqual(existing, carried)) {
            return ItemStack.EMPTY;
        }
        return existing;
    }

    private static double quickMoveAdditionalWeightKg(EntityPlayer player, Slot slot) {
        return weightOf(quickMoveAddedStack(player, slot));
    }

    private static ItemStack quickMoveAddedStack(EntityPlayer player, Slot slot) {
        if (isPlayerInventorySlot(player, slot) || !slot.getHasStack() || !slot.canTakeStack(player)) {
            return ItemStack.EMPTY;
        }
        return slot.getStack();
    }

    private static double swapAdditionalWeightKg(EntityPlayer player, Slot slot, int dragType) {
        ItemStack added = swapAddedStack(player, slot, dragType);
        if (added.isEmpty()) {
            return 0.0D;
        }
        ItemStack outgoing = player.inventory.getStackInSlot(dragType);
        return positiveDelta(weightOf(added) - weightOf(outgoing));
    }

    private static ItemStack swapAddedStack(EntityPlayer player, Slot slot, int dragType) {
        if (isPlayerInventorySlot(player, slot)
            || dragType < 0
            || dragType >= InventoryPlayer.getHotbarSize()
            || !slot.getHasStack()
            || !slot.canTakeStack(player)) {
            return ItemStack.EMPTY;
        }

        ItemStack outgoing = player.inventory.getStackInSlot(dragType);
        if (!outgoing.isEmpty() && !slot.isItemValid(outgoing)) {
            return ItemStack.EMPTY;
        }
        return slot.getStack();
    }

    private static ItemStack swapRemovedStack(EntityPlayer player, Slot slot, int dragType) {
        if (isPlayerInventorySlot(player, slot)
            || dragType < 0
            || dragType >= InventoryPlayer.getHotbarSize()
            || !slot.getHasStack()
            || !slot.canTakeStack(player)) {
            return ItemStack.EMPTY;
        }
        return player.inventory.getStackInSlot(dragType);
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

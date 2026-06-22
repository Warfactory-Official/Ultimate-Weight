package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.core.InventoryConstraintEvaluator;
import com.warfactory.ultimateweight.v1122.UltimateWeightState1122;
import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public final class WeightManager {
    private static final double EPSILON = 0.000001D;

    private WeightManager() {
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

    /**
     * Captures the full menu state before a click that the predictive {@link #isTransferAllowed} gate
     * permitted, so {@link #finishMenuClick} can authoritatively roll back any heavy or group-limit
     * over-fill the predictive model cannot foresee (drag-distribute, multi-slot shift-click, ...).
     * Returns {@code null} off the server, where no authoritative rollback is needed.
     */
    public static MenuClickSnapshot snapshotMenuClick(Container menu, EntityPlayer player, int clickedSlot) {
        if (!(player instanceof EntityPlayerMP)) {
            return null;
        }

        List<ItemStack> slotItems = new ArrayList<ItemStack>(menu.inventorySlots.size());
        for (int index = 0; index < menu.inventorySlots.size(); index++) {
            slotItems.add(menu.getSlot(index).getStack().copy());
        }
        return new MenuClickSnapshot(
            slotItems,
            player.inventory.getItemStack().copy(),
            WeightViews1122.totalWeight(player),
            clickedSlot,
            UltimateWeightCommon.bootstrap().constraintEvaluator().resolveGroupLimitState(WeightViews1122.player(player))
        );
    }

    /**
     * Authoritative post-click check mirroring the 1.20.1/1.21.1 rollback. If the executed click pushed
     * the player across the weight hard lock or worsened a group limit, the whole menu is restored to
     * {@code snapshot} and the client is resynced - except when a dynamic (capability-backed) container
     * was involved, whose contents live outside a restorable slot, so the change is accepted and the
     * weight rescanned instead (rolling back slots there would dupe or lose the stashed item).
     */
    public static void finishMenuClick(Container menu, EntityPlayer player, MenuClickSnapshot snapshot) {
        if (snapshot == null || !(player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        if (serverPlayer.capabilities.isCreativeMode) {
            return;
        }

        double afterWeightKg = WeightViews1122.totalWeight(serverPlayer);
        InventoryConstraintEvaluator.GroupLimitState afterGroups =
            UltimateWeightCommon.bootstrap().constraintEvaluator().resolveGroupLimitState(WeightViews1122.player(serverPlayer));
        InventoryConstraintEvaluator.GroupLimitViolation violation =
            UltimateWeightCommon.bootstrap().constraintEvaluator().findWorsenedViolation(snapshot.beforeGroups(), afterGroups);
        boolean overweight = afterWeightKg + EPSILON >= UltimateWeightCommon.bootstrap().config().hardLockWeightKg()
            && afterWeightKg > snapshot.beforeWeightKg() + EPSILON;
        if (!overweight && violation == null) {
            return;
        }

        if (involvesDynamicContainer(menu, serverPlayer, snapshot)) {
            UltimateWeightState1122.markDirty(serverPlayer);
            return;
        }

        restoreMenu(serverPlayer, menu, snapshot);
        if (violation != null) {
            serverPlayer.sendStatusMessage(
                new TextComponentTranslation(
                    "message.wfweight.group_limit_transfer_blocked",
                    violation.label(),
                    Integer.valueOf(violation.limit())
                ),
                true
            );
        } else {
            serverPlayer.sendStatusMessage(new TextComponentTranslation("message.wfweight.transfer_blocked"), true);
        }
        UltimateWeightState1122.markDirty(serverPlayer);
    }

    private static void restoreMenu(EntityPlayerMP player, Container menu, MenuClickSnapshot snapshot) {
        int slotCount = Math.min(snapshot.slotItems().size(), menu.inventorySlots.size());
        for (int index = 0; index < slotCount; index++) {
            menu.getSlot(index).putStack(snapshot.slotItems().get(index).copy());
        }
        player.inventory.setItemStack(snapshot.carried().copy());
        // sendAllContents pushes a full SPacketWindowItems plus the cursor (SPacketSetSlot -1/-1), so
        // the client's optimistic view of the rejected click is fully overwritten.
        player.sendAllContents(menu, menu.getInventory());
    }

    private static boolean involvesDynamicContainer(Container menu, EntityPlayer player, MenuClickSnapshot snapshot) {
        if (WeightViews1122.isDynamicContainer(player.inventory.getItemStack())
            || WeightViews1122.isDynamicContainer(snapshot.carried())) {
            return true;
        }
        int clickedSlot = snapshot.clickedSlot();
        if (clickedSlot >= 0
            && clickedSlot < menu.inventorySlots.size()
            && WeightViews1122.isDynamicContainer(menu.getSlot(clickedSlot).getStack())) {
            return true;
        }
        return clickedSlot >= 0
            && clickedSlot < snapshot.slotItems().size()
            && WeightViews1122.isDynamicContainer(snapshot.slotItems().get(clickedSlot));
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

    /** Pre-click menu state captured for the authoritative post-click rollback in {@link #finishMenuClick}. */
    public static final class MenuClickSnapshot {
        private final List<ItemStack> slotItems;
        private final ItemStack carried;
        private final double beforeWeightKg;
        private final int clickedSlot;
        private final InventoryConstraintEvaluator.GroupLimitState beforeGroups;

        private MenuClickSnapshot(
            List<ItemStack> slotItems,
            ItemStack carried,
            double beforeWeightKg,
            int clickedSlot,
            InventoryConstraintEvaluator.GroupLimitState beforeGroups
        ) {
            this.slotItems = slotItems;
            this.carried = carried;
            this.beforeWeightKg = beforeWeightKg;
            this.clickedSlot = clickedSlot;
            this.beforeGroups = beforeGroups;
        }

        private List<ItemStack> slotItems() {
            return slotItems;
        }

        private ItemStack carried() {
            return carried;
        }

        private double beforeWeightKg() {
            return beforeWeightKg;
        }

        private int clickedSlot() {
            return clickedSlot;
        }

        private InventoryConstraintEvaluator.GroupLimitState beforeGroups() {
            return beforeGroups;
        }
    }
}

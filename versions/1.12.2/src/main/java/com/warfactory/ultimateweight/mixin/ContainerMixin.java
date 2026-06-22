package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.WeightManager;
import com.warfactory.ultimateweight.v1122.event.InventoryHook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Container.class)
public abstract class ContainerMixin {
    @Unique
    private static final Logger ultimateweight$logger = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    @Shadow
    public List<Slot> inventorySlots;

    @Shadow
    public List<IContainerListener> listeners;

    @Shadow
    public abstract NonNullList<ItemStack> getInventory();

    @Unique
    private WeightManager.MenuClickSnapshot ultimateweight$clickSnapshot;

    @Redirect(
            method = "detectAndSendChanges",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object redirectInventoryItemStacksSet(NonNullList<ItemStack> instance, int index, Object element) {
        ItemStack newItem = (ItemStack) element;
        ItemStack previousItem = instance.set(index, newItem);

        InventoryHook.onContainerChange((Container) (Object) this, this.listeners, index, previousItem, newItem);

        return previousItem;
    }


    @Inject(method = "slotClick", at = @At("HEAD"), cancellable = true)
    private void ultimateweight$refuseOverweightTransfer(
        int slotId,
        int dragType,
        ClickType clickType,
        EntityPlayer player,
        CallbackInfoReturnable<ItemStack> callbackInfo
    ) {
        this.ultimateweight$clickSnapshot = null;
        if (slotId < 0 || slotId >= this.inventorySlots.size()) {
            return;
        }

        Slot slot = this.inventorySlots.get(slotId);
        if (slot == null) {
            return;
        }
        if (WeightManager.isTransferAllowed(player, slot, clickType, dragType)) {
            // Predictive gate allowed the click; snapshot the menu so the RETURN hook can authoritatively
            // roll back any heavy/over-limit transfer it could not foresee (drag-distribute, etc.).
            this.ultimateweight$clickSnapshot = WeightManager.snapshotMenuClick((Container) (Object) this, player, slotId);
            return;
        }

        if (UltimateWeightCommon.isDebugEnabled()) {
            ultimateweight$logger.info(
                "1.12.2 mixin blocked slotClick for player={}, container={}, slotId={}, dragType={}, clickType={}, slotStack={}, carried={}.",
                player.getName(),
                ((Container) (Object) this).getClass().getName(),
                Integer.valueOf(slotId),
                Integer.valueOf(dragType),
                clickType,
                ultimateweight$describeStack(slot.getStack()),
                ultimateweight$describeStack(player.inventory.getItemStack())
            );
        }

        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).sendAllContents((Container) (Object) this, this.getInventory());
        }
        callbackInfo.setReturnValue(ItemStack.EMPTY);
    }

    @Inject(method = "slotClick", at = @At("RETURN"))
    private void ultimateweight$rollbackHeavyTransfer(
        int slotId,
        int dragType,
        ClickType clickType,
        EntityPlayer player,
        CallbackInfoReturnable<ItemStack> callbackInfo
    ) {
        WeightManager.MenuClickSnapshot snapshot = this.ultimateweight$clickSnapshot;
        this.ultimateweight$clickSnapshot = null;
        if (snapshot != null) {
            WeightManager.finishMenuClick((Container) (Object) this, player, snapshot);
        }
    }

    @Unique
    private static String ultimateweight$describeStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        return stack.getCount() + "x" + stack.getItem().getRegistryName();
    }
}

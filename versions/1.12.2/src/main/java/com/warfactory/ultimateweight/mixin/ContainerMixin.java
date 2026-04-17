package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.WeightManager;
import java.util.List;

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

@Mixin(value = Container.class, remap = false)
public abstract class ContainerMixin {
    @Unique
    private static final Logger ultimateweight$logger = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    @Shadow(remap = false)
    public List<Slot> inventorySlots;

    @Shadow(remap = false)
    public List<IContainerListener> listeners;

    @Shadow(remap = false)
    public abstract NonNullList<ItemStack> getInventory();

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


    @Inject(method = "slotClick", at = @At("HEAD"), cancellable = true, remap = false)
    private void ultimateweight$refuseOverweightTransfer(
        int slotId,
        int dragType,
        ClickType clickType,
        EntityPlayer player,
        CallbackInfoReturnable<ItemStack> callbackInfo
    ) {
        if (slotId < 0 || slotId >= this.inventorySlots.size()) {
            return;
        }

        Slot slot = this.inventorySlots.get(slotId);
        if (slot == null || WeightManager.isTransferAllowed(player, slot, clickType, dragType)) {
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

    @Unique
    private static String ultimateweight$describeStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        return stack.getCount() + "x" + stack.getItem().getRegistryName();
    }
}

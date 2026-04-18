package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1201.event.InventoryHook1201;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow
    public int selected;

    @Unique
    private ItemStack ultimateweight$selectedBeforeDrop = ItemStack.EMPTY;

    @Inject(method = "setItem", at = @At("HEAD"))
    private void ultimateweight$onSetItem(int index, ItemStack stack, CallbackInfo callbackInfo) {
        InventoryHook1201.onClientSlotChange((Inventory) (Object) this, index, stack);
    }

    @Inject(method = "removeFromSelected", at = @At("HEAD"))
    private void ultimateweight$captureSelectedBeforeDrop(boolean removeAll, CallbackInfoReturnable<ItemStack> callbackInfo) {
        Inventory inventory = (Inventory) (Object) this;
        this.ultimateweight$selectedBeforeDrop = inventory.getItem(this.selected).copy();
    }

    @Inject(method = "removeFromSelected", at = @At("RETURN"))
    private void ultimateweight$trackSelectedDrop(boolean removeAll, CallbackInfoReturnable<ItemStack> callbackInfo) {
        Inventory inventory = (Inventory) (Object) this;
        InventoryHook1201.onSelectedDrop(inventory, this.selected, this.ultimateweight$selectedBeforeDrop, inventory.getItem(this.selected));
        this.ultimateweight$selectedBeforeDrop = ItemStack.EMPTY;
    }
}

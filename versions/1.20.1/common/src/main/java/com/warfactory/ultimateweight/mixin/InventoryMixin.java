package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1201.event.InventoryHook1201;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Inject(method = "setItem", at = @At("HEAD"))
    private void ultimateweight$onSetItem(int index, ItemStack stack, CallbackInfo callbackInfo) {
        InventoryHook1201.onClientSlotChange((Inventory) (Object) this, index, stack);
    }
}

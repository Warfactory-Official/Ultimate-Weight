package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1122.event.InventoryHook;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryPlayer.class)
public abstract class InventoryPlayerMixin {

    @Inject(
            method = "setInventorySlotContents",
            at = @At("HEAD")
    )
    private void onSetInventorySlotContents(int index, ItemStack stack, CallbackInfo ci) {
        // (Object) this cast is required to satisfy the compiler when casting a Mixin to its target class
        InventoryHook.onClientSlotChange((InventoryPlayer) (Object) this, index, stack);
    }
}

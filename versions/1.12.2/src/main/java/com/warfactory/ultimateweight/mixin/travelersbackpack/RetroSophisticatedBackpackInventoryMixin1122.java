package com.warfactory.ultimateweight.mixin.travelersbackpack;

import com.cleanroommc.retrosophisticatedbackpacks.inventory.BackpackItemStackHandler;
import com.warfactory.ultimateweight.v1122.UltimateWeightState1122;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = BackpackItemStackHandler.class, remap = false)
public abstract class RetroSophisticatedBackpackInventoryMixin1122 {
    @Inject(method = "insertItem", at = @At("RETURN"))
    private void ultimateweight$markDirtyAfterInsert(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> callbackInfo) {
        if (!simulate) {
            UltimateWeightState1122.onNestedBackpackContentsChanged();
        }
    }

    @Inject(method = "extractItem", at = @At("RETURN"))
    private void ultimateweight$markDirtyAfterExtract(int slot, int amount, boolean simulate, CallbackInfoReturnable<ItemStack> callbackInfo) {
        if (!simulate) {
            UltimateWeightState1122.onNestedBackpackContentsChanged();
        }
    }

    @Inject(method = "setStackInSlot", at = @At("RETURN"), require = 0)
    private void ultimateweight$markDirtyAfterSetStackInSlot(int slot, ItemStack stack, CallbackInfo callbackInfo) {
        UltimateWeightState1122.onNestedBackpackContentsChanged();
    }
}

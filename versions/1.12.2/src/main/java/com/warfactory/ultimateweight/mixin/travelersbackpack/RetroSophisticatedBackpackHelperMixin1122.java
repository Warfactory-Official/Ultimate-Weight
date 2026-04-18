package com.warfactory.ultimateweight.mixin.travelersbackpack;

import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper;
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper;
import com.warfactory.ultimateweight.v1122.UltimateWeightState1122;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = BackpackInventoryHelper.class, remap = false)
public abstract class RetroSophisticatedBackpackHelperMixin1122 {
    @Inject(method = "sortInventory", at = @At("RETURN"))
    private void ultimateweight$markDirtyAfterSort(BackpackWrapper wrapper, CallbackInfo callbackInfo) {
        UltimateWeightState1122.onNestedBackpackContentsChanged();
    }

    @Inject(method = "transferPlayerInventoryToBackpack", at = @At("RETURN"))
    private void ultimateweight$markDirtyAfterTransferToBackpack(BackpackWrapper wrapper, PlayerMainInvWrapper playerInventory, boolean onlyToEmptySlots, CallbackInfo callbackInfo) {
        UltimateWeightState1122.onNestedBackpackContentsChanged();
    }

    @Inject(method = "transferBackpackToPlayerInventory", at = @At("RETURN"))
    private void ultimateweight$markDirtyAfterTransferToPlayer(BackpackWrapper wrapper, PlayerMainInvWrapper playerInventory, boolean onlyToEmptySlots, CallbackInfo callbackInfo) {
        UltimateWeightState1122.onNestedBackpackContentsChanged();
    }
}

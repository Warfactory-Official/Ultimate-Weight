package com.warfactory.ultimateweight.mixin.travelersbackpack;

import com.warfactory.ultimateweight.v1122.UltimateWeightState1122;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tiviacz.travelersbackpack.common.ServerActions", remap = false)
public abstract class ServerActionsMixin1122 {
    @Inject(method = "equipBackpack", at = @At("RETURN"))
    private static void ultimateweight$onEquipBackpack(EntityPlayer player, CallbackInfo callbackInfo) {
        UltimateWeightState1122.onTravelersBackpackStateChange(player);
    }

    @Inject(method = "unequipBackpack", at = @At("RETURN"))
    private static void ultimateweight$onUnequipBackpack(EntityPlayer player, CallbackInfo callbackInfo) {
        UltimateWeightState1122.onTravelersBackpackStateChange(player);
    }
}

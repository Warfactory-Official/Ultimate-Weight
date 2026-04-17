package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1201.client.UltimateWeightClientState1201;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class ClientEntitySprintMixin {
    @Inject(method = "setSprinting(Z)V", at = @At("HEAD"), cancellable = true)
    private void ultimateweight$preventExhaustedSprint(boolean sprinting, CallbackInfo ci) {
        if (sprinting
            && (Object) this instanceof LocalPlayer
            && UltimateWeightClientState1201.isExhausted()) {
            ci.cancel();
        }
    }
}

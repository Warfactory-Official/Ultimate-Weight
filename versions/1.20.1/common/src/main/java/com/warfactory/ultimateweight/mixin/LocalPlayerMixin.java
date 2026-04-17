package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1201.client.UltimateWeightClientState1201;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    private static final double EPSILON = 0.000001D;

    @Inject(method = "aiStep()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V", shift = At.Shift.AFTER))
    private void ultimateweight$clampMovementInput(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (player.input == null) {
            return;
        }

        if (UltimateWeightClientState1201.latest().speedMultiplier() <= EPSILON) {
            player.input.leftImpulse = 0.0F;
            player.input.forwardImpulse = 0.0F;
            player.input.left = false;
            player.input.right = false;
            player.input.up = false;
            player.input.down = false;
            player.setDeltaMovement(0.0D, player.getDeltaMovement().y, 0.0D);
            if (player.isSprinting()) {
                player.setSprinting(false);
            }
        }

        if (UltimateWeightClientState1201.latest().jumpMultiplier() <= EPSILON) {
            player.input.jumping = false;
        }
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void ultimateweight$stopSprintWhileExhausted(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (player.isSprinting() && UltimateWeightClientState1201.isExhausted()) {
            player.setSprinting(false);
        }
        if (UltimateWeightClientState1201.latest().speedMultiplier() <= EPSILON) {
            player.setDeltaMovement(0.0D, player.getDeltaMovement().y, 0.0D);
        }
    }
}

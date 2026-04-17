package com.warfactory.ultimateweight.mixin;

import com.mojang.authlib.GameProfile;
import com.warfactory.ultimateweight.v1122.UltimateWeightState1122;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityPlayerSP.class, remap = false)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {
    @Shadow(remap = false)
    public MovementInput movementInput;

    private static final double EPSILON = 0.000001D;

    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "setSprinting(Z)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void ultimateweight$preventExhaustedSprint(boolean sprinting, CallbackInfo ci) {
        com.warfactory.ultimateweight.v1122.capability.IPlayerWeightData1122 data =
            UltimateWeightState1122.resolveWeightData((EntityPlayerSP) (Object) this);
        if (sprinting && data != null && data.isExhausted()) {
            ci.cancel();
            super.setSprinting(false);
        }
    }

    @Inject(method = "onLivingUpdate()V", at = @At("TAIL"), remap = false)
    private void ultimateweight$stopSprintWhileExhausted(CallbackInfo ci) {
        com.warfactory.ultimateweight.v1122.capability.IPlayerWeightData1122 data =
            UltimateWeightState1122.resolveWeightData((EntityPlayerSP) (Object) this);
        if (this.isSprinting() && data != null && data.isExhausted()) {
            this.setSprinting(false);
        }
        if (data != null) {
            if (data.getSpeedMultiplier() <= EPSILON) {
                this.motionX = 0.0D;
                this.motionZ = 0.0D;
                if (this.isSprinting()) {
                    this.setSprinting(false);
                }
            }
            if (data.getJumpMultiplier() <= EPSILON && this.movementInput != null) {
                this.movementInput.jump = false;
            }
        }
    }
}

package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // 1.21.1 split getJumpPower into ()F and (F)F (the no-arg delegates to the float overload), so pin
    // the descriptor: scaling both would multiply the jump power twice. jumpFromGround() calls ()F.
    @Inject(method = "getJumpPower()F", at = @At("RETURN"), cancellable = true)
    private void ultimateweight$scaleJumpPower(CallbackInfoReturnable<Float> callbackInfo) {
        if (!((Object) this instanceof Player player)) {
            return;
        }

        callbackInfo.setReturnValue((float) (callbackInfo.getReturnValueF() * UltimateWeight1211.jumpMultiplier(player)));
    }

    // LivingEntity.setSprinting is the single funnel that both sets the sprint flag AND adds the
    // +30% sprint speed modifier. Cancelling at this chokepoint (rather than Entity.setSprinting)
    // prevents BOTH while exhausted - otherwise the speed boost was still applied even though the
    // flag was cleared, letting the player keep sprint speed. Works on client and server, so a
    // stray START_SPRINTING packet cannot re-enable sprinting server-side either.
    @Inject(method = "setSprinting(Z)V", at = @At("HEAD"), cancellable = true)
    private void ultimateweight$preventExhaustedSprint(boolean sprinting, CallbackInfo callbackInfo) {
        if (sprinting
            && (Object) this instanceof Player player
            && UltimateWeight1211.isSprintBlocked(player)) {
            callbackInfo.cancel();
        }
    }
}

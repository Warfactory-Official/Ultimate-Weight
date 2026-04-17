package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "getJumpPower", at = @At("RETURN"), cancellable = true)
    private void ultimateweight$scaleJumpPower(CallbackInfoReturnable<Float> callbackInfo) {
        if (!((Object) this instanceof Player player)) {
            return;
        }

        callbackInfo.setReturnValue((float) (callbackInfo.getReturnValueF() * UltimateWeight1201.jumpMultiplier(player)));
    }
}

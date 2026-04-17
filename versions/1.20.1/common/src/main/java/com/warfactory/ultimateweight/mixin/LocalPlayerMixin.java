package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1201.client.UltimateWeightClientState1201;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void ultimateweight$stopSprintWhileExhausted(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (player.isSprinting() && UltimateWeightClientState1201.isExhausted()) {
            player.setSprinting(false);
        }
    }
}

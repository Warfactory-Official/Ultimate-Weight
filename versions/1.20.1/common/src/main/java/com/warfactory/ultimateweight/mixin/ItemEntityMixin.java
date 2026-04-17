package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void ultimateweight$rejectPickup(Player player, CallbackInfo callbackInfo) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ItemEntity itemEntity = (ItemEntity) (Object) this;
        Component message = UltimateWeight1201.pickupBlockMessage(serverPlayer, itemEntity.getItem());
        if (message != null) {
            serverPlayer.displayClientMessage(message, true);
            callbackInfo.cancel();
        }
    }
}

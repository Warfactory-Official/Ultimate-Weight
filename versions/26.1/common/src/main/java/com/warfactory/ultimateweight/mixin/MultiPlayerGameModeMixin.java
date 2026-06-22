package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"), cancellable = true)
    private void ultimateweight$onHandleInventoryMouseClick(
        int containerId,
        int slotId,
        int mouseButton,
        ClickType clickType,
        Player player,
        CallbackInfo callbackInfo
    ) {
        if (slotId < 0) {
            return;
        }

        if (!UltimateWeight1211.isTransferAllowedClient(player, slotId, clickType, mouseButton)) {
            callbackInfo.cancel();
        }
    }
}

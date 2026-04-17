package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.WeightManager;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerControllerMP.class, remap = false)
public abstract class MixinPlayerControllerMP {

    @Inject(
        method = "windowClick(IIILnet/minecraft/inventory/ClickType;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void onWindowClick(
        int windowId,
        int slotId,
        int mouseButton,
        ClickType type,
        EntityPlayer player,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        if (slotId < 0) {
            return;
        }

        if (!WeightManager.isTransferAllowedClient(player, slotId, type, mouseButton)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}

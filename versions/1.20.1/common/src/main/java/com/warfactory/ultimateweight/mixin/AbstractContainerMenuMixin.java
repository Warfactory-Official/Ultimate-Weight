package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Unique
    private UltimateWeight1201.MenuClickSnapshot ultimateweight$clickSnapshot;

    @Inject(method = "clicked", at = @At("HEAD"))
    private void ultimateweight$snapshotClick(
        int slot,
        int button,
        ClickType clickType,
        Player player,
        CallbackInfo callbackInfo
    ) {
        this.ultimateweight$clickSnapshot = UltimateWeight1201.snapshotMenuClick(
            (AbstractContainerMenu) (Object) this,
            player,
            slot
        );
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void ultimateweight$rollbackHeavyTransfer(
        int slot,
        int button,
        ClickType clickType,
        Player player,
        CallbackInfo callbackInfo
    ) {
        UltimateWeight1201.finishMenuClick(
            (AbstractContainerMenu) (Object) this,
            player,
            this.ultimateweight$clickSnapshot
        );
        this.ultimateweight$clickSnapshot = null;
    }
}

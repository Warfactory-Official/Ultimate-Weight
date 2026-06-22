package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import com.warfactory.ultimateweight.v1211.event.InventoryHook1211;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Shadow
    public NonNullList<Slot> slots;

    @Shadow
    private List<ContainerListener> containerListeners;

    @Unique
    private UltimateWeight1211.MenuClickSnapshot ultimateweight$clickSnapshot;

    @Unique
    private List<ItemStack> ultimateweight$broadcastSnapshot;

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void ultimateweight$captureBroadcastState(CallbackInfo callbackInfo) {
        this.ultimateweight$broadcastSnapshot = InventoryHook1211.captureMenuSlots((AbstractContainerMenu) (Object) this);
    }

    @Inject(method = "broadcastChanges", at = @At("TAIL"))
    private void ultimateweight$trackBroadcastChanges(CallbackInfo callbackInfo) {
        InventoryHook1211.onContainerChange(
            (AbstractContainerMenu) (Object) this,
            this.containerListeners,
            this.ultimateweight$broadcastSnapshot
        );
        this.ultimateweight$broadcastSnapshot = null;
    }

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void ultimateweight$guardClick(
        int slot,
        int button,
        ClickType clickType,
        Player player,
        CallbackInfo callbackInfo
    ) {
        if (!(player instanceof ServerPlayer) || slot < 0 || slot >= this.slots.size()) {
            this.ultimateweight$clickSnapshot = UltimateWeight1211.snapshotMenuClick(
                (AbstractContainerMenu) (Object) this,
                player,
                slot
            );
            return;
        }

        Slot clickedSlot = this.slots.get(slot);
        if (!UltimateWeight1211.isTransferAllowed(player, clickedSlot, clickType, button)) {
            UltimateWeight1211.resyncMenu((ServerPlayer) player, (AbstractContainerMenu) (Object) this, slot);
            this.ultimateweight$clickSnapshot = null;
            callbackInfo.cancel();
            return;
        }

        this.ultimateweight$clickSnapshot = UltimateWeight1211.snapshotMenuClick(
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
        UltimateWeight1211.finishMenuClick(
            (AbstractContainerMenu) (Object) this,
            player,
            this.ultimateweight$clickSnapshot
        );
        this.ultimateweight$clickSnapshot = null;
    }
}

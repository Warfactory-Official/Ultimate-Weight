package com.warfactory.ultimateweight.mixin;

import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Event-driven change detection for Sophisticated Backpacks contents. A backpack's stored items
 * live in world SavedData, so a change does not bump the player inventory counters the weight
 * tracker watches; without this hook, upgrade-driven inserts (auto-pickup/feeding) while the GUI is
 * closed would only be picked up by the 30s failsafe scan. {@code InventoryHandler.onContentsChanged}
 * runs on every stored-slot mutation, so marking the player dirty here keeps weight event-driven.
 *
 * <p>{@code @Pseudo} + a string target keeps this a no-op when the mod is absent (Fabric / packs
 * without Sophisticated Backpacks); {@code require = 0} tolerates the method being renamed in other
 * mod versions, falling back to the failsafe scan rather than crashing.
 */
@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler", remap = false)
public abstract class SophisticatedBackpackContentsMixin {
    @Inject(method = "onContentsChanged", at = @At("RETURN"), require = 0, remap = false)
    private void ultimateweight$onContentsChanged(int slot, CallbackInfo callbackInfo) {
        UltimateWeight1201.onBackpackContentsChanged();
    }
}

package com.warfactory.ultimateweight.forge;

import com.warfactory.ultimateweight.core.WeightSnapshot;
import com.warfactory.ultimateweight.forge.capability.UltimateWeightCapabilities1201;
import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class UltimateWeightForgeStateHooks implements UltimateWeight1201.PlayerStateListener {
    @Override
    public void onSnapshot(ServerPlayer player, WeightSnapshot snapshot, boolean effectImmune) {
        UltimateWeightCapabilities1201.update(player, snapshot, effectImmune);
    }

    @Override
    public void onClone(Player original, Player clone) {
        UltimateWeightCapabilities1201.copy(original, clone);
    }

    @Override
    public void onPlayerLeave(ServerPlayer player) {
    }

    @Override
    public void onStamina(
        ServerPlayer player,
        double currentStamina,
        double maxStamina,
        boolean staminaEnabled,
        boolean exhausted
    ) {
        UltimateWeightCapabilities1201.updateStamina(player, currentStamina, maxStamina, staminaEnabled, exhausted);
    }

    @Override
    public UltimateWeight1201.StaminaState loadStamina(Player player) {
        var data = UltimateWeightCapabilities1201.get(player);
        if (data == null) {
            return null;
        }
        return new UltimateWeight1201.StaminaState(
            data.getCurrentStamina(),
            data.getMaxStamina(),
            data.isStaminaEnabled(),
            data.isExhausted()
        );
    }
}

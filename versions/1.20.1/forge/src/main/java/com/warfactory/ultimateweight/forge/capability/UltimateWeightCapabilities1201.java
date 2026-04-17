package com.warfactory.ultimateweight.forge.capability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class UltimateWeightCapabilities1201 {
    public static final Capability<IPlayerWeightData1201> PLAYER_WEIGHT_CAPABILITY = CapabilityManager.get(
        new CapabilityToken<IPlayerWeightData1201>() {
        }
    );

    private UltimateWeightCapabilities1201() {
    }

    public static IPlayerWeightData1201 get(Player player) {
        return player.getCapability(PLAYER_WEIGHT_CAPABILITY).resolve().orElse(null);
    }

    public static void copy(Player original, Player clone) {
        IPlayerWeightData1201 oldData = get(original);
        IPlayerWeightData1201 newData = get(clone);
        if (oldData == null || newData == null) {
            return;
        }

        newData.setCurrentWeightKg(oldData.getCurrentWeightKg());
        newData.setCarryCapacityKg(oldData.getCarryCapacityKg());
        newData.setSpeedMultiplier(oldData.getSpeedMultiplier());
        newData.setJumpMultiplier(oldData.getJumpMultiplier());
        newData.setHardLocked(oldData.isHardLocked());
        newData.setCurrentStamina(oldData.getCurrentStamina());
        newData.setMaxStamina(oldData.getMaxStamina());
        newData.setStaminaEnabled(oldData.isStaminaEnabled());
        newData.setExhausted(oldData.isExhausted());
    }

    public static void update(ServerPlayer player, com.warfactory.ultimateweight.core.WeightSnapshot snapshot, boolean effectImmune) {
        IPlayerWeightData1201 data = get(player);
        if (data == null) {
            return;
        }

        data.setCurrentWeightKg(snapshot.totalWeightKg());
        data.setCarryCapacityKg(snapshot.carryCapacityKg());
        data.setSpeedMultiplier(effectImmune ? 1.0D : snapshot.thresholdEffect().speedMultiplier());
        data.setJumpMultiplier(effectImmune ? 1.0D : snapshot.thresholdEffect().jumpMultiplier());
        data.setHardLocked(!effectImmune && snapshot.hardLocked());
    }

    public static void updateStamina(
        ServerPlayer player,
        double currentStamina,
        double maxStamina,
        boolean staminaEnabled,
        boolean exhausted
    ) {
        IPlayerWeightData1201 data = get(player);
        if (data == null) {
            return;
        }

        data.setCurrentStamina(currentStamina);
        data.setMaxStamina(maxStamina);
        data.setStaminaEnabled(staminaEnabled);
        data.setExhausted(exhausted);
    }
}

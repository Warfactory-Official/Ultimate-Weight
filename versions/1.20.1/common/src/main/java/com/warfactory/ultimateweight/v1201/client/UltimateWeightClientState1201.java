package com.warfactory.ultimateweight.v1201.client;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.core.StaminaMath;
import com.warfactory.ultimateweight.v1201.network.StaminaUpdatePacket1201;
import com.warfactory.ultimateweight.v1201.network.WeightUpdatePacket1201;

public final class UltimateWeightClientState1201 {
    private static volatile WeightUpdatePacket1201 latest = WeightUpdatePacket1201.empty();
    private static volatile StaminaUpdatePacket1201 latestStamina = StaminaUpdatePacket1201.empty();
    private static volatile boolean latestStaminaExhausted;

    private UltimateWeightClientState1201() {
    }

    public static void apply(WeightUpdatePacket1201 packet) {
        latest = packet;
    }

    public static WeightUpdatePacket1201 latest() {
        return latest;
    }

    public static void applyStamina(StaminaUpdatePacket1201 packet) {
        latestStamina = packet;
        latestStaminaExhausted = StaminaMath.resolveExhausted(
            UltimateWeightCommon.bootstrap().config().stamina(),
            packet.currentStamina(),
            packet.maxStamina(),
            packet.staminaEnabled(),
            latestStaminaExhausted
        );
    }

    public static StaminaUpdatePacket1201 latestStamina() {
        return latestStamina;
    }

    public static boolean isExhausted() {
        StaminaUpdatePacket1201 stamina = latestStamina;
        latestStaminaExhausted = StaminaMath.resolveExhausted(
            UltimateWeightCommon.bootstrap().config().stamina(),
            stamina.currentStamina(),
            stamina.maxStamina(),
            stamina.staminaEnabled(),
            latestStaminaExhausted
        );
        return latestStaminaExhausted;
    }

    public static void reset() {
        latest = WeightUpdatePacket1201.empty();
        latestStamina = StaminaUpdatePacket1201.empty();
        latestStaminaExhausted = false;
    }
}

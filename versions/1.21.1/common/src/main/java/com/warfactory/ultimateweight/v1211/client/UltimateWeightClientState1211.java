package com.warfactory.ultimateweight.v1211.client;

import com.warfactory.ultimateweight.v1211.network.StaminaUpdatePacket1211;
import com.warfactory.ultimateweight.v1211.network.WeightUpdatePacket1211;

public final class UltimateWeightClientState1211 {
    private static volatile WeightUpdatePacket1211 latest = WeightUpdatePacket1211.empty();
    private static volatile StaminaUpdatePacket1211 latestStamina = StaminaUpdatePacket1211.empty();

    private UltimateWeightClientState1211() {
    }

    public static void apply(WeightUpdatePacket1211 packet) {
        latest = packet;
    }

    public static WeightUpdatePacket1211 latest() {
        return latest;
    }

    public static void applyStamina(StaminaUpdatePacket1211 packet) {
        latestStamina = packet;
    }

    public static StaminaUpdatePacket1211 latestStamina() {
        return latestStamina;
    }

    public static boolean isExhausted() {
        return latestStamina.exhausted();
    }

    public static void reset() {
        latest = WeightUpdatePacket1211.empty();
        latestStamina = StaminaUpdatePacket1211.empty();
    }
}

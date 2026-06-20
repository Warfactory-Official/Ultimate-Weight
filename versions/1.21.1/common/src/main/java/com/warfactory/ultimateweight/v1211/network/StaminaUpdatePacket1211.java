package com.warfactory.ultimateweight.v1211.network;

import net.minecraft.network.FriendlyByteBuf;

public final class StaminaUpdatePacket1211 {
    private final double currentStamina;
    private final double maxStamina;
    private final boolean staminaEnabled;
    private final boolean exhausted;

    public StaminaUpdatePacket1211(double currentStamina, double maxStamina, boolean staminaEnabled, boolean exhausted) {
        this.currentStamina = currentStamina;
        this.maxStamina = maxStamina;
        this.staminaEnabled = staminaEnabled;
        this.exhausted = exhausted;
    }

    public static StaminaUpdatePacket1211 empty() {
        return new StaminaUpdatePacket1211(0.0D, 0.0D, false, false);
    }

    public static StaminaUpdatePacket1211 decode(FriendlyByteBuf buffer) {
        return new StaminaUpdatePacket1211(
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readBoolean(),
            buffer.readBoolean()
        );
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(currentStamina);
        buffer.writeDouble(maxStamina);
        buffer.writeBoolean(staminaEnabled);
        buffer.writeBoolean(exhausted);
    }

    public double currentStamina() {
        return currentStamina;
    }

    public double maxStamina() {
        return maxStamina;
    }

    public boolean staminaEnabled() {
        return staminaEnabled;
    }

    public boolean exhausted() {
        return exhausted;
    }
}

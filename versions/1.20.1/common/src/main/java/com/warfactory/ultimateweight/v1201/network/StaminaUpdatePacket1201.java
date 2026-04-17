package com.warfactory.ultimateweight.v1201.network;

import net.minecraft.network.FriendlyByteBuf;

public final class StaminaUpdatePacket1201 {
    private final double currentStamina;
    private final double maxStamina;
    private final boolean staminaEnabled;

    public StaminaUpdatePacket1201(double currentStamina, double maxStamina, boolean staminaEnabled) {
        this.currentStamina = currentStamina;
        this.maxStamina = maxStamina;
        this.staminaEnabled = staminaEnabled;
    }

    public static StaminaUpdatePacket1201 empty() {
        return new StaminaUpdatePacket1201(0.0D, 0.0D, false);
    }

    public static StaminaUpdatePacket1201 decode(FriendlyByteBuf buffer) {
        return new StaminaUpdatePacket1201(
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readBoolean()
        );
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(currentStamina);
        buffer.writeDouble(maxStamina);
        buffer.writeBoolean(staminaEnabled);
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
}

package com.warfactory.ultimateweight.v1122.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public final class PacketStaminaUpdate1122 implements IMessage {
    private double currentStamina;
    private double maxStamina;
    private boolean staminaEnabled;

    public PacketStaminaUpdate1122() {
    }

    public PacketStaminaUpdate1122(double currentStamina, double maxStamina, boolean staminaEnabled) {
        this.currentStamina = currentStamina;
        this.maxStamina = maxStamina;
        this.staminaEnabled = staminaEnabled;
    }

    public static PacketStaminaUpdate1122 empty() {
        return new PacketStaminaUpdate1122(0.0D, 0.0D, false);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        currentStamina = buf.readDouble();
        maxStamina = buf.readDouble();
        staminaEnabled = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(currentStamina);
        buf.writeDouble(maxStamina);
        buf.writeBoolean(staminaEnabled);
    }

    public double getCurrentStamina() {
        return currentStamina;
    }

    public double getMaxStamina() {
        return maxStamina;
    }

    public boolean isStaminaEnabled() {
        return staminaEnabled;
    }
}

package com.warfactory.ultimateweight.v1122.network;

import com.warfactory.ultimateweight.core.WeightSnapshot;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public final class PacketWeightUpdate1122 implements IMessage {
    private double totalWeightKg;
    private double carryCapacityKg;
    private double speedMultiplier;
    private double jumpMultiplier;
    private boolean hardLocked;

    public PacketWeightUpdate1122() {
    }

    public PacketWeightUpdate1122(
        double totalWeightKg,
        double carryCapacityKg,
        double speedMultiplier,
        double jumpMultiplier,
        boolean hardLocked
    ) {
        this.totalWeightKg = totalWeightKg;
        this.carryCapacityKg = carryCapacityKg;
        this.speedMultiplier = speedMultiplier;
        this.jumpMultiplier = jumpMultiplier;
        this.hardLocked = hardLocked;
    }

    public static PacketWeightUpdate1122 fromSnapshot(WeightSnapshot snapshot) {
        return new PacketWeightUpdate1122(
            snapshot.totalWeightKg(),
            snapshot.carryCapacityKg(),
            snapshot.thresholdEffect().speedMultiplier(),
            snapshot.thresholdEffect().jumpMultiplier(),
            snapshot.hardLocked()
        );
    }

    public static PacketWeightUpdate1122 empty() {
        return new PacketWeightUpdate1122(0.0D, 0.0D, 1.0D, 1.0D, false);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        totalWeightKg = buf.readDouble();
        carryCapacityKg = buf.readDouble();
        speedMultiplier = buf.readDouble();
        jumpMultiplier = buf.readDouble();
        hardLocked = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(totalWeightKg);
        buf.writeDouble(carryCapacityKg);
        buf.writeDouble(speedMultiplier);
        buf.writeDouble(jumpMultiplier);
        buf.writeBoolean(hardLocked);
    }

    public double getTotalWeightKg() {
        return totalWeightKg;
    }

    public double getCarryCapacityKg() {
        return carryCapacityKg;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public double getJumpMultiplier() {
        return jumpMultiplier;
    }

    public boolean isHardLocked() {
        return hardLocked;
    }
}

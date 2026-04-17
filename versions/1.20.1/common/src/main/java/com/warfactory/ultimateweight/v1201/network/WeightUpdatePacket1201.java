package com.warfactory.ultimateweight.v1201.network;

import com.warfactory.ultimateweight.core.WeightSnapshot;
import net.minecraft.network.FriendlyByteBuf;

public final class WeightUpdatePacket1201 {
    private final double totalWeightKg;
    private final double carryCapacityKg;
    private final double speedMultiplier;
    private final double jumpMultiplier;
    private final boolean hardLocked;

    public WeightUpdatePacket1201(
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

    public static WeightUpdatePacket1201 fromSnapshot(WeightSnapshot snapshot) {
        return new WeightUpdatePacket1201(
            snapshot.totalWeightKg(),
            snapshot.carryCapacityKg(),
            snapshot.thresholdEffect().speedMultiplier(),
            snapshot.thresholdEffect().jumpMultiplier(),
            snapshot.hardLocked()
        );
    }

    public static WeightUpdatePacket1201 empty() {
        return new WeightUpdatePacket1201(0.0D, 0.0D, 1.0D, 1.0D, false);
    }

    public static WeightUpdatePacket1201 decode(FriendlyByteBuf buffer) {
        return new WeightUpdatePacket1201(
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readBoolean()
        );
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(totalWeightKg);
        buffer.writeDouble(carryCapacityKg);
        buffer.writeDouble(speedMultiplier);
        buffer.writeDouble(jumpMultiplier);
        buffer.writeBoolean(hardLocked);
    }

    public double totalWeightKg() {
        return totalWeightKg;
    }

    public double carryCapacityKg() {
        return carryCapacityKg;
    }

    public double speedMultiplier() {
        return speedMultiplier;
    }

    public double jumpMultiplier() {
        return jumpMultiplier;
    }

    public boolean hardLocked() {
        return hardLocked;
    }
}

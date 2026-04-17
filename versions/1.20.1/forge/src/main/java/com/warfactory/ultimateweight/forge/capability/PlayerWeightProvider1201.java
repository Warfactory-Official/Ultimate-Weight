package com.warfactory.ultimateweight.forge.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerWeightProvider1201 implements ICapabilitySerializable<CompoundTag> {
    private final IPlayerWeightData1201 data = new PlayerWeightData1201();
    private final LazyOptional<IPlayerWeightData1201> optional = LazyOptional.of(() -> data);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
        @NotNull net.minecraftforge.common.capabilities.Capability<T> capability,
        @Nullable Direction side
    ) {
        return UltimateWeightCapabilities1201.PLAYER_WEIGHT_CAPABILITY.orEmpty(capability, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("currentWeightKg", data.getCurrentWeightKg());
        tag.putDouble("carryCapacityKg", data.getCarryCapacityKg());
        tag.putDouble("speedMultiplier", data.getSpeedMultiplier());
        tag.putDouble("jumpMultiplier", data.getJumpMultiplier());
        tag.putBoolean("hardLocked", data.isHardLocked());
        tag.putDouble("currentStamina", data.getCurrentStamina());
        tag.putDouble("maxStamina", data.getMaxStamina());
        tag.putBoolean("staminaEnabled", data.isStaminaEnabled());
        tag.putBoolean("staminaExhausted", data.isExhausted());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.setCurrentWeightKg(nbt.getDouble("currentWeightKg"));
        data.setCarryCapacityKg(nbt.getDouble("carryCapacityKg"));
        data.setSpeedMultiplier(nbt.getDouble("speedMultiplier"));
        data.setJumpMultiplier(nbt.getDouble("jumpMultiplier"));
        data.setHardLocked(nbt.getBoolean("hardLocked"));
        data.setCurrentStamina(nbt.getDouble("currentStamina"));
        data.setMaxStamina(nbt.getDouble("maxStamina"));
        data.setStaminaEnabled(nbt.getBoolean("staminaEnabled"));
        data.setExhausted(nbt.getBoolean("staminaExhausted"));
    }

    public void invalidate() {
        optional.invalidate();
    }
}

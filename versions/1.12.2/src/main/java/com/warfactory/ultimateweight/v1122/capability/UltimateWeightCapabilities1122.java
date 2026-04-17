package com.warfactory.ultimateweight.v1122.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public final class UltimateWeightCapabilities1122 {
    @CapabilityInject(IPlayerWeightData1122.class)
    public static final Capability<IPlayerWeightData1122> PLAYER_WEIGHT_DATA = null;

    private UltimateWeightCapabilities1122() {
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(
            IPlayerWeightData1122.class,
            new Capability.IStorage<IPlayerWeightData1122>() {
                @Override
                public NBTBase writeNBT(
                    Capability<IPlayerWeightData1122> capability,
                    IPlayerWeightData1122 instance,
                    EnumFacing side
                ) {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setDouble("currentWeightKg", instance.getCurrentWeightKg());
                    tag.setDouble("carryCapacityKg", instance.getCarryCapacityKg());
                    tag.setDouble("speedMultiplier", instance.getSpeedMultiplier());
                    tag.setDouble("jumpMultiplier", instance.getJumpMultiplier());
                    tag.setBoolean("hardLocked", instance.isHardLocked());
                    tag.setDouble("currentStamina", instance.getCurrentStamina());
                    tag.setDouble("maxStamina", instance.getMaxStamina());
                    tag.setBoolean("staminaEnabled", instance.isStaminaEnabled());
                    tag.setBoolean("staminaExhausted", instance.isExhausted());
                    return tag;
                }

                @Override
                public void readNBT(
                    Capability<IPlayerWeightData1122> capability,
                    IPlayerWeightData1122 instance,
                    EnumFacing side,
                    NBTBase nbt
                ) {
                    if (!(nbt instanceof NBTTagCompound)) {
                        return;
                    }

                    NBTTagCompound tag = (NBTTagCompound) nbt;
                    instance.setCurrentWeightKg(tag.getDouble("currentWeightKg"));
                    instance.setCarryCapacityKg(tag.getDouble("carryCapacityKg"));
                    instance.setSpeedMultiplier(tag.getDouble("speedMultiplier"));
                    instance.setJumpMultiplier(tag.getDouble("jumpMultiplier"));
                    instance.setHardLocked(tag.getBoolean("hardLocked"));
                    instance.setCurrentStamina(tag.getDouble("currentStamina"));
                    instance.setMaxStamina(tag.getDouble("maxStamina"));
                    instance.setStaminaEnabled(tag.getBoolean("staminaEnabled"));
                    instance.setExhausted(tag.getBoolean("staminaExhausted"));
                }
            },
            PlayerWeightData1122::new
        );
    }

    public static IPlayerWeightData1122 get(EntityPlayer player) {
        if (PLAYER_WEIGHT_DATA == null) {
            return null;
        }
        return player.getCapability(PLAYER_WEIGHT_DATA, null);
    }
}

package com.warfactory.ultimateweight.v1122.capability;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public final class PlayerWeightProvider1122 implements ICapabilitySerializable<NBTTagCompound> {
    private final IPlayerWeightData1122 data = new PlayerWeightData1122();

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == UltimateWeightCapabilities1122.PLAYER_WEIGHT_DATA;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == UltimateWeightCapabilities1122.PLAYER_WEIGHT_DATA) {
            return UltimateWeightCapabilities1122.PLAYER_WEIGHT_DATA.cast(data);
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTBase nbt = UltimateWeightCapabilities1122.PLAYER_WEIGHT_DATA.writeNBT(data, null);
        return nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        UltimateWeightCapabilities1122.PLAYER_WEIGHT_DATA.readNBT(data, null, nbt);
    }
}

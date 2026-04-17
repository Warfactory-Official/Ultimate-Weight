package com.warfactory.ultimateweight.forge.capability;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IPlayerWeightData1201 {
    double getCurrentWeightKg();

    void setCurrentWeightKg(double value);

    double getCarryCapacityKg();

    void setCarryCapacityKg(double value);

    double getSpeedMultiplier();

    void setSpeedMultiplier(double value);

    double getJumpMultiplier();

    void setJumpMultiplier(double value);

    boolean isHardLocked();

    void setHardLocked(boolean value);

    double getCurrentStamina();

    void setCurrentStamina(double value);

    double getMaxStamina();

    void setMaxStamina(double value);

    boolean isStaminaEnabled();

    void setStaminaEnabled(boolean value);
}

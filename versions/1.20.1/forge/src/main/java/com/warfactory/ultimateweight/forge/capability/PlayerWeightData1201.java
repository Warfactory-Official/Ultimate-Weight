package com.warfactory.ultimateweight.forge.capability;

public final class PlayerWeightData1201 implements IPlayerWeightData1201 {
    private double currentWeightKg;
    private double carryCapacityKg;
    private double speedMultiplier = 1.0D;
    private double jumpMultiplier = 1.0D;
    private boolean hardLocked;
    private double currentStamina;
    private double maxStamina;
    private boolean staminaEnabled;
    private boolean exhausted;

    @Override
    public double getCurrentWeightKg() {
        return currentWeightKg;
    }

    @Override
    public void setCurrentWeightKg(double value) {
        currentWeightKg = value;
    }

    @Override
    public double getCarryCapacityKg() {
        return carryCapacityKg;
    }

    @Override
    public void setCarryCapacityKg(double value) {
        carryCapacityKg = value;
    }

    @Override
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    @Override
    public void setSpeedMultiplier(double value) {
        speedMultiplier = value;
    }

    @Override
    public double getJumpMultiplier() {
        return jumpMultiplier;
    }

    @Override
    public void setJumpMultiplier(double value) {
        jumpMultiplier = value;
    }

    @Override
    public boolean isHardLocked() {
        return hardLocked;
    }

    @Override
    public void setHardLocked(boolean value) {
        hardLocked = value;
    }

    @Override
    public double getCurrentStamina() {
        return currentStamina;
    }

    @Override
    public void setCurrentStamina(double value) {
        currentStamina = value;
    }

    @Override
    public double getMaxStamina() {
        return maxStamina;
    }

    @Override
    public void setMaxStamina(double value) {
        maxStamina = value;
    }

    @Override
    public boolean isStaminaEnabled() {
        return staminaEnabled;
    }

    @Override
    public void setStaminaEnabled(boolean value) {
        staminaEnabled = value;
    }

    @Override
    public boolean isExhausted() {
        return exhausted;
    }

    @Override
    public void setExhausted(boolean value) {
        exhausted = value;
    }
}

package com.warfactory.ultimateweight.core;

public final class ThresholdEffect {
    private final int thresholdIndex;
    private final double loadPercent;
    private final double speedMultiplier;
    private final double jumpMultiplier;

    public ThresholdEffect(int thresholdIndex, double loadPercent, double speedMultiplier, double jumpMultiplier) {
        this.thresholdIndex = thresholdIndex;
        this.loadPercent = loadPercent;
        this.speedMultiplier = speedMultiplier;
        this.jumpMultiplier = jumpMultiplier;
    }

    public static ThresholdEffect defaults(double loadPercent) {
        return new ThresholdEffect(-1, loadPercent, 1.0D, 1.0D);
    }

    public int thresholdIndex() {
        return thresholdIndex;
    }

    public double loadPercent() {
        return loadPercent;
    }

    public double speedMultiplier() {
        return speedMultiplier;
    }

    public double jumpMultiplier() {
        return jumpMultiplier;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ThresholdEffect)) {
            return false;
        }

        ThresholdEffect effect = (ThresholdEffect) other;
        return thresholdIndex == effect.thresholdIndex
            && Double.compare(loadPercent, effect.loadPercent) == 0
            && Double.compare(speedMultiplier, effect.speedMultiplier) == 0
            && Double.compare(jumpMultiplier, effect.jumpMultiplier) == 0;
    }

    @Override
    public int hashCode() {
        int result = thresholdIndex;
        long value = Double.doubleToLongBits(loadPercent);
        result = 31 * result + (int) (value ^ (value >>> 32));
        value = Double.doubleToLongBits(speedMultiplier);
        result = 31 * result + (int) (value ^ (value >>> 32));
        value = Double.doubleToLongBits(jumpMultiplier);
        result = 31 * result + (int) (value ^ (value >>> 32));
        return result;
    }
}

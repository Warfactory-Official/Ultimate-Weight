package com.warfactory.ultimateweight.core;

public final class WeightUpdate {
    public enum ScanType {
        NONE,
        DELTA,
        FULL
    }

    private final WeightSnapshot snapshot;
    private final ScanType scanType;
    private final boolean recalculated;
    private final boolean weightChanged;
    private final boolean thresholdChanged;

    public WeightUpdate(
        WeightSnapshot snapshot,
        boolean recalculated,
        boolean weightChanged,
        boolean thresholdChanged
    ) {
        this(
            snapshot,
            recalculated ? ScanType.FULL : ScanType.NONE,
            recalculated,
            weightChanged,
            thresholdChanged
        );
    }

    public WeightUpdate(
        WeightSnapshot snapshot,
        ScanType scanType,
        boolean recalculated,
        boolean weightChanged,
        boolean thresholdChanged
    ) {
        this.snapshot = snapshot;
        this.scanType = scanType == null ? ScanType.NONE : scanType;
        this.recalculated = recalculated;
        this.weightChanged = weightChanged;
        this.thresholdChanged = thresholdChanged;
    }

    public WeightSnapshot snapshot() {
        return snapshot;
    }

    public ScanType scanType() {
        return scanType;
    }

    public boolean updated() {
        return scanType != ScanType.NONE;
    }

    public boolean deltaScan() {
        return scanType == ScanType.DELTA;
    }

    public boolean fullScan() {
        return scanType == ScanType.FULL;
    }

    public boolean recalculated() {
        return recalculated;
    }

    public boolean weightChanged() {
        return weightChanged;
    }

    public boolean thresholdChanged() {
        return thresholdChanged;
    }
}

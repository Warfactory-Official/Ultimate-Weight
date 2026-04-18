package com.warfactory.ultimateweight.core;

import com.warfactory.ultimateweight.api.WeightPlayerView;
import com.warfactory.ultimateweight.config.WeightConfig;

import java.util.HashMap;
import java.util.Map;

public final class PlayerWeightTracker {
    private static final double EPSILON = 0.000001D;

    private final WeightConfig config;
    private final WeightInventoryCalculator inventoryCalculator;
    private final Map<String, TrackedState> states = new HashMap<String, TrackedState>();

    public PlayerWeightTracker(WeightConfig config, WeightInventoryCalculator inventoryCalculator) {
        this.config = config;
        this.inventoryCalculator = inventoryCalculator;
    }

    public synchronized void markDirty(String playerId) {
        TrackedState state = states.get(playerId);
        if (state == null) {
            states.put(playerId, new TrackedState(null, true, Long.MIN_VALUE));
        } else {
            state.dirty = true;
        }
    }

    public synchronized void clear(String playerId) {
        states.remove(playerId);
    }

    public synchronized WeightUpdate refresh(WeightPlayerView player, long gameTick) {
        TrackedState state = states.get(player.playerId());
        if (state == null) {
            state = new TrackedState(null, true, Long.MIN_VALUE);
            states.put(player.playerId(), state);
        }

        boolean fullScanDue = config.enableFailsafeFullScan()
            && (state.lastFullScanTick == Long.MIN_VALUE
            || gameTick - state.lastFullScanTick >= config.fullScanIntervalTicks());
        if (!state.dirty && !fullScanDue && state.snapshot != null) {
            return new WeightUpdate(state.snapshot, false, false, false);
        }

        double carryCapacityKg = player.carryCapacityKg() > 0.0D
            ? player.carryCapacityKg()
            : config.defaultCarryCapacityKg();
        double totalWeightKg = inventoryCalculator.calculateTotalWeightKg(player.inventory());
        ThresholdEffect thresholdEffect = resolveThreshold(totalWeightKg, carryCapacityKg);
        WeightSnapshot snapshot = new WeightSnapshot(
            totalWeightKg,
            carryCapacityKg,
            thresholdEffect,
            totalWeightKg >= config.hardLockWeightKg()
        );

        WeightSnapshot previous = state.snapshot;
        state.snapshot = snapshot;
        state.dirty = false;
        state.lastFullScanTick = gameTick;

        return new WeightUpdate(
            snapshot,
            WeightUpdate.ScanType.FULL,
            true,
            previous == null || changed(previous.totalWeightKg(), snapshot.totalWeightKg())
                || changed(previous.carryCapacityKg(), snapshot.carryCapacityKg())
                || previous.hardLocked() != snapshot.hardLocked(),
            previous == null || !previous.thresholdEffect().equals(snapshot.thresholdEffect())
        );
    }

    public synchronized WeightUpdate applyDelta(
        WeightPlayerView player,
        double previousStackWeightKg,
        double newStackWeightKg,
        long gameTick
    ) {
        TrackedState state = states.get(player.playerId());
        if (state == null || state.snapshot == null || state.dirty) {
            return new WeightUpdate(state == null ? null : state.snapshot, false, false, false);
        }

        double carryCapacityKg = player.carryCapacityKg() > 0.0D
            ? player.carryCapacityKg()
            : config.defaultCarryCapacityKg();
        double totalWeightKg = Math.max(
            0.0D,
            state.snapshot.totalWeightKg() + (newStackWeightKg - previousStackWeightKg)
        );
        ThresholdEffect thresholdEffect = resolveThreshold(totalWeightKg, carryCapacityKg);
        WeightSnapshot snapshot = new WeightSnapshot(
            totalWeightKg,
            carryCapacityKg,
            thresholdEffect,
            totalWeightKg >= config.hardLockWeightKg()
        );

        WeightSnapshot previous = state.snapshot;
        state.snapshot = snapshot;

        return new WeightUpdate(
            snapshot,
            WeightUpdate.ScanType.DELTA,
            false,
            previous == null || changed(previous.totalWeightKg(), snapshot.totalWeightKg())
                || changed(previous.carryCapacityKg(), snapshot.carryCapacityKg())
                || previous.hardLocked() != snapshot.hardLocked(),
            previous == null || !previous.thresholdEffect().equals(snapshot.thresholdEffect())
        );
    }

    public boolean canAcceptAdditionalWeight(WeightSnapshot snapshot, double additionalWeightKg) {
        return snapshot.totalWeightKg() + additionalWeightKg < config.hardLockWeightKg();
    }

    private ThresholdEffect resolveThreshold(double totalWeightKg, double carryCapacityKg) {
        double loadPercent = carryCapacityKg <= 0.0D ? 0.0D : totalWeightKg / carryCapacityKg;
        ThresholdEffect effect = ThresholdEffect.defaults(loadPercent);

        for (int index = 0; index < config.thresholds().size(); index++) {
            WeightConfig.ThresholdRule rule = config.thresholds().get(index);
            if (loadPercent >= rule.percent()) {
                effect = new ThresholdEffect(
                    index,
                    loadPercent,
                    rule.speedMultiplier(),
                    rule.jumpMultiplier()
                );
            }
        }
        return effect;
    }

    private static boolean changed(double previous, double current) {
        return Math.abs(previous - current) > EPSILON;
    }

    private static final class TrackedState {
        private WeightSnapshot snapshot;
        private boolean dirty;
        private long lastFullScanTick;

        private TrackedState(WeightSnapshot snapshot, boolean dirty, long lastFullScanTick) {
            this.snapshot = snapshot;
            this.dirty = dirty;
            this.lastFullScanTick = lastFullScanTick;
        }
    }
}

package com.warfactory.ultimateweight.core;

public final class WeightResolutionContext {
    private static final ThreadLocal<Integer> DEPTH = new ThreadLocal<Integer>();

    private WeightResolutionContext() {
    }

    public static int currentDepth() {
        Integer depth = DEPTH.get();
        return depth == null ? 0 : depth.intValue();
    }

    public static Integer setDepth(int depth) {
        Integer previous = DEPTH.get();
        DEPTH.set(Integer.valueOf(depth));
        return previous;
    }

    public static void restoreDepth(Integer previous) {
        if (previous == null) {
            DEPTH.remove();
            return;
        }
        DEPTH.set(previous);
    }
}

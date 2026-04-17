package com.warfactory.ultimateweight.core;

public final class ResolvedWeight {
    private final double singleItemWeightKg;
    private final double stackWeightKg;
    private final Source source;
    private final String matchedKey;

    public ResolvedWeight(double singleItemWeightKg, int count, Source source, String matchedKey) {
        this.singleItemWeightKg = singleItemWeightKg;
        this.stackWeightKg = singleItemWeightKg * Math.max(0, count);
        this.source = source;
        this.matchedKey = matchedKey;
    }

    public double singleItemWeightKg() {
        return singleItemWeightKg;
    }

    public double stackWeightKg() {
        return stackWeightKg;
    }

    public Source source() {
        return source;
    }

    public String matchedKey() {
        return matchedKey;
    }

    public enum Source {
        COMPONENT_OVERRIDE,
        EXACT_ITEM,
        GROUP_MATCH,
        PREFIX_MATCH,
        DEFAULT
    }
}

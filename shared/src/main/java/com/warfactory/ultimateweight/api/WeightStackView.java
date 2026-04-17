package com.warfactory.ultimateweight.api;

public interface WeightStackView {
    WeightItemView item();

    int count();

    default int metadata() {
        return 0;
    }

    default int resolutionDepth() {
        return 0;
    }

    default String complexCacheKey() {
        return null;
    }

    default Object unwrap() {
        return null;
    }

    default WeightDataView data() {
        return WeightDataView.empty();
    }
}

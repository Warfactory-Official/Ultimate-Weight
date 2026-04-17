package com.warfactory.ultimateweight.api;

public interface WeightStackView {
    WeightItemView item();

    int count();

    default WeightDataView data() {
        return WeightDataView.empty();
    }
}

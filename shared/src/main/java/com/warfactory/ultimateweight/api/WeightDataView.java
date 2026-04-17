package com.warfactory.ultimateweight.api;

public interface WeightDataView {
    WeightDataView EMPTY = new WeightDataView() {
        @Override
        public Double getDouble(String key) {
            return null;
        }
    };

    Double getDouble(String key);

    static WeightDataView empty() {
        return EMPTY;
    }
}

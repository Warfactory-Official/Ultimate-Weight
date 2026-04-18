package com.warfactory.ultimateweight.api;

import java.util.OptionalDouble;

public interface IWeightCompatProvider {
    OptionalDouble getUnitWeight(Object stack);

    int getPriority();
}

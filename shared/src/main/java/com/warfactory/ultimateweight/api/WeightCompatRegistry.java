package com.warfactory.ultimateweight.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class WeightCompatRegistry {
    private static final List<IWeightCompatProvider> PROVIDERS = new ArrayList<IWeightCompatProvider>();
    private static final Comparator<IWeightCompatProvider> PRIORITY_ORDER = new Comparator<IWeightCompatProvider>() {
        @Override
        public int compare(IWeightCompatProvider left, IWeightCompatProvider right) {
            int priority = Integer.compare(right.getPriority(), left.getPriority());
            if (priority != 0) {
                return priority;
            }
            return left.getClass().getName().compareTo(right.getClass().getName());
        }
    };

    private WeightCompatRegistry() {
    }

    public static synchronized void register(IWeightCompatProvider provider) {
        if (provider == null) {
            return;
        }
        PROVIDERS.add(provider);
        Collections.sort(PROVIDERS, PRIORITY_ORDER);
    }

    public static synchronized void registerAll(Iterable<? extends IWeightCompatProvider> providers) {
        if (providers == null) {
            return;
        }
        for (IWeightCompatProvider provider : providers) {
            register(provider);
        }
    }

    public static synchronized List<IWeightCompatProvider> providers() {
        return new ArrayList<IWeightCompatProvider>(PROVIDERS);
    }

    public static synchronized void clear() {
        PROVIDERS.clear();
    }
}

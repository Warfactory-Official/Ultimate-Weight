package com.warfactory.ultimateweight.api;

import java.util.Collection;
import java.util.Collections;

public interface WeightItemView {
    String itemId();

    default String itemPath() {
        String itemId = itemId();
        int separator = itemId.indexOf(':');
        return separator >= 0 ? itemId.substring(separator + 1) : itemId;
    }

    default Collection<String> matchKeys() {
        return Collections.emptyList();
    }
}

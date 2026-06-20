package com.warfactory.ultimateweight.compat;

import java.util.Arrays;

/**
 * One entry from the build-time compat plugin index: the class to load plus the gating metadata
 * read off its {@code @CompatPlugin} annotation. Carrying the gating here lets the bootstrap decide
 * whether to load a plugin without first loading its class.
 */
public final class DiscoveredPlugin {
    private final String className;
    private final String[] requiredMods;
    private final String[] anyOf;
    private final int priority;
    private final String id;

    public DiscoveredPlugin(String className, String[] requiredMods, String[] anyOf, int priority, String id) {
        this.className = className;
        this.requiredMods = requiredMods == null ? EMPTY : requiredMods;
        this.anyOf = anyOf == null ? EMPTY : anyOf;
        this.priority = priority;
        this.id = id == null || id.isEmpty() ? simpleName(className) : id;
    }

    private static final String[] EMPTY = new String[0];

    public String className() {
        return className;
    }

    public String[] requiredMods() {
        return requiredMods;
    }

    public String[] anyOf() {
        return anyOf;
    }

    public int priority() {
        return priority;
    }

    public String id() {
        return id;
    }

    private static String simpleName(String className) {
        if (className == null) {
            return "";
        }
        int dot = className.lastIndexOf('.');
        return dot >= 0 ? className.substring(dot + 1) : className;
    }

    @Override
    public String toString() {
        return "DiscoveredPlugin{" + className
            + ", requiredMods=" + Arrays.toString(requiredMods)
            + ", anyOf=" + Arrays.toString(anyOf)
            + ", priority=" + priority + '}';
    }
}

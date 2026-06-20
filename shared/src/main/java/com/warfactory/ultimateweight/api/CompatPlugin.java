package com.warfactory.ultimateweight.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an auto-discovered compatibility plugin.
 *
 * <p>A plugin is found at build time by the compat annotation processor, which records the
 * annotated class (and the gating metadata below) into a {@code META-INF/wfweight/compat-plugins.txt}
 * index bundled in the jar. At load time {@link com.warfactory.ultimateweight.compat.WeightCompatBootstrap}
 * reads that index, keeps only the plugins whose required mods are present, and then instantiates
 * them - so a plugin that references classes from an absent mod is never loaded.
 *
 * <p>The annotated class must have a public no-arg constructor and implement one of:
 * <ul>
 *     <li>{@link IWeightCompatProvider} - auto-registered as a weight provider (the common case,
 *     zero extra wiring); or</li>
 *     <li>{@link WeightCompatPlugin} - its {@link WeightCompatPlugin#register(WeightCompatContext)}
 *     runs so it can contribute inventory sources, dynamic-container markers, etc.</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface CompatPlugin {
    /**
     * Mod ids that must <em>all</em> be loaded for this plugin to activate. Empty means
     * "always active" (e.g. the generic nested-container provider).
     */
    String[] requiredMods() default {};

    /**
     * Mod ids of which <em>at least one</em> must be loaded for this plugin to activate. Empty
     * means "no such constraint". Combined with {@link #requiredMods()} via AND. Use this for
     * support shared between several mods, e.g. a worn-slot source that any of Curios or
     * Traveler's Backpack should enable.
     */
    String[] anyOf() default {};

    /**
     * Load order among plugins; higher runs first. Governs the order {@code register(..)} is
     * invoked. Note that {@link IWeightCompatProvider}'s own {@code getPriority()} still governs
     * weight-resolution order, independent of this value.
     */
    int priority() default 0;

    /**
     * Stable identifier used in debug logging. Defaults to the simple class name when blank.
     */
    String id() default "";
}

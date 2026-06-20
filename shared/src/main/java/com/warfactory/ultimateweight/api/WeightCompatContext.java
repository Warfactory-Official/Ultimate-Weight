package com.warfactory.ultimateweight.api;

/**
 * The registration surface handed to a {@link WeightCompatPlugin}. This shared base exposes only
 * the platform-independent capability - registering a weight provider - plus a mod-presence query.
 *
 * <p>Each Minecraft version supplies a concrete subtype (e.g. {@code CompatContext1201}) that adds
 * typed methods for version-specific hooks like inventory sources and dynamic-container markers.
 * Plugins that need those cast the context to the version subtype.
 */
public interface WeightCompatContext {
    /**
     * Register an additional per-item weight provider. Equivalent to
     * {@link WeightCompatRegistry#register(IWeightCompatProvider)}; provided here so plugins do not
     * touch the static registry directly.
     */
    void registerWeightProvider(IWeightCompatProvider provider);

    /**
     * @return whether the given mod id is loaded, per the active loader.
     */
    boolean isModLoaded(String modId);
}

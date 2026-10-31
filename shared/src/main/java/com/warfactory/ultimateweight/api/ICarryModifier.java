package com.warfactory.ultimateweight.api;

/**
 * Something that raises or lowers how much a player carries before the overweight thresholds: an artefact, a drug.
 * Registered with {@link CarryModifiers}; asked whenever the player's capacity is resolved, on the server thread.
 */
@FunctionalInterface
public interface ICarryModifier {
    /**
     * Kilograms added to this player's carry capacity; negative lowers it.
     *
     * @param player the loader's player object (a {@code ServerPlayer} on modern versions)
     */
    double bonusKg(Object player);
}

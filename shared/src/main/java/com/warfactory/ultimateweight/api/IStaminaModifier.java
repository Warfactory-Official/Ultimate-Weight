package com.warfactory.ultimateweight.api;

/**
 * Something that speeds or slows a player's stamina recovery: an artefact, a drug. Registered with
 * {@link StaminaModifiers}; asked every tick the player's stamina updates, on the server thread.
 */
@FunctionalInterface
public interface IStaminaModifier {
    /**
     * Stamina this player gets back this tick on top of the configured gain, as a share of their
     * maximum (0.004 = 0.4% of the bar); negative slows recovery, and can drain. Applies whether they
     * are resting or sprinting.
     *
     * @param player the loader's player object (a {@code ServerPlayer} on modern versions)
     */
    double recoveryPerTick(Object player);
}

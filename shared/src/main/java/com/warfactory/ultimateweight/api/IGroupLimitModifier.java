package com.warfactory.ultimateweight.api;

/**
 * Raises or lowers an inventory group's item-count limit for a player (powered armour, a rig).
 * Registered with {@link GroupLimitModifiers}; asked whenever the player's group limits are resolved.
 */
@FunctionalInterface
public interface IGroupLimitModifier {
    /**
     * @param player  the loader's player object
     * @param groupId a configured {@code groupLimits} id
     * @return items added to the group's limit; negative lowers it
     */
    int bonus(Object player, String groupId);
}

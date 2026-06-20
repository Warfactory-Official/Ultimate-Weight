package com.warfactory.ultimateweight.api;

/**
 * The richer compatibility plugin contract. Implement this (instead of, or in addition to,
 * {@link IWeightCompatProvider}) when a plugin needs to contribute more than a single weight
 * provider - for example registering an extra inventory source (Baubles/Curios slots, a worn
 * capability backpack) or marking an item as a dynamic container.
 *
 * <p>Implementations must be annotated with {@link CompatPlugin} and have a public no-arg
 * constructor. {@link #register(WeightCompatContext)} is invoked once at startup, only when the
 * plugin's mod gating is satisfied.
 */
public interface WeightCompatPlugin {
    /**
     * Contribute this plugin's compatibility hooks. The supplied context is the platform-specific
     * implementation; cast it to the version's context type (e.g. {@code CompatContext1201}) to
     * reach typed registration methods such as {@code registerInventorySource(..)}.
     */
    void register(WeightCompatContext context);
}

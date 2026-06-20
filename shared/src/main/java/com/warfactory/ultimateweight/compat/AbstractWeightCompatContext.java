package com.warfactory.ultimateweight.compat;

import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatRegistry;

/**
 * Base {@link WeightCompatContext} that wires the platform-independent capability - weight provider
 * registration via {@link WeightCompatRegistry} - and mod presence through a {@link ModPresenceChecker}.
 * Each Minecraft version extends this to add its typed hooks (inventory sources, dynamic containers).
 */
public abstract class AbstractWeightCompatContext implements WeightCompatContext {
    private final ModPresenceChecker modPresence;

    protected AbstractWeightCompatContext(ModPresenceChecker modPresence) {
        this.modPresence = modPresence;
    }

    @Override
    public void registerWeightProvider(IWeightCompatProvider provider) {
        WeightCompatRegistry.register(provider);
    }

    @Override
    public boolean isModLoaded(String modId) {
        return modPresence != null && modPresence.isModLoaded(modId);
    }
}

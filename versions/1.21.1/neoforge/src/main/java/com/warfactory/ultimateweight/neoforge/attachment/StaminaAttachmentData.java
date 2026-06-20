package com.warfactory.ultimateweight.neoforge.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Persisted stamina state, stored as a NeoForge data attachment on the player (the 1.20.1 forge
 * {@code IPlayerWeightData} capability analogue). Only the stamina fields need to survive death /
 * dimension change - weight is recomputed from the live inventory on (re)join.
 */
public record StaminaAttachmentData(
    double currentStamina,
    double maxStamina,
    boolean staminaEnabled,
    boolean exhausted
) {
    public static final StaminaAttachmentData EMPTY = new StaminaAttachmentData(0.0D, 0.0D, false, false);

    public static final Codec<StaminaAttachmentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.DOUBLE.fieldOf("current").forGetter(StaminaAttachmentData::currentStamina),
        Codec.DOUBLE.fieldOf("max").forGetter(StaminaAttachmentData::maxStamina),
        Codec.BOOL.fieldOf("enabled").forGetter(StaminaAttachmentData::staminaEnabled),
        Codec.BOOL.fieldOf("exhausted").forGetter(StaminaAttachmentData::exhausted)
    ).apply(instance, StaminaAttachmentData::new));
}

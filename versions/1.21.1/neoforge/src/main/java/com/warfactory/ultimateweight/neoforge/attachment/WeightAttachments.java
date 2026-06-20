package com.warfactory.ultimateweight.neoforge.attachment;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class WeightAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, UltimateWeightCommon.MOD_ID);

    public static final Supplier<AttachmentType<StaminaAttachmentData>> STAMINA =
        ATTACHMENT_TYPES.register(
            "stamina",
            () -> AttachmentType.builder(() -> StaminaAttachmentData.EMPTY)
                .serialize(StaminaAttachmentData.CODEC)
                .copyOnDeath()
                .build()
        );

    private WeightAttachments() {
    }
}

package com.warfactory.ultimateweight.fabric;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.core.WeightSnapshot;
import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Bridges the common runtime's player-state callbacks to a Fabric data attachment so stamina
 * survives logout, death and dimension change. Weight effects are transient (recomputed on rejoin),
 * so only stamina is persisted - mirroring the NeoForge state hooks.
 */
public final class UltimateWeightFabricStateHooks implements UltimateWeight1211.PlayerStateListener {
    private static final double EPSILON = 0.000001D;

    /**
     * {@code persistent} writes the stamina to the player's save data (survives logout);
     * {@code copyOnDeath} carries it through a death respawn. End-portal returns are handled by the
     * explicit {@link #onClone} copy wired to {@code ServerPlayerEvents.COPY_FROM}.
     */
    public static final AttachmentType<StaminaAttachmentData> STAMINA = AttachmentRegistry
        .<StaminaAttachmentData>builder()
        .persistent(StaminaAttachmentData.CODEC)
        .copyOnDeath()
        .buildAndRegister(ResourceLocation.fromNamespaceAndPath(UltimateWeightCommon.MOD_ID, "stamina"));

    @Override
    public void onSnapshot(ServerPlayer player, WeightSnapshot snapshot, boolean effectImmune) {
        // Weight/threshold state is recomputed from the live inventory each (re)join, so nothing to
        // persist here. Hook retained for parity / future external exposure.
    }

    @Override
    public void onClone(Player original, Player clone) {
        StaminaAttachmentData data = original.getAttached(STAMINA);
        if (data != null) {
            clone.setAttached(STAMINA, data);
        }
    }

    @Override
    public void onPlayerLeave(ServerPlayer player) {
    }

    @Override
    public void onStamina(
        ServerPlayer player,
        double currentStamina,
        double maxStamina,
        boolean staminaEnabled,
        boolean exhausted
    ) {
        player.setAttached(STAMINA, new StaminaAttachmentData(currentStamina, maxStamina, staminaEnabled, exhausted));
    }

    @Override
    public UltimateWeight1211.StaminaState loadStamina(Player player) {
        StaminaAttachmentData data = player.getAttached(STAMINA);
        if (data == null || data.maxStamina() <= EPSILON) {
            return null;
        }
        return new UltimateWeight1211.StaminaState(
            data.currentStamina(),
            data.maxStamina(),
            data.staminaEnabled(),
            data.exhausted()
        );
    }
}

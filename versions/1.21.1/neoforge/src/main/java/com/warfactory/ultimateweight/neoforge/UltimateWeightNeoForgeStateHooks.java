package com.warfactory.ultimateweight.neoforge;

import com.warfactory.ultimateweight.core.WeightSnapshot;
import com.warfactory.ultimateweight.neoforge.attachment.StaminaAttachmentData;
import com.warfactory.ultimateweight.neoforge.attachment.WeightAttachments;
import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Bridges the common runtime's player-state callbacks to NeoForge data attachments so stamina
 * survives death and dimension change. Weight effects are transient (recomputed on rejoin), so only
 * stamina is persisted.
 */
public final class UltimateWeightNeoForgeStateHooks implements UltimateWeight1211.PlayerStateListener {
    private static final double EPSILON = 0.000001D;

    @Override
    public void onSnapshot(ServerPlayer player, WeightSnapshot snapshot, boolean effectImmune) {
        // Weight/threshold state is recomputed from the live inventory each (re)join, so nothing to
        // persist here. Hook retained for parity / future external exposure.
    }

    @Override
    public void onClone(Player original, Player clone) {
        // copyOnDeath() handles death respawn; copy explicitly so end-portal returns keep stamina too.
        clone.setData(WeightAttachments.STAMINA.get(), original.getData(WeightAttachments.STAMINA.get()));
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
        player.setData(
            WeightAttachments.STAMINA.get(),
            new StaminaAttachmentData(currentStamina, maxStamina, staminaEnabled, exhausted)
        );
    }

    @Override
    public UltimateWeight1211.StaminaState loadStamina(Player player) {
        StaminaAttachmentData data = player.getData(WeightAttachments.STAMINA.get());
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

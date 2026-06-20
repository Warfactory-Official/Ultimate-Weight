package com.warfactory.ultimateweight.lexforge;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.core.WeightSnapshot;
import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Persists stamina across death / dimension change via Forge's {@code Entity.getPersistentData()}
 * (MinecraftForge 1.21.1 has no NeoForge-style data attachments). The death/end-portal respawn does
 * not carry persistent data automatically, so {@link #onClone} copies the sub-tag explicitly. Weight
 * effects are transient (recomputed on rejoin), so only stamina is stored.
 */
public final class UltimateWeightLexForgeStateHooks implements UltimateWeight1211.PlayerStateListener {
    private static final String STAMINA_TAG = UltimateWeightCommon.MOD_ID + "_stamina";
    private static final double EPSILON = 0.000001D;

    @Override
    public void onSnapshot(ServerPlayer player, WeightSnapshot snapshot, boolean effectImmune) {
    }

    @Override
    public void onClone(Player original, Player clone) {
        CompoundTag originalData = original.getPersistentData();
        if (originalData.contains(STAMINA_TAG, Tag.TAG_COMPOUND)) {
            clone.getPersistentData().put(STAMINA_TAG, originalData.getCompound(STAMINA_TAG).copy());
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
        CompoundTag stamina = new CompoundTag();
        stamina.putDouble("current", currentStamina);
        stamina.putDouble("max", maxStamina);
        stamina.putBoolean("enabled", staminaEnabled);
        stamina.putBoolean("exhausted", exhausted);
        player.getPersistentData().put(STAMINA_TAG, stamina);
    }

    @Override
    public UltimateWeight1211.StaminaState loadStamina(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(STAMINA_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag stamina = root.getCompound(STAMINA_TAG);
        if (stamina.getDouble("max") <= EPSILON) {
            return null;
        }
        return new UltimateWeight1211.StaminaState(
            stamina.getDouble("current"),
            stamina.getDouble("max"),
            stamina.getBoolean("enabled"),
            stamina.getBoolean("exhausted")
        );
    }
}

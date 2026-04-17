package com.warfactory.ultimateweight.forge;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.forge.capability.PlayerWeightProvider1201;
import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltimateWeightCommon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class UltimateWeightForgeEvents {
    private static final ResourceLocation PLAYER_WEIGHT_KEY = new ResourceLocation(UltimateWeightCommon.MOD_ID, "player_weight");

    private UltimateWeightForgeEvents() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ServerPlayer || event.getObject() instanceof net.minecraft.client.player.LocalPlayer || event.getObject() instanceof net.minecraft.world.entity.player.Player) {
            PlayerWeightProvider1201 provider = new PlayerWeightProvider1201();
            event.addCapability(PLAYER_WEIGHT_KEY, provider);
            event.addListener(provider::invalidate);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            UltimateWeight1201.onServerTick(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UltimateWeight1201.onPlayerJoin(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UltimateWeight1201.onPlayerLeave(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UltimateWeight1201.onPlayerJoin(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        UltimateWeight1201.onPlayerClone(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UltimateWeight1201.onPlayerJoin(player);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            event.setDamageMultiplier(
                UltimateWeight1201.adjustFallDamageMultiplier(player, event.getDamageMultiplier())
            );
        }
    }
}

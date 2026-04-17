package com.warfactory.ultimateweight.fabric;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import com.warfactory.ultimateweight.v1201.UltimateWeightConfigFile1201;
import com.warfactory.ultimateweight.v1201.WeightSyncTransport1201;
import com.warfactory.ultimateweight.v1201.network.ConfigFragmentPacket1201;
import com.warfactory.ultimateweight.v1201.network.StaminaUpdatePacket1201;
import com.warfactory.ultimateweight.v1201.network.WeightUpdatePacket1201;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class UltimateWeightFabric implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    @Override
    public void onInitialize() {
        UltimateWeightConfigFile1201.configure(FabricLoader.getInstance().getConfigDir());
        UltimateWeight1201.setTransport(new FabricTransport());

        ServerTickEvents.END_SERVER_TICK.register(UltimateWeight1201::onServerTick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> UltimateWeight1201.onPlayerJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> UltimateWeight1201.onPlayerLeave(handler.player));

        LOGGER.info("{} 1.20.1 Fabric integration initialized.", UltimateWeightCommon.MOD_NAME);
    }

    private static final class FabricTransport implements WeightSyncTransport1201 {
        @Override
        public void sendConfigFragment(ServerPlayer player, ConfigFragmentPacket1201 packet) {
            FriendlyByteBuf buffer = PacketByteBufs.create();
            packet.encode(buffer);
            ServerPlayNetworking.send(player, UltimateWeight1201.CONFIG_FRAGMENT_ID, buffer);
        }

        @Override
        public void sendWeightUpdate(ServerPlayer player, WeightUpdatePacket1201 packet) {
            FriendlyByteBuf buffer = PacketByteBufs.create();
            packet.encode(buffer);
            ServerPlayNetworking.send(player, UltimateWeight1201.WEIGHT_UPDATE_ID, buffer);
        }

        @Override
        public void sendStaminaUpdate(ServerPlayer player, StaminaUpdatePacket1201 packet) {
            FriendlyByteBuf buffer = PacketByteBufs.create();
            packet.encode(buffer);
            ServerPlayNetworking.send(player, UltimateWeight1201.STAMINA_UPDATE_ID, buffer);
        }
    }
}

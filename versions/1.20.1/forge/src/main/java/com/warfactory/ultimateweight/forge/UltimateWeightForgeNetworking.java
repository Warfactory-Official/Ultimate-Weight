package com.warfactory.ultimateweight.forge;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import com.warfactory.ultimateweight.v1201.WeightSyncTransport1201;
import com.warfactory.ultimateweight.v1201.network.ConfigFragmentPacket1201;
import com.warfactory.ultimateweight.v1201.network.StaminaUpdatePacket1201;
import com.warfactory.ultimateweight.v1201.network.WeightUpdatePacket1201;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class UltimateWeightForgeNetworking {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(UltimateWeightCommon.MOD_ID, "main"),
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    private static boolean bootstrapped;

    private UltimateWeightForgeNetworking() {
    }

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;

        CHANNEL.messageBuilder(ConfigFragmentPacket1201.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(ConfigFragmentPacket1201::encode)
            .decoder(ConfigFragmentPacket1201::decode)
            .consumerMainThread((packet, contextSupplier) -> UltimateWeight1201.receiveConfigFragment(packet))
            .add();
        CHANNEL.messageBuilder(WeightUpdatePacket1201.class, 1, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(WeightUpdatePacket1201::encode)
            .decoder(WeightUpdatePacket1201::decode)
            .consumerMainThread((packet, contextSupplier) -> UltimateWeight1201.receiveWeightUpdate(packet))
            .add();
        CHANNEL.messageBuilder(StaminaUpdatePacket1201.class, 2, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(StaminaUpdatePacket1201::encode)
            .decoder(StaminaUpdatePacket1201::decode)
            .consumerMainThread((packet, contextSupplier) -> UltimateWeight1201.receiveStaminaUpdate(packet))
            .add();

        UltimateWeight1201.setTransport(new ForgeTransport());
    }

    private static final class ForgeTransport implements WeightSyncTransport1201 {
        @Override
        public void sendConfigFragment(ServerPlayer player, ConfigFragmentPacket1201 packet) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }

        @Override
        public void sendWeightUpdate(ServerPlayer player, WeightUpdatePacket1201 packet) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }

        @Override
        public void sendStaminaUpdate(ServerPlayer player, StaminaUpdatePacket1201 packet) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }
}

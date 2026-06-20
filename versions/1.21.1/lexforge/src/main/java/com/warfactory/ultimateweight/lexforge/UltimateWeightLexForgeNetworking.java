package com.warfactory.ultimateweight.lexforge;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import com.warfactory.ultimateweight.v1211.WeightSyncTransport1211;
import com.warfactory.ultimateweight.v1211.network.ConfigFragmentPacket1211;
import com.warfactory.ultimateweight.v1211.network.StaminaUpdatePacket1211;
import com.warfactory.ultimateweight.v1211.network.WeightUpdatePacket1211;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

/**
 * MinecraftForge 1.21.1 networking. Unlike NeoForge's {@code PayloadRegistrar}, Forge keeps a
 * {@link SimpleChannel} built through {@link ChannelBuilder}. Each shared {@code *Packet1211} carries
 * its own encode/decode; all three are server-to-client only.
 */
public final class UltimateWeightLexForgeNetworking {
    private static final SimpleChannel CHANNEL = ChannelBuilder
        .named(ResourceLocation.fromNamespaceAndPath(UltimateWeightCommon.MOD_ID, "main"))
        .networkProtocolVersion(1)
        .acceptedVersions(Channel.VersionTest.exact(1))
        .simpleChannel();

    private static boolean bootstrapped;

    private UltimateWeightLexForgeNetworking() {
    }

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;

        CHANNEL.messageBuilder(ConfigFragmentPacket1211.class, 0)
            .direction(PacketFlow.CLIENTBOUND)
            .encoder(ConfigFragmentPacket1211::encode)
            .decoder(ConfigFragmentPacket1211::decode)
            .consumerMainThread((packet, context) -> {
                UltimateWeight1211.receiveConfigFragment(packet);
                context.setPacketHandled(true);
            })
            .add();
        CHANNEL.messageBuilder(WeightUpdatePacket1211.class, 1)
            .direction(PacketFlow.CLIENTBOUND)
            .encoder(WeightUpdatePacket1211::encode)
            .decoder(WeightUpdatePacket1211::decode)
            .consumerMainThread((packet, context) -> {
                UltimateWeight1211.receiveWeightUpdate(packet);
                context.setPacketHandled(true);
            })
            .add();
        CHANNEL.messageBuilder(StaminaUpdatePacket1211.class, 2)
            .direction(PacketFlow.CLIENTBOUND)
            .encoder(StaminaUpdatePacket1211::encode)
            .decoder(StaminaUpdatePacket1211::decode)
            .consumerMainThread((packet, context) -> {
                UltimateWeight1211.receiveStaminaUpdate(packet);
                context.setPacketHandled(true);
            })
            .add();

        UltimateWeight1211.setTransport(new LexForgeTransport());
    }

    private static final class LexForgeTransport implements WeightSyncTransport1211 {
        @Override
        public void sendConfigFragment(ServerPlayer player, ConfigFragmentPacket1211 packet) {
            CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
        }

        @Override
        public void sendWeightUpdate(ServerPlayer player, WeightUpdatePacket1211 packet) {
            CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
        }

        @Override
        public void sendStaminaUpdate(ServerPlayer player, StaminaUpdatePacket1211 packet) {
            CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
        }
    }
}

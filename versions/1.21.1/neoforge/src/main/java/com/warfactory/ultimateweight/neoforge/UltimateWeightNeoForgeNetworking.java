package com.warfactory.ultimateweight.neoforge;

import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import com.warfactory.ultimateweight.v1211.WeightSyncTransport1211;
import com.warfactory.ultimateweight.v1211.network.ConfigFragmentPacket1211;
import com.warfactory.ultimateweight.v1211.network.StaminaUpdatePacket1211;
import com.warfactory.ultimateweight.v1211.network.WeightUpdatePacket1211;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge 1.21.1 networking. The old {@code SimpleChannel} is gone; payloads are registered through
 * the {@link PayloadRegistrar} and dispatched with {@link PacketDistributor}. Each shared packet is
 * wrapped in a thin {@link CustomPacketPayload} record that delegates to the packet's own
 * encode/decode (kept loader-agnostic in common). All three are server-to-client only.
 */
public final class UltimateWeightNeoForgeNetworking {
    private UltimateWeightNeoForgeNetworking() {
    }

    /** Mod-bus listener: register the three S2C payloads. */
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
            ConfigFragmentPayload.TYPE,
            ConfigFragmentPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> UltimateWeight1211.receiveConfigFragment(payload.packet()))
        );
        registrar.playToClient(
            WeightUpdatePayload.TYPE,
            WeightUpdatePayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> UltimateWeight1211.receiveWeightUpdate(payload.packet()))
        );
        registrar.playToClient(
            StaminaUpdatePayload.TYPE,
            StaminaUpdatePayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> UltimateWeight1211.receiveStaminaUpdate(payload.packet()))
        );
    }

    public static void installTransport() {
        UltimateWeight1211.setTransport(new NeoForgeTransport());
    }

    private static final class NeoForgeTransport implements WeightSyncTransport1211 {
        @Override
        public void sendConfigFragment(ServerPlayer player, ConfigFragmentPacket1211 packet) {
            PacketDistributor.sendToPlayer(player, new ConfigFragmentPayload(packet));
        }

        @Override
        public void sendWeightUpdate(ServerPlayer player, WeightUpdatePacket1211 packet) {
            PacketDistributor.sendToPlayer(player, new WeightUpdatePayload(packet));
        }

        @Override
        public void sendStaminaUpdate(ServerPlayer player, StaminaUpdatePacket1211 packet) {
            PacketDistributor.sendToPlayer(player, new StaminaUpdatePayload(packet));
        }
    }

    public record ConfigFragmentPayload(ConfigFragmentPacket1211 packet) implements CustomPacketPayload {
        public static final Type<ConfigFragmentPayload> TYPE = new Type<>(UltimateWeight1211.CONFIG_FRAGMENT_ID);
        public static final StreamCodec<FriendlyByteBuf, ConfigFragmentPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> payload.packet().encode(buffer),
            buffer -> new ConfigFragmentPayload(ConfigFragmentPacket1211.decode(buffer))
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record WeightUpdatePayload(WeightUpdatePacket1211 packet) implements CustomPacketPayload {
        public static final Type<WeightUpdatePayload> TYPE = new Type<>(UltimateWeight1211.WEIGHT_UPDATE_ID);
        public static final StreamCodec<FriendlyByteBuf, WeightUpdatePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> payload.packet().encode(buffer),
            buffer -> new WeightUpdatePayload(WeightUpdatePacket1211.decode(buffer))
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record StaminaUpdatePayload(StaminaUpdatePacket1211 packet) implements CustomPacketPayload {
        public static final Type<StaminaUpdatePayload> TYPE = new Type<>(UltimateWeight1211.STAMINA_UPDATE_ID);
        public static final StreamCodec<FriendlyByteBuf, StaminaUpdatePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> payload.packet().encode(buffer),
            buffer -> new StaminaUpdatePayload(StaminaUpdatePacket1211.decode(buffer))
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}

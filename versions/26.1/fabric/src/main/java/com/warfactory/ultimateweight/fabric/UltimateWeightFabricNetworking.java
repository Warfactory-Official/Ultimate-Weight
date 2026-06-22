package com.warfactory.ultimateweight.fabric;

import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import com.warfactory.ultimateweight.v1211.network.ConfigFragmentPacket1211;
import com.warfactory.ultimateweight.v1211.network.StaminaUpdatePacket1211;
import com.warfactory.ultimateweight.v1211.network.WeightUpdatePacket1211;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Fabric 1.21.1 networking. Like NeoForge, the loader-agnostic shared packets are wrapped in thin
 * {@link CustomPacketPayload} records and dispatched through the modern payload API
 * ({@code PacketByteBufs} + a per-id channel are gone since 1.20.5). The payload codecs are
 * registered on the common entrypoint so both client and server resolve them; sending is server-side
 * ({@link UltimateWeightFabricTransport}), receiving client-side
 * ({@link com.warfactory.ultimateweight.fabric.client.UltimateWeightFabricClient}). All three are
 * server-to-client only.
 *
 * <p>The stream codecs are declared over {@link FriendlyByteBuf}; a {@code RegistryFriendlyByteBuf}
 * (which the play channel actually supplies) is a subtype, so the codecs satisfy the
 * {@code ? super RegistryFriendlyByteBuf} bound on {@link PayloadTypeRegistry#playS2C()} while the
 * shared packets keep their plain-buffer encode/decode.
 */
public final class UltimateWeightFabricNetworking {
    private UltimateWeightFabricNetworking() {
    }

    /** Common-side: register the three S2C payload codecs (must run on both client and server). */
    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playS2C().register(ConfigFragmentPayload.TYPE, ConfigFragmentPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(WeightUpdatePayload.TYPE, WeightUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(StaminaUpdatePayload.TYPE, StaminaUpdatePayload.STREAM_CODEC);
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

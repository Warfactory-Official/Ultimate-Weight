package com.warfactory.ultimateweight.v1211.network;

import com.warfactory.ultimateweight.network.ConfigFragment;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public final class ConfigFragmentPacket1211 {
    private final UUID transferId;
    private final int index;
    private final int total;
    private final byte[] payload;

    public ConfigFragmentPacket1211(UUID transferId, int index, int total, byte[] payload) {
        this.transferId = transferId;
        this.index = index;
        this.total = total;
        this.payload = payload;
    }

    public static ConfigFragmentPacket1211 fromFragment(ConfigFragment fragment) {
        return new ConfigFragmentPacket1211(
            fragment.transferId(),
            fragment.index(),
            fragment.total(),
            fragment.payload()
        );
    }

    public static ConfigFragmentPacket1211 decode(FriendlyByteBuf buffer) {
        return new ConfigFragmentPacket1211(
            buffer.readUUID(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readByteArray()
        );
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(transferId);
        buffer.writeVarInt(index);
        buffer.writeVarInt(total);
        buffer.writeByteArray(payload);
    }

    public ConfigFragment toFragment() {
        return new ConfigFragment(transferId, index, total, payload);
    }
}

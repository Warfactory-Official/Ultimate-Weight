package com.warfactory.ultimateweight.v1122.network;

import com.warfactory.ultimateweight.network.ConfigFragment;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public final class PacketConfigFragment1122 implements IMessage {
    private UUID transferId;
    private int index;
    private int total;
    private byte[] payload;

    public PacketConfigFragment1122() {
    }

    public PacketConfigFragment1122(UUID transferId, int index, int total, byte[] payload) {
        this.transferId = transferId;
        this.index = index;
        this.total = total;
        this.payload = payload;
    }

    public static PacketConfigFragment1122 fromFragment(ConfigFragment fragment) {
        return new PacketConfigFragment1122(
            fragment.transferId(),
            fragment.index(),
            fragment.total(),
            fragment.payload()
        );
    }

    public ConfigFragment toFragment() {
        return new ConfigFragment(transferId, index, total, payload);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        transferId = new UUID(buf.readLong(), buf.readLong());
        index = buf.readInt();
        total = buf.readInt();
        payload = new byte[buf.readInt()];
        buf.readBytes(payload);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(transferId.getMostSignificantBits());
        buf.writeLong(transferId.getLeastSignificantBits());
        buf.writeInt(index);
        buf.writeInt(total);
        buf.writeInt(payload.length);
        buf.writeBytes(payload);
    }
}

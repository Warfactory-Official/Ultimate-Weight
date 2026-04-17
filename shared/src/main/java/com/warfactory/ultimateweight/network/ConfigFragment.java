package com.warfactory.ultimateweight.network;

import java.util.Arrays;
import java.util.UUID;

public final class ConfigFragment {
    private final UUID transferId;
    private final int index;
    private final int total;
    private final byte[] payload;

    public ConfigFragment(UUID transferId, int index, int total, byte[] payload) {
        this.transferId = transferId;
        this.index = index;
        this.total = total;
        this.payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
    }

    public UUID transferId() {
        return transferId;
    }

    public int index() {
        return index;
    }

    public int total() {
        return total;
    }

    public byte[] payload() {
        return Arrays.copyOf(payload, payload.length);
    }
}

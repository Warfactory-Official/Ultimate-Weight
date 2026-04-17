package com.warfactory.ultimateweight.network;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ConfigReassembler {
    private final Map<UUID, AssemblyState> states = new HashMap<UUID, AssemblyState>();

    public synchronized String accept(ConfigFragment fragment) {
        validate(fragment);
        AssemblyState state = states.get(fragment.transferId());
        if (state == null) {
            state = new AssemblyState(fragment.total());
            states.put(fragment.transferId(), state);
        } else if (state.total != fragment.total()) {
            throw new IllegalArgumentException("Fragment total changed for transfer " + fragment.transferId());
        }

        if (state.parts[fragment.index()] == null) {
            state.parts[fragment.index()] = fragment.payload();
            state.received++;
        }

        if (state.received != state.total) {
            return null;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (byte[] payload : state.parts) {
            output.write(payload, 0, payload.length);
        }
        states.remove(fragment.transferId());
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void validate(ConfigFragment fragment) {
        if (fragment.total() <= 0) {
            throw new IllegalArgumentException("Fragment total must be positive");
        }
        if (fragment.index() < 0 || fragment.index() >= fragment.total()) {
            throw new IllegalArgumentException("Fragment index is out of bounds");
        }
    }

    private static final class AssemblyState {
        private final int total;
        private final byte[][] parts;
        private int received;

        private AssemblyState(int total) {
            this.total = total;
            this.parts = new byte[total][];
        }
    }
}

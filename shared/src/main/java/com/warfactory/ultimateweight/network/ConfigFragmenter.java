package com.warfactory.ultimateweight.network;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ConfigFragmenter {
    public static final int LEGACY_SAFE_MAX_PAYLOAD_BYTES = 31 * 1024;

    private final int maxPayloadBytes;

    public ConfigFragmenter() {
        this(LEGACY_SAFE_MAX_PAYLOAD_BYTES);
    }

    public ConfigFragmenter(int maxPayloadBytes) {
        if (maxPayloadBytes <= 0) {
            throw new IllegalArgumentException("maxPayloadBytes must be positive");
        }
        this.maxPayloadBytes = maxPayloadBytes;
    }

    public List<ConfigFragment> fragment(String yamlText) {
        byte[] bytes = yamlText == null ? new byte[0] : yamlText.getBytes(StandardCharsets.UTF_8);
        int total = Math.max(1, (bytes.length + maxPayloadBytes - 1) / maxPayloadBytes);
        ArrayList<ConfigFragment> fragments = new ArrayList<ConfigFragment>(total);
        UUID transferId = UUID.randomUUID();

        for (int index = 0; index < total; index++) {
            int start = index * maxPayloadBytes;
            int end = Math.min(bytes.length, start + maxPayloadBytes);
            int length = end - start;
            byte[] payload = new byte[length];
            if (length > 0) {
                System.arraycopy(bytes, start, payload, 0, length);
            }
            fragments.add(new ConfigFragment(transferId, index, total, payload));
        }
        return fragments;
    }
}

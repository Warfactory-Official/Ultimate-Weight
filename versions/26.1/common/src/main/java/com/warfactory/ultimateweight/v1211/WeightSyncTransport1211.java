package com.warfactory.ultimateweight.v1211;

import com.warfactory.ultimateweight.v1211.network.ConfigFragmentPacket1211;
import com.warfactory.ultimateweight.v1211.network.StaminaUpdatePacket1211;
import com.warfactory.ultimateweight.v1211.network.WeightUpdatePacket1211;
import net.minecraft.server.level.ServerPlayer;

public interface WeightSyncTransport1211 {
    WeightSyncTransport1211 NOOP = new WeightSyncTransport1211() {
        @Override
        public void sendConfigFragment(ServerPlayer player, ConfigFragmentPacket1211 packet) {
        }

        @Override
        public void sendWeightUpdate(ServerPlayer player, WeightUpdatePacket1211 packet) {
        }

        @Override
        public void sendStaminaUpdate(ServerPlayer player, StaminaUpdatePacket1211 packet) {
        }
    };

    void sendConfigFragment(ServerPlayer player, ConfigFragmentPacket1211 packet);

    void sendWeightUpdate(ServerPlayer player, WeightUpdatePacket1211 packet);

    void sendStaminaUpdate(ServerPlayer player, StaminaUpdatePacket1211 packet);
}

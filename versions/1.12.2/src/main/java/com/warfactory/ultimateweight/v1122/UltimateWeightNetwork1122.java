package com.warfactory.ultimateweight.v1122;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.v1122.network.PacketConfigFragment1122;
import com.warfactory.ultimateweight.v1122.network.PacketStaminaUpdate1122;
import com.warfactory.ultimateweight.v1122.network.PacketWeightUpdate1122;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class UltimateWeightNetwork1122 {
    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(UltimateWeightCommon.MOD_ID);
    private static boolean registered;

    private UltimateWeightNetwork1122() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        CHANNEL.registerMessage(ConfigFragmentHandler.class, PacketConfigFragment1122.class, 0, Side.CLIENT);
        CHANNEL.registerMessage(WeightUpdateHandler.class, PacketWeightUpdate1122.class, 1, Side.CLIENT);
        CHANNEL.registerMessage(StaminaUpdateHandler.class, PacketStaminaUpdate1122.class, 2, Side.CLIENT);
    }

    public static SimpleNetworkWrapper channel() {
        return CHANNEL;
    }

    public static final class ConfigFragmentHandler implements IMessageHandler<PacketConfigFragment1122, IMessage> {
        @Override
        public IMessage onMessage(final PacketConfigFragment1122 message, MessageContext ctx) {
            IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
            thread.addScheduledTask(() -> UltimateWeightState1122.receiveConfigFragment(message));
            return null;
        }
    }

    public static final class WeightUpdateHandler implements IMessageHandler<PacketWeightUpdate1122, IMessage> {
        @Override
        public IMessage onMessage(final PacketWeightUpdate1122 message, MessageContext ctx) {
            IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
            thread.addScheduledTask(() -> UltimateWeightState1122.receiveWeightUpdate(message));
            return null;
        }
    }

    public static final class StaminaUpdateHandler implements IMessageHandler<PacketStaminaUpdate1122, IMessage> {
        @Override
        public IMessage onMessage(final PacketStaminaUpdate1122 message, MessageContext ctx) {
            IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
            thread.addScheduledTask(() -> UltimateWeightState1122.receiveStaminaUpdate(message));
            return null;
        }
    }
}

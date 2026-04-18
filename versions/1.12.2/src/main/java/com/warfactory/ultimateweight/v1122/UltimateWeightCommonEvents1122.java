package com.warfactory.ultimateweight.v1122;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.v1122.capability.PlayerWeightProvider1122;
import com.warfactory.ultimateweight.v1122.event.WeightInventoryChangeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
public final class UltimateWeightCommonEvents1122 {
    private static final ResourceLocation PLAYER_WEIGHT_KEY = new ResourceLocation(
        UltimateWeightCommon.MOD_ID,
        "player_weight"
    );

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(PLAYER_WEIGHT_KEY, new PlayerWeightProvider1122());
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            UltimateWeightState1122.onPlayerJoin((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UltimateWeightState1122.onPlayerLeave(event.player);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            UltimateWeightState1122.onPlayerJoin((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            UltimateWeightState1122.onPlayerJoin((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerInventoryChanged(WeightInventoryChangeEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player == null || player.world.isRemote) {
            return;
        }

        if (event.isDelta()) {
            if (player instanceof EntityPlayerMP) {
                UltimateWeightState1122.onInventoryDelta((EntityPlayerMP) player, event);
            }
            return;
        }

        UltimateWeightState1122.markDirty(player);
    }

    @SubscribeEvent
    public void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        UltimateWeightState1122.onPlayerClone(event.getOriginal(), event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof EntityPlayerMP) {
            UltimateWeightState1122.onServerPlayerTick((EntityPlayerMP) event.player);
        }
    }


    @SubscribeEvent
    public void onPickup(EntityItemPickupEvent event) {
        ItemStack stack = event.getItem().getItem();
        TextComponentTranslation message = UltimateWeightState1122.pickupBlockMessage(event.getEntityPlayer(), stack);
        if (message != null) {
            event.setCanceled(true);
            event.getEntityPlayer().sendStatusMessage(message, true);
        }
    }

    @SubscribeEvent
    public void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            UltimateWeightState1122.onPlayerJump((EntityPlayer) event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            UltimateWeightState1122.onLivingFall((EntityPlayer) event.getEntityLiving(), event);
        }
    }

}

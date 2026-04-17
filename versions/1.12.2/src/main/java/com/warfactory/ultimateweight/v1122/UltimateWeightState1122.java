package com.warfactory.ultimateweight.v1122;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.core.StaminaMath;
import com.warfactory.ultimateweight.core.ThresholdEffect;
import com.warfactory.ultimateweight.core.WeightSnapshot;
import com.warfactory.ultimateweight.core.WeightUpdate;
import com.warfactory.ultimateweight.network.ConfigFragment;
import com.warfactory.ultimateweight.runtime.UltimateWeightServices;
import com.warfactory.ultimateweight.v1122.capability.IPlayerWeightData1122;
import com.warfactory.ultimateweight.v1122.capability.UltimateWeightCapabilities1122;
import com.warfactory.ultimateweight.v1122.event.WeightInventoryChangeEvent;
import com.warfactory.ultimateweight.v1122.network.PacketConfigFragment1122;
import com.warfactory.ultimateweight.v1122.network.PacketStaminaUpdate1122;
import com.warfactory.ultimateweight.v1122.network.PacketWeightUpdate1122;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public final class UltimateWeightState1122 {
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("8f13a597-e735-4a4f-90b3-6eea0430e255");
    private static final String SPEED_MODIFIER_NAME = UltimateWeightCommon.MOD_ID + "_move_speed";
    private static final double EPSILON = 0.000001D;
    private static final int EXHAUSTED_HUD_COLOR = 11184810;
    private static final Map<UUID, ServerPlayerState> SERVER_STATES = new Object2ObjectOpenHashMap<UUID, ServerPlayerState>();
    private static volatile PacketWeightUpdate1122 latestClientUpdate = PacketWeightUpdate1122.empty();
    private static volatile PacketStaminaUpdate1122 latestClientStaminaUpdate = PacketStaminaUpdate1122.empty();

    private UltimateWeightState1122() {
    }

    public static void onPlayerJoin(EntityPlayerMP player) {
        ServerPlayerState state = getState(player);
        state.lastObservedFingerprint = fingerprint(player.inventory);
        state.acceptedSnapshot = InventorySnapshot.capture(player);
        state.acceptedWeightKg = WeightViews1122.totalWeight(player);
        UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(player.getUniqueID().toString());
        initializeStamina(player);
        sendConfig(player);
        synchronize(player, true);
        syncStamina(player, true);
    }

    public static void onPlayerLeave(EntityPlayer player) {
        SERVER_STATES.remove(player.getUniqueID());
        UltimateWeightCommon.bootstrap().playerWeightTracker().clear(player.getUniqueID().toString());
    }

    public static void onPlayerClone(EntityPlayer original, EntityPlayer clone) {
        IPlayerWeightData1122 oldData = UltimateWeightCapabilities1122.get(original);
        IPlayerWeightData1122 newData = UltimateWeightCapabilities1122.get(clone);
        if (oldData != null && newData != null) {
            newData.setCurrentWeightKg(oldData.getCurrentWeightKg());
            newData.setCarryCapacityKg(oldData.getCarryCapacityKg());
            newData.setSpeedMultiplier(oldData.getSpeedMultiplier());
            newData.setJumpMultiplier(oldData.getJumpMultiplier());
            newData.setHardLocked(oldData.isHardLocked());
            newData.setCurrentStamina(oldData.getCurrentStamina());
            newData.setMaxStamina(oldData.getMaxStamina());
            newData.setStaminaEnabled(oldData.isStaminaEnabled());
            newData.setExhausted(oldData.isExhausted());
        }
    }

    public static void markDirty(EntityPlayer player) {
        UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(player.getUniqueID().toString());
    }

    public static void onServerPlayerTick(EntityPlayerMP player) {
        synchronize(player, false);
        syncStamina(player, false);
    }

    public static boolean onInventoryDelta(EntityPlayerMP player, WeightInventoryChangeEvent event) {
        if (player == null || event == null || !event.isDelta()) {
            return false;
        }

        ServerPlayerState state = getState(player);
        long currentFingerprint = fingerprint(player.inventory);
        double previousStackWeightKg = WeightViews1122.weightOf(event.getOldStack());
        double newStackWeightKg = WeightViews1122.weightOf(event.getNewStack());
        WeightUpdate update = UltimateWeightCommon.bootstrap().playerWeightTracker().applyDelta(
            WeightViews1122.player(player),
            previousStackWeightKg,
            newStackWeightKg,
            player.ticksExisted
        );

        state.lastObservedFingerprint = currentFingerprint;
        if (!update.updated()) {
            UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(player.getUniqueID().toString());
            return false;
        }

        applyWeightUpdate(player, state, update, true, false);
        return true;
    }

    public static boolean shouldRejectPickup(EntityPlayer player, ItemStack stack) {
        if (isEffectImmune(player)) {
            return false;
        }
        double currentWeightKg = WeightViews1122.totalWeight(player);
        double additionalWeightKg = WeightViews1122.weightOf(stack);
        return currentWeightKg + additionalWeightKg >= UltimateWeightCommon.bootstrap().config().hardLockWeightKg() - EPSILON;
    }

    public static void onPlayerJump(EntityPlayer player) {
        if (isEffectImmune(player)) {
            return;
        }
        WeightConfig.Stamina stamina = UltimateWeightCommon.bootstrap().config().stamina();
        if (player.world.isRemote) {
            if (stamina.drainOnJump()
                && latestClientStaminaUpdate.isStaminaEnabled()
                && latestClientStaminaUpdate.getCurrentStamina() <= EPSILON) {
                player.motionY = 0.0D;
                return;
            }

            player.motionY *= latestClientUpdate.getJumpMultiplier();

            return;
        }

        IPlayerWeightData1122 data = UltimateWeightCapabilities1122.get(player);
        if (data != null && data.isStaminaEnabled() && stamina.drainOnJump()) {
            if (data.getCurrentStamina() <= EPSILON) {
                player.motionY = 0.0D;
                player.setSprinting(false);
                return;
            }
            if (player instanceof EntityPlayerMP) {
                drainStamina((EntityPlayerMP) player, data, stamina.jumpStaminaLoss());
                getState(player).lastStaminaJumpTick = player.ticksExisted;
            }
        }

        if (data != null) {
            player.motionY *= data.getJumpMultiplier();
            player.motionX *= data.getSpeedMultiplier();
            player.motionZ *= data.getSpeedMultiplier();
        }
    }

    public static void onLivingFall(EntityPlayer player, net.minecraftforge.event.entity.living.LivingFallEvent event) {
        if (player.world.isRemote || isEffectImmune(player)) {
            return;
        }

        WeightConfig.FallDamage fallDamage = UltimateWeightCommon.bootstrap().config().fallDamage();
        if (!fallDamage.enabled()) {
            return;
        }

        IPlayerWeightData1122 data = UltimateWeightCapabilities1122.get(player);
        double carryCapacityKg = data != null && data.getCarryCapacityKg() > EPSILON
            ? data.getCarryCapacityKg()
            : UltimateWeightCommon.bootstrap().config().defaultCarryCapacityKg();
        if (carryCapacityKg <= EPSILON) {
            return;
        }

        double currentWeightKg = data != null ? data.getCurrentWeightKg() : WeightViews1122.totalWeight(player);
        double loadPercent = currentWeightKg / carryCapacityKg;
        if (loadPercent <= fallDamage.startLoadPercent()) {
            return;
        }

        double extraMultiplier = (loadPercent - fallDamage.startLoadPercent())
            * fallDamage.extraDamageMultiplierPerLoadPercent();
        if (data != null && data.isHardLocked()) {
            extraMultiplier += fallDamage.hardLockMultiplierBonus();
        }
        if (extraMultiplier <= EPSILON) {
            return;
        }

        float adjusted = (float) Math.min(
            fallDamage.maxDamageMultiplier(),
            event.getDamageMultiplier() + extraMultiplier
        );
        event.setDamageMultiplier(adjusted);
    }

    public static void receiveConfigFragment(PacketConfigFragment1122 message) {
        UltimateWeightServices services = UltimateWeightCommon.bootstrap();
        String yaml = services.configReassembler().accept(message.toFragment());
        if (yaml != null) {
            UltimateWeightCommon.applySyncedConfig(yaml);
        }
    }

    public static void receiveWeightUpdate(PacketWeightUpdate1122 message) {
        latestClientUpdate = message;
    }

    public static void receiveStaminaUpdate(PacketStaminaUpdate1122 message) {
        latestClientStaminaUpdate = message;
    }

    public static void resetClientState() {
        latestClientUpdate = PacketWeightUpdate1122.empty();
        latestClientStaminaUpdate = PacketStaminaUpdate1122.empty();
    }

    public static String hudText(EntityPlayer player) {
        PacketWeightUpdate1122 update = latestClientUpdate;
        double totalWeightKg = update.getCarryCapacityKg() > EPSILON
            ? update.getTotalWeightKg()
            : WeightViews1122.totalWeight(player);
        double carryCapacityKg = update.getCarryCapacityKg() > EPSILON
            ? update.getCarryCapacityKg()
            : UltimateWeightCommon.bootstrap().config().defaultCarryCapacityKg();
        return UltimateWeightCommon.bootstrap().formatter().formatHud(totalWeightKg, carryCapacityKg);
    }

    public static double hudWeightKg(EntityPlayer player) {
        PacketWeightUpdate1122 update = latestClientUpdate;
        return update.getCarryCapacityKg() > EPSILON
            ? update.getTotalWeightKg()
            : WeightViews1122.totalWeight(player);
    }

    public static double hudCarryCapacityKg(EntityPlayer player) {
        PacketWeightUpdate1122 update = latestClientUpdate;
        return update.getCarryCapacityKg() > EPSILON
            ? update.getCarryCapacityKg()
            : UltimateWeightCommon.bootstrap().config().defaultCarryCapacityKg();
    }

    public static int hudColor(EntityPlayer player) {
        if (isEffectImmune(player)) {
            return 10919845;
        }
        if (hudExhausted(player)) {
            return EXHAUSTED_HUD_COLOR;
        }
        PacketWeightUpdate1122 update = latestClientUpdate;
        double totalWeightKg = update.getCarryCapacityKg() > EPSILON
            ? update.getTotalWeightKg()
            : WeightViews1122.totalWeight(player);
        double carryCapacityKg = update.getCarryCapacityKg() > EPSILON
            ? update.getCarryCapacityKg()
            : UltimateWeightCommon.bootstrap().config().defaultCarryCapacityKg();

        if (update.isHardLocked() || totalWeightKg >= UltimateWeightCommon.bootstrap().config().hardLockWeightKg()) {
            return 14556416;
        }

        double percent = carryCapacityKg <= EPSILON ? 0.0D : totalWeightKg / carryCapacityKg;
        if (percent >= 1.0D) {
            return 14575104;
        }
        if (percent >= 0.75D) {
            return 16759808;
        }
        return 10919845;
    }

    public static boolean hudStaminaEnabled(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        if (player.world.isRemote) {
            IPlayerWeightData1122 data = resolveStaminaData(player);
            return data != null ? data.isStaminaEnabled() : latestClientStaminaUpdate.isStaminaEnabled();
        }
        IPlayerWeightData1122 data = UltimateWeightCapabilities1122.get(player);
        return data != null && data.isStaminaEnabled();
    }

    public static double hudStamina(EntityPlayer player) {
        if (player == null) {
            return 0.0D;
        }
        if (player.world.isRemote) {
            IPlayerWeightData1122 data = resolveStaminaData(player);
            return data != null ? data.getCurrentStamina() : latestClientStaminaUpdate.getCurrentStamina();
        }
        IPlayerWeightData1122 data = UltimateWeightCapabilities1122.get(player);
        return data == null ? 0.0D : data.getCurrentStamina();
    }

    public static double hudMaxStamina(EntityPlayer player) {
        if (player == null) {
            return 0.0D;
        }
        if (player.world.isRemote) {
            IPlayerWeightData1122 data = resolveStaminaData(player);
            return data != null ? data.getMaxStamina() : latestClientStaminaUpdate.getMaxStamina();
        }
        IPlayerWeightData1122 data = UltimateWeightCapabilities1122.get(player);
        return data == null ? 0.0D : data.getMaxStamina();
    }

    public static IPlayerWeightData1122 resolveStaminaData(EntityPlayer player) {
        if (player == null) {
            return null;
        }

        IPlayerWeightData1122 data = UltimateWeightCapabilities1122.get(player);
        if (data == null || !player.world.isRemote) {
            return data;
        }

        data.setCurrentStamina(latestClientStaminaUpdate.getCurrentStamina());
        data.setMaxStamina(latestClientStaminaUpdate.getMaxStamina());
        data.setStaminaEnabled(latestClientStaminaUpdate.isStaminaEnabled());
        data.setExhausted(
            resolveExhaustedState(
                data.getCurrentStamina(),
                data.getMaxStamina(),
                data.isStaminaEnabled(),
                data.isExhausted()
            )
        );
        return data;
    }

    public static int hudStaminaColor(EntityPlayer player) {
        if (hudExhausted(player)) {
            return EXHAUSTED_HUD_COLOR;
        }

        double current = hudStamina(player);
        double max = hudMaxStamina(player);
        if (max <= EPSILON) {
            return 10919845;
        }
        double percent = current / max;
        if (percent <= 0.10D) {
            return 14556416;
        }
        if (percent <= 0.35D) {
            return 16759808;
        }
        return 5635925;
    }

    public static boolean hudExhausted(EntityPlayer player) {
        IPlayerWeightData1122 data = resolveStaminaData(player);
        return data != null && data.isExhausted();
    }

    private static void synchronize(EntityPlayerMP player, boolean forceSend) {
        ServerPlayerState state = getState(player);
        long fingerprint = fingerprint(player.inventory);
        boolean inventoryChanged = fingerprint != state.lastObservedFingerprint;
        if (inventoryChanged) {
            state.lastObservedFingerprint = fingerprint;
            UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(player.getUniqueID().toString());
        }

        WeightUpdate update = UltimateWeightCommon.bootstrap().playerWeightTracker().refresh(
            WeightViews1122.player(player),
            player.ticksExisted
        );
        applyWeightUpdate(player, state, update, inventoryChanged, forceSend);
    }

    private static void applyWeightUpdate(
        EntityPlayerMP player,
        ServerPlayerState state,
        WeightUpdate update,
        boolean inventoryChanged,
        boolean forceSend
    ) {
        boolean immune = isEffectImmune(player);
        boolean immunityChanged = state.lastEffectImmune != immune;
        state.lastEffectImmune = immune;
        if (!update.updated() && !forceSend && !immunityChanged) {
            return;
        }

        WeightSnapshot snapshot = immune ? suppressEffects(update.snapshot()) : update.snapshot();
        double currentWeightKg = snapshot.totalWeightKg();
        double hardLockKg = UltimateWeightCommon.bootstrap().config().hardLockWeightKg();
        if (!immune
            && inventoryChanged
            && state.acceptedSnapshot != null
            && state.acceptedWeightKg < hardLockKg - EPSILON
            && currentWeightKg >= hardLockKg - EPSILON) {
            state.acceptedSnapshot.restore(player);
            player.sendStatusMessage(new TextComponentTranslation("message.wfweight.transfer_blocked"), true);
            player.inventory.markDirty();
            state.lastObservedFingerprint = fingerprint(player.inventory);
            UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(player.getUniqueID().toString());
            WeightUpdate reverted = UltimateWeightCommon.bootstrap().playerWeightTracker().refresh(
                WeightViews1122.player(player),
                player.ticksExisted
            );
            if (reverted.updated()) {
                snapshot = immune ? suppressEffects(reverted.snapshot()) : reverted.snapshot();
                currentWeightKg = snapshot.totalWeightKg();
                updateCapability(player, snapshot);
                applySpeedModifier(player, snapshot.thresholdEffect().speedMultiplier());
                state.lastThresholdIndex = snapshot.thresholdEffect().thresholdIndex();
                state.acceptedWeightKg = currentWeightKg;
                UltimateWeightNetwork1122.channel().sendTo(
                    PacketWeightUpdate1122.fromSnapshot(snapshot),
                    player
                );
                syncInventory(player);
            }
            return;
        }

        if (state.acceptedSnapshot == null
            || immune
            || currentWeightKg < hardLockKg - EPSILON
            || state.acceptedWeightKg >= hardLockKg - EPSILON) {
            state.acceptedSnapshot = InventorySnapshot.capture(player);
            state.acceptedWeightKg = currentWeightKg;
        }

        updateCapability(player, snapshot);
        if (state.lastThresholdIndex != snapshot.thresholdEffect().thresholdIndex() || immunityChanged) {
            applySpeedModifier(player, snapshot.thresholdEffect().speedMultiplier());
            state.lastThresholdIndex = snapshot.thresholdEffect().thresholdIndex();
        }

        if (forceSend || update.weightChanged() || update.thresholdChanged() || immunityChanged) {
            UltimateWeightNetwork1122.channel().sendTo(PacketWeightUpdate1122.fromSnapshot(snapshot), player);
        }
    }

    private static void updateCapability(EntityPlayer player, WeightSnapshot snapshot) {
        IPlayerWeightData1122 data = UltimateWeightCapabilities1122.get(player);
        if (data == null) {
            return;
        }

        data.setCurrentWeightKg(snapshot.totalWeightKg());
        data.setCarryCapacityKg(snapshot.carryCapacityKg());
        data.setSpeedMultiplier(snapshot.thresholdEffect().speedMultiplier());
        data.setJumpMultiplier(snapshot.thresholdEffect().jumpMultiplier());
        data.setHardLocked(snapshot.hardLocked());
    }

    private static void initializeStamina(EntityPlayer player) {
        IPlayerWeightData1122 data = UltimateWeightCapabilities1122.get(player);
        if (data == null) {
            return;
        }

        WeightConfig.Stamina stamina = UltimateWeightCommon.bootstrap().config().stamina();
        double maxStamina = stamina.totalStamina();
        if (data.getMaxStamina() <= EPSILON) {
            data.setCurrentStamina(maxStamina);
        } else {
            data.setCurrentStamina(StaminaMath.clamp(data.getCurrentStamina(), maxStamina));
        }
        data.setMaxStamina(maxStamina);
        data.setStaminaEnabled(StaminaMath.isEnabled(stamina));
        data.setExhausted(
            resolveExhaustedState(
                data.getCurrentStamina(),
                data.getMaxStamina(),
                data.isStaminaEnabled(),
                data.isExhausted()
            )
        );
    }

    private static void syncStamina(EntityPlayerMP player, boolean forceSend) {
        IPlayerWeightData1122 data = UltimateWeightCapabilities1122.get(player);
        if (data == null) {
            return;
        }

        WeightConfig.Stamina stamina = UltimateWeightCommon.bootstrap().config().stamina();
        boolean enabled = StaminaMath.isEnabled(stamina);
        double maxStamina = stamina.totalStamina();
        double currentStamina = data.getCurrentStamina();
        boolean exhausted = data.isExhausted();
        boolean changed = false;
        boolean jumpedThisTick = getState(player).lastStaminaJumpTick == player.ticksExisted;

        if (data.getMaxStamina() <= EPSILON) {
            currentStamina = maxStamina;
            changed = true;
        }

        if (!enabled) {
            currentStamina = maxStamina;
        } else {
            boolean drained = false;
            if (stamina.drainWhileRunning() && isRunning(player)) {
                currentStamina = drainValue(player, data, currentStamina, stamina.sprintStaminaLossRate());
                drained = true;
            }

            if (!drained && !jumpedThisTick) {
                currentStamina = StaminaMath.clamp(currentStamina + stamina.staminaGainRate(), maxStamina);
            }
        }

        currentStamina = StaminaMath.clamp(currentStamina, maxStamina);
        exhausted = resolveExhaustedState(currentStamina, maxStamina, enabled, exhausted);
        if (exhausted) {
            player.setSprinting(false);
        }
        if (Math.abs(data.getCurrentStamina() - currentStamina) > EPSILON) {
            data.setCurrentStamina(currentStamina);
            changed = true;
        }
        if (Math.abs(data.getMaxStamina() - maxStamina) > EPSILON) {
            data.setMaxStamina(maxStamina);
            changed = true;
        }
        if (data.isStaminaEnabled() != enabled) {
            data.setStaminaEnabled(enabled);
            changed = true;
        }
        if (data.isExhausted() != exhausted) {
            data.setExhausted(exhausted);
            changed = true;
        }

        if (forceSend || changed) {
            UltimateWeightNetwork1122.channel().sendTo(
                new PacketStaminaUpdate1122(
                    data.getCurrentStamina(),
                    data.getMaxStamina(),
                    data.isStaminaEnabled()
                ),
                player
            );
        }
    }

    private static void drainStamina(EntityPlayerMP player, IPlayerWeightData1122 data, double baseLoss) {
        if (data == null || !data.isStaminaEnabled()) {
            return;
        }

        double currentStamina = drainValue(player, data, data.getCurrentStamina(), baseLoss);
        data.setCurrentStamina(currentStamina);
        data.setExhausted(
            resolveExhaustedState(currentStamina, data.getMaxStamina(), data.isStaminaEnabled(), data.isExhausted())
        );
        if (data.isExhausted()) {
            player.setSprinting(false);
        }
        UltimateWeightNetwork1122.channel().sendTo(
            new PacketStaminaUpdate1122(
                data.getCurrentStamina(),
                data.getMaxStamina(),
                data.isStaminaEnabled()
            ),
            player
        );
    }

    private static double drainValue(EntityPlayer player, IPlayerWeightData1122 data, double currentValue, double baseLoss) {
        double carryCapacityKg = data != null && data.getCarryCapacityKg() > EPSILON
            ? data.getCarryCapacityKg()
            : UltimateWeightCommon.bootstrap().config().defaultCarryCapacityKg();
        double totalWeightKg = data != null ? data.getCurrentWeightKg() : WeightViews1122.totalWeight(player);
        double useMultiplier = StaminaMath.resolveUseMultiplier(
            UltimateWeightCommon.bootstrap().config().stamina(),
            totalWeightKg,
            carryCapacityKg
        );
        return StaminaMath.clamp(
            currentValue - (baseLoss * useMultiplier),
            data == null ? UltimateWeightCommon.bootstrap().config().stamina().totalStamina() : data.getMaxStamina()
        );
    }

    private static boolean resolveExhaustedState(
        double currentStamina,
        double maxStamina,
        boolean staminaEnabled,
        boolean currentlyExhausted
    ) {
        return StaminaMath.resolveExhausted(
            UltimateWeightCommon.bootstrap().config().stamina(),
            currentStamina,
            maxStamina,
            staminaEnabled,
            currentlyExhausted
        );
    }

    private static boolean isRunning(EntityPlayer player) {
        return player.isSprinting();
    }

    private static void applySpeedModifier(EntityPlayer player, double speedMultiplier) {
        IAttributeInstance movement = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (movement == null) {
            return;
        }

        AttributeModifier existing = movement.getModifier(SPEED_MODIFIER_ID);
        if (existing != null) {
            movement.removeModifier(existing);
        }

        double amount = speedMultiplier - 1.0D;
        if (Math.abs(amount) > EPSILON) {
            movement.applyModifier(
                new AttributeModifier(SPEED_MODIFIER_ID, SPEED_MODIFIER_NAME, amount, 2).setSaved(false)
            );
        }
    }

    private static WeightSnapshot suppressEffects(WeightSnapshot snapshot) {
        double loadPercent = snapshot.carryCapacityKg() <= EPSILON
            ? 0.0D
            : snapshot.totalWeightKg() / snapshot.carryCapacityKg();
        return new WeightSnapshot(
            snapshot.totalWeightKg(),
            snapshot.carryCapacityKg(),
            ThresholdEffect.defaults(loadPercent),
            false
        );
    }

    private static void sendConfig(EntityPlayerMP player) {
        String yaml = UltimateWeightCommon.serializeActiveConfig();
        for (ConfigFragment fragment : UltimateWeightCommon.bootstrap().configFragmenter().fragment(yaml)) {
            UltimateWeightNetwork1122.channel().sendTo(PacketConfigFragment1122.fromFragment(fragment), player);
        }
    }

    private static void syncInventory(EntityPlayerMP player) {
        Container openContainer = player.openContainer;
        openContainer.detectAndSendChanges();
        player.sendContainerToPlayer(openContainer);
        player.connection.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
    }

    private static long fingerprint(InventoryPlayer inventory) {
        long hash = 1125899906842597L;
        for (int index = 0; index < inventory.getSizeInventory(); index++) {
            hash = 31L * hash + stackFingerprint(inventory.getStackInSlot(index));
        }
        hash = 31L * hash + inventory.currentItem;
        hash = 31L * hash + stackFingerprint(inventory.getItemStack());
        return hash;
    }

    private static int stackFingerprint(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        int hash = stack.getItem().getRegistryName() == null ? 0 : stack.getItem().getRegistryName().hashCode();
        hash = 31 * hash + stack.getMetadata();
        hash = 31 * hash + stack.getCount();
        NBTTagCompound tag = stack.getTagCompound();
        hash = 31 * hash + (tag == null ? 0 : tag.hashCode());
        return hash;
    }

    private static ServerPlayerState getState(EntityPlayer player) {
        ServerPlayerState state = SERVER_STATES.get(player.getUniqueID());
        if (state == null) {
            state = new ServerPlayerState();
            SERVER_STATES.put(player.getUniqueID(), state);
        }
        return state;
    }

    private static boolean isEffectImmune(EntityPlayer player) {
        return player.capabilities.isCreativeMode;
    }

    private static final class ServerPlayerState {
        private long lastObservedFingerprint;
        private InventorySnapshot acceptedSnapshot;
        private double acceptedWeightKg;
        private int lastThresholdIndex = Integer.MIN_VALUE;
        private boolean lastEffectImmune;
        private long lastStaminaJumpTick = Long.MIN_VALUE;
    }

    private static final class InventorySnapshot {
        private final int windowId;
        private final ArrayList<ItemStack> containerStacks;
        private final NBTTagList inventoryData;
        private final NBTTagCompound carriedData;
        private final int currentItem;

        private InventorySnapshot(
            int windowId,
            ArrayList<ItemStack> containerStacks,
            NBTTagList inventoryData,
            NBTTagCompound carriedData,
            int currentItem
        ) {
            this.windowId = windowId;
            this.containerStacks = containerStacks;
            this.inventoryData = inventoryData;
            this.carriedData = carriedData;
            this.currentItem = currentItem;
        }

        private static InventorySnapshot capture(EntityPlayer player) {
            ArrayList<ItemStack> slotStacks = new ArrayList<ItemStack>(player.openContainer.inventorySlots.size());
            for (Slot slot : player.openContainer.inventorySlots) {
                slotStacks.add(slot.getStack().copy());
            }
            return new InventorySnapshot(
                player.openContainer.windowId,
                slotStacks,
                player.inventory.writeToNBT(new NBTTagList()),
                serializeStack(player.inventory.getItemStack()),
                player.inventory.currentItem
            );
        }

        private void restore(EntityPlayerMP player) {
            if (player.openContainer.windowId == windowId
                && player.openContainer.inventorySlots.size() == containerStacks.size()) {
                for (int index = 0; index < containerStacks.size(); index++) {
                    player.openContainer.getSlot(index).putStack(containerStacks.get(index).copy());
                }
            }
            player.inventory.readFromNBT((NBTTagList) inventoryData.copy());
            player.inventory.setItemStack(deserializeStack(carriedData));
            player.inventory.currentItem = currentItem;
        }

        private static NBTTagCompound serializeStack(ItemStack stack) {
            if (stack.isEmpty()) {
                return new NBTTagCompound();
            }
            return stack.writeToNBT(new NBTTagCompound());
        }

        private static ItemStack deserializeStack(NBTTagCompound tag) {
            return tag.isEmpty() ? ItemStack.EMPTY : new ItemStack(tag);
        }
    }
}

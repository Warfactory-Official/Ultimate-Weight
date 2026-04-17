package com.warfactory.ultimateweight.v1201;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.core.StaminaMath;
import com.warfactory.ultimateweight.core.ThresholdEffect;
import com.warfactory.ultimateweight.core.WeightSnapshot;
import com.warfactory.ultimateweight.core.WeightUpdate;
import com.warfactory.ultimateweight.network.ConfigFragment;
import com.warfactory.ultimateweight.runtime.UltimateWeightServices;
import com.warfactory.ultimateweight.v1201.client.UltimateWeightClientState1201;
import com.warfactory.ultimateweight.v1201.network.ConfigFragmentPacket1201;
import com.warfactory.ultimateweight.v1201.network.StaminaUpdatePacket1201;
import com.warfactory.ultimateweight.v1201.network.WeightUpdatePacket1201;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class UltimateWeight1201 {
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("6f1f8b1f-37ae-45d1-8ab6-6d9da3d4a51c");
    private static final String SPEED_MODIFIER_NAME = UltimateWeightCommon.MOD_ID + "_move_speed";
    private static final double EPSILON = 0.000001D;

    public static final ResourceLocation CONFIG_FRAGMENT_ID = new ResourceLocation(
        UltimateWeightCommon.MOD_ID,
        "config_fragment"
    );
    public static final ResourceLocation WEIGHT_UPDATE_ID = new ResourceLocation(
        UltimateWeightCommon.MOD_ID,
        "weight_update"
    );
    public static final ResourceLocation STAMINA_UPDATE_ID = new ResourceLocation(
        UltimateWeightCommon.MOD_ID,
        "stamina_update"
    );

    private static final Map<UUID, PlayerRuntime> PLAYER_RUNTIMES = new HashMap<UUID, PlayerRuntime>();
    private static WeightSyncTransport1201 transport = WeightSyncTransport1201.NOOP;
    private static PlayerStateListener stateListener = PlayerStateListener.NOOP;
    private static long serverTicks;

    private UltimateWeight1201() {
    }

    public static synchronized void setTransport(WeightSyncTransport1201 value) {
        transport = value == null ? WeightSyncTransport1201.NOOP : value;
    }

    public static synchronized void setStateListener(PlayerStateListener listener) {
        stateListener = listener == null ? PlayerStateListener.NOOP : listener;
    }

    public static synchronized void onServerTick(MinecraftServer server) {
        serverTicks++;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncPlayer(player, false);
        }
    }

    public static synchronized void onPlayerJoin(ServerPlayer player) {
        PlayerRuntime runtime = runtime(player);
        runtime.lastInventoryChanges = -1;
        runtime.lastMenuStateId = -1;
        runtime.lastEffectImmune = isEffectImmune(player);
        UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(player.getStringUUID());
        initializeStamina(player, runtime);
        sendConfig(player);
        syncPlayer(player, true);
        syncStamina(player, true);
    }

    public static synchronized void onPlayerLeave(ServerPlayer player) {
        PLAYER_RUNTIMES.remove(player.getUUID());
        UltimateWeightCommon.bootstrap().playerWeightTracker().clear(player.getStringUUID());
        stateListener.onPlayerLeave(player);
    }

    public static void onPlayerClone(Player original, Player clone) {
        stateListener.onClone(original, clone);
    }

    public static synchronized boolean shouldRejectPickup(ServerPlayer player, ItemStack stack) {
        if (isEffectImmune(player)) {
            return false;
        }
        WeightSnapshot snapshot = ensureSnapshot(player);
        double additionalWeightKg = WeightViews1201.weightOf(stack);
        return additionalWeightKg > EPSILON
            && !UltimateWeightCommon.bootstrap().playerWeightTracker().canAcceptAdditionalWeight(snapshot, additionalWeightKg);
    }

    public static synchronized MenuClickSnapshot snapshotMenuClick(
        AbstractContainerMenu menu,
        Player player,
        int clickedSlot
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return null;
        }

        ArrayList<ItemStack> slotItems = new ArrayList<ItemStack>(menu.slots.size());
        for (int index = 0; index < menu.slots.size(); index++) {
            slotItems.add(menu.getSlot(index).getItem().copy());
        }
        return new MenuClickSnapshot(
            slotItems,
            menu.getCarried().copy(),
            WeightViews1201.totalWeight(serverPlayer),
            clickedSlot
        );
    }

    public static synchronized void finishMenuClick(
        AbstractContainerMenu menu,
        Player player,
        MenuClickSnapshot snapshot
    ) {
        if (snapshot == null || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (isEffectImmune(serverPlayer)) {
            return;
        }

        WeightConfig config = UltimateWeightCommon.bootstrap().config();
        double afterWeightKg = WeightViews1201.totalWeight(serverPlayer);
        if (afterWeightKg + EPSILON < config.hardLockWeightKg()
            || afterWeightKg <= snapshot.beforeWeightKg() + EPSILON) {
            return;
        }

        restoreMenu(serverPlayer, menu, snapshot);
        serverPlayer.displayClientMessage(Component.translatable("message.wfweight.transfer_blocked"), true);
        UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(serverPlayer.getStringUUID());
        syncPlayer(serverPlayer, true);
    }

    public static synchronized void receiveConfigFragment(ConfigFragmentPacket1201 packet) {
        UltimateWeightServices services = UltimateWeightCommon.bootstrap();
        String yaml = services.configReassembler().accept(packet.toFragment());
        if (yaml != null) {
            UltimateWeightCommon.applySyncedConfig(yaml);
        }
    }

    public static void receiveWeightUpdate(WeightUpdatePacket1201 packet) {
        UltimateWeightClientState1201.apply(packet);
    }

    public static void receiveStaminaUpdate(StaminaUpdatePacket1201 packet) {
        UltimateWeightClientState1201.applyStamina(packet);
    }

    public static void resetClientState() {
        UltimateWeightClientState1201.reset();
    }

    public static double jumpMultiplier(Player player) {
        if (isEffectImmune(player)) {
            return 1.0D;
        }
        WeightConfig.Stamina stamina = UltimateWeightCommon.bootstrap().config().stamina();
        if (player.level().isClientSide && player.isLocalPlayer()) {
            StaminaUpdatePacket1201 staminaUpdate = UltimateWeightClientState1201.latestStamina();
            if (stamina.drainOnJump()
                && staminaUpdate.staminaEnabled()
                && staminaUpdate.currentStamina() <= EPSILON) {
                return 0.0D;
            }
            return UltimateWeightClientState1201.latest().jumpMultiplier();
        }
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerRuntime runtime = PLAYER_RUNTIMES.get(serverPlayer.getUUID());
            if (runtime != null && runtime.snapshot != null) {
                if (stamina.drainOnJump() && runtime.staminaEnabled) {
                    if (runtime.currentStamina <= EPSILON) {
                        serverPlayer.setSprinting(false);
                        return 0.0D;
                    }
                    if (runtime.lastStaminaJumpTick != serverTicks) {
                        runtime.currentStamina = drainStaminaValue(runtime, stamina.jumpStaminaLoss());
                        runtime.lastStaminaJumpTick = serverTicks;
                        syncStamina(serverPlayer, true);
                    }
                }
                return runtime.snapshot.thresholdEffect().jumpMultiplier();
            }
        }
        return 1.0D;
    }

    public static float adjustFallDamageMultiplier(Player player, float currentMultiplier) {
        if (!(player instanceof ServerPlayer serverPlayer) || isEffectImmune(player)) {
            return currentMultiplier;
        }

        WeightConfig.FallDamage fallDamage = UltimateWeightCommon.bootstrap().config().fallDamage();
        if (!fallDamage.enabled()) {
            return currentMultiplier;
        }

        WeightSnapshot snapshot = ensureSnapshot(serverPlayer);
        double carryCapacityKg = snapshot.carryCapacityKg();
        if (carryCapacityKg <= EPSILON) {
            return currentMultiplier;
        }

        double loadPercent = snapshot.totalWeightKg() / carryCapacityKg;
        if (loadPercent <= fallDamage.startLoadPercent()) {
            return currentMultiplier;
        }

        double extraMultiplier = (loadPercent - fallDamage.startLoadPercent())
            * fallDamage.extraDamageMultiplierPerLoadPercent();
        if (snapshot.hardLocked()) {
            extraMultiplier += fallDamage.hardLockMultiplierBonus();
        }
        if (extraMultiplier <= EPSILON) {
            return currentMultiplier;
        }

        return (float) Math.min(
            fallDamage.maxDamageMultiplier(),
            currentMultiplier + extraMultiplier
        );
    }

    public static void appendTooltip(ItemStack stack, List<Component> lines) {
        if (stack.isEmpty()) {
            return;
        }

        double singleWeightKg = UltimateWeightCommon.bootstrap().resolver().resolve(WeightViews1201.stack(stack)).singleItemWeightKg();
        if (singleWeightKg <= EPSILON) {
            return;
        }

        lines.add(
            Component.translatable(
                "tooltip.wfweight.weight",
                UltimateWeightCommon.bootstrap().formatter().formatTooltipWeight(singleWeightKg)
            ).withStyle(ChatFormatting.GRAY)
        );
        if (stack.getCount() > 1) {
            lines.add(
                Component.translatable(
                    "tooltip.wfweight.stack_weight",
                    UltimateWeightCommon.bootstrap().formatter().formatStackWeight(singleWeightKg * stack.getCount())
                ).withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }

    private static void sendConfig(ServerPlayer player) {
        String yaml = UltimateWeightCommon.serializeActiveConfig();
        for (ConfigFragment fragment : UltimateWeightCommon.bootstrap().configFragmenter().fragment(yaml)) {
            transport.sendConfigFragment(player, ConfigFragmentPacket1201.fromFragment(fragment));
        }
    }

    private static WeightSnapshot ensureSnapshot(ServerPlayer player) {
        PlayerRuntime runtime = runtime(player);
        if (runtime.snapshot == null) {
            UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(player.getStringUUID());
            syncPlayer(player, true);
        }
        return runtime.snapshot;
    }

    private static void syncPlayer(ServerPlayer player, boolean forceSend) {
        PlayerRuntime runtime = runtime(player);
        boolean effectImmune = isEffectImmune(player);
        boolean immunityChanged = runtime.lastEffectImmune != effectImmune;
        runtime.lastEffectImmune = effectImmune;
        int inventoryChanges = player.getInventory().getTimesChanged();
        int menuStateId = player.containerMenu.getStateId();
        if (runtime.lastInventoryChanges != inventoryChanges || runtime.lastMenuStateId != menuStateId) {
            UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(player.getStringUUID());
            runtime.lastInventoryChanges = inventoryChanges;
            runtime.lastMenuStateId = menuStateId;
        }

        WeightUpdate update = UltimateWeightCommon.bootstrap().playerWeightTracker().refresh(
            WeightViews1201.player(player),
            serverTicks
        );
        if (!update.updated() && !forceSend && !immunityChanged) {
            return;
        }

        WeightSnapshot snapshot = effectImmune ? suppressEffects(update.snapshot()) : update.snapshot();
        int thresholdIndex = snapshot.thresholdEffect().thresholdIndex();
        if (runtime.lastThresholdIndex != thresholdIndex) {
            applySpeedModifier(player, snapshot.thresholdEffect().speedMultiplier());
            runtime.lastThresholdIndex = thresholdIndex;
        }

        runtime.snapshot = snapshot;
        stateListener.onSnapshot(player, snapshot, effectImmune);
        if (forceSend || update.weightChanged() || update.thresholdChanged() || immunityChanged) {
            transport.sendWeightUpdate(player, WeightUpdatePacket1201.fromSnapshot(snapshot));
        }
    }

    private static void initializeStamina(ServerPlayer player, PlayerRuntime runtime) {
        WeightConfig.Stamina stamina = UltimateWeightCommon.bootstrap().config().stamina();
        StaminaState persisted = stateListener.loadStamina(player);
        runtime.maxStamina = stamina.totalStamina();
        runtime.staminaEnabled = StaminaMath.isEnabled(stamina);
        runtime.currentStamina = persisted != null && persisted.maxStamina() > EPSILON
            ? StaminaMath.clamp(persisted.currentStamina(), runtime.maxStamina)
            : runtime.maxStamina;
        stateListener.onStamina(player, runtime.currentStamina, runtime.maxStamina, runtime.staminaEnabled);
    }

    private static void syncStamina(ServerPlayer player, boolean forceSend) {
        PlayerRuntime runtime = runtime(player);
        WeightConfig.Stamina stamina = UltimateWeightCommon.bootstrap().config().stamina();
        runtime.maxStamina = stamina.totalStamina();
        runtime.staminaEnabled = StaminaMath.isEnabled(stamina);

        if (!runtime.staminaEnabled) {
            runtime.currentStamina = runtime.maxStamina;
        } else {
            boolean drained = false;
            if (stamina.drainWhileRunning() && isRunning(player)) {
                runtime.currentStamina = drainStaminaValue(runtime, stamina.sprintStaminaLossRate());
                drained = true;
                if (runtime.currentStamina <= EPSILON) {
                    player.setSprinting(false);
                }
            }

            if (!drained && runtime.lastStaminaJumpTick != serverTicks) {
                runtime.currentStamina = StaminaMath.clamp(
                    runtime.currentStamina + stamina.staminaGainRate(),
                    runtime.maxStamina
                );
            }
        }

        runtime.currentStamina = StaminaMath.clamp(runtime.currentStamina, runtime.maxStamina);
        boolean changed = forceSend
            || Math.abs(runtime.currentStamina - runtime.lastSentStamina) > EPSILON
            || Math.abs(runtime.maxStamina - runtime.lastSentMaxStamina) > EPSILON
            || runtime.staminaEnabled != runtime.lastSentStaminaEnabled;
        if (!changed) {
            return;
        }

        runtime.lastSentStamina = runtime.currentStamina;
        runtime.lastSentMaxStamina = runtime.maxStamina;
        runtime.lastSentStaminaEnabled = runtime.staminaEnabled;
        stateListener.onStamina(player, runtime.currentStamina, runtime.maxStamina, runtime.staminaEnabled);
        transport.sendStaminaUpdate(
            player,
            new StaminaUpdatePacket1201(runtime.currentStamina, runtime.maxStamina, runtime.staminaEnabled)
        );
    }

    private static double drainStaminaValue(PlayerRuntime runtime, double baseLoss) {
        if (!runtime.staminaEnabled) {
            return runtime.maxStamina;
        }

        WeightSnapshot snapshot = runtime.snapshot;
        double useMultiplier = snapshot == null
            ? 1.0D
            : StaminaMath.resolveUseMultiplier(
                UltimateWeightCommon.bootstrap().config().stamina(),
                snapshot.totalWeightKg(),
                snapshot.carryCapacityKg()
            );
        return StaminaMath.clamp(runtime.currentStamina - (baseLoss * useMultiplier), runtime.maxStamina);
    }

    private static boolean isRunning(Player player) {
        return player.isSprinting();
    }

    private static void applySpeedModifier(ServerPlayer player, double speedMultiplier) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) {
            return;
        }

        movementSpeed.removeModifier(SPEED_MODIFIER_ID);
        double amount = speedMultiplier - 1.0D;
        if (Math.abs(amount) > EPSILON) {
            movementSpeed.addTransientModifier(
                new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    SPEED_MODIFIER_NAME,
                    amount,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
                )
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

    private static boolean isEffectImmune(Player player) {
        return player.getAbilities().instabuild;
    }

    private static void restoreMenu(ServerPlayer player, AbstractContainerMenu menu, MenuClickSnapshot snapshot) {
        for (int index = 0; index < snapshot.slotItems().size(); index++) {
            menu.getSlot(index).set(snapshot.slotItems().get(index).copy());
        }
        menu.setCarried(snapshot.carried().copy());
        menu.broadcastFullState();

        if (snapshot.clickedSlot() >= 0 && snapshot.clickedSlot() < menu.slots.size()) {
            player.connection.send(
                new ClientboundContainerSetSlotPacket(
                    menu.containerId,
                    menu.incrementStateId(),
                    snapshot.clickedSlot(),
                    menu.getSlot(snapshot.clickedSlot()).getItem()
                )
            );
        }
        player.connection.send(
            new ClientboundContainerSetSlotPacket(-1, menu.incrementStateId(), -1, menu.getCarried())
        );
    }

    private static PlayerRuntime runtime(ServerPlayer player) {
        PlayerRuntime runtime = PLAYER_RUNTIMES.get(player.getUUID());
        if (runtime == null) {
            runtime = new PlayerRuntime();
            PLAYER_RUNTIMES.put(player.getUUID(), runtime);
        }
        return runtime;
    }

    public static final class MenuClickSnapshot {
        private final List<ItemStack> slotItems;
        private final ItemStack carried;
        private final double beforeWeightKg;
        private final int clickedSlot;

        private MenuClickSnapshot(
            List<ItemStack> slotItems,
            ItemStack carried,
            double beforeWeightKg,
            int clickedSlot
        ) {
            this.slotItems = slotItems;
            this.carried = carried;
            this.beforeWeightKg = beforeWeightKg;
            this.clickedSlot = clickedSlot;
        }

        public List<ItemStack> slotItems() {
            return slotItems;
        }

        public ItemStack carried() {
            return carried;
        }

        public double beforeWeightKg() {
            return beforeWeightKg;
        }

        public int clickedSlot() {
            return clickedSlot;
        }
    }

    private static final class PlayerRuntime {
        private int lastInventoryChanges = -1;
        private int lastMenuStateId = -1;
        private int lastThresholdIndex = Integer.MIN_VALUE;
        private boolean lastEffectImmune;
        private WeightSnapshot snapshot;
        private double currentStamina;
        private double maxStamina;
        private boolean staminaEnabled;
        private long lastStaminaJumpTick = Long.MIN_VALUE;
        private double lastSentStamina = Double.NaN;
        private double lastSentMaxStamina = Double.NaN;
        private boolean lastSentStaminaEnabled;
    }

    public interface PlayerStateListener {
        PlayerStateListener NOOP = new PlayerStateListener() {
            @Override
            public void onSnapshot(ServerPlayer player, WeightSnapshot snapshot, boolean effectImmune) {
            }

            @Override
            public void onClone(Player original, Player clone) {
            }

            @Override
            public void onPlayerLeave(ServerPlayer player) {
            }

            @Override
            public void onStamina(ServerPlayer player, double currentStamina, double maxStamina, boolean staminaEnabled) {
            }

            @Override
            public StaminaState loadStamina(Player player) {
                return null;
            }
        };

        void onSnapshot(ServerPlayer player, WeightSnapshot snapshot, boolean effectImmune);

        void onClone(Player original, Player clone);

        void onPlayerLeave(ServerPlayer player);

        void onStamina(ServerPlayer player, double currentStamina, double maxStamina, boolean staminaEnabled);

        StaminaState loadStamina(Player player);
    }

    public static final class StaminaState {
        private final double currentStamina;
        private final double maxStamina;
        private final boolean staminaEnabled;

        public StaminaState(double currentStamina, double maxStamina, boolean staminaEnabled) {
            this.currentStamina = currentStamina;
            this.maxStamina = maxStamina;
            this.staminaEnabled = staminaEnabled;
        }

        public double currentStamina() {
            return currentStamina;
        }

        public double maxStamina() {
            return maxStamina;
        }

        public boolean staminaEnabled() {
            return staminaEnabled;
        }
    }
}

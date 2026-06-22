package com.warfactory.ultimateweight.v1211;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.config.EquipmentBonusRules;
import com.warfactory.ultimateweight.config.InventoryGroupRules;
import com.warfactory.ultimateweight.config.WeightConfig;
import com.warfactory.ultimateweight.core.*;
import com.warfactory.ultimateweight.network.ConfigFragment;
import com.warfactory.ultimateweight.runtime.UltimateWeightServices;
import com.warfactory.ultimateweight.v1211.client.UltimateWeightClientState1211;
import com.warfactory.ultimateweight.v1211.network.ConfigFragmentPacket1211;
import com.warfactory.ultimateweight.v1211.network.StaminaUpdatePacket1211;
import com.warfactory.ultimateweight.v1211.network.WeightUpdatePacket1211;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class UltimateWeight1211 {
    private static final Identifier SPEED_MODIFIER_ID =
        Identifier.fromNamespaceAndPath(UltimateWeightCommon.MOD_ID, "move_speed");
    private static final double EPSILON = 0.000001D;

    public static final Identifier CONFIG_FRAGMENT_ID = Identifier.fromNamespaceAndPath(
        UltimateWeightCommon.MOD_ID,
        "config_fragment"
    );
    public static final Identifier WEIGHT_UPDATE_ID = Identifier.fromNamespaceAndPath(
        UltimateWeightCommon.MOD_ID,
        "weight_update"
    );
    public static final Identifier STAMINA_UPDATE_ID = Identifier.fromNamespaceAndPath(
        UltimateWeightCommon.MOD_ID,
        "stamina_update"
    );

    private static final Map<UUID, PlayerRuntime> PLAYER_RUNTIMES = new HashMap<UUID, PlayerRuntime>();
    private static WeightSyncTransport1211 transport = WeightSyncTransport1211.NOOP;
    private static PlayerStateListener stateListener = PlayerStateListener.NOOP;
    private static long serverTicks;

    private UltimateWeight1211() {
    }

    public static synchronized void setTransport(WeightSyncTransport1211 value) {
        transport = value == null ? WeightSyncTransport1211.NOOP : value;
    }

    public static synchronized void setStateListener(PlayerStateListener listener) {
        stateListener = listener == null ? PlayerStateListener.NOOP : listener;
    }

    public static synchronized void onServerTick(MinecraftServer server) {
        serverTicks++;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncPlayer(player, false);
            syncStamina(player, false);
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

    /**
     * Event hook invoked by backpack-mod compat mixins when a backpack's stored contents change
     * outside of a normal inventory-slot edit (e.g. an upgrade auto-inserting while the GUI is
     * closed). Marks tracked players dirty so the next server tick performs one authoritative
     * rescan - it never polls. No-ops on the client, where the runtime map is empty.
     */
    public static synchronized void onBackpackContentsChanged() {
        if (PLAYER_RUNTIMES.isEmpty()) {
            return;
        }
        for (UUID playerId : PLAYER_RUNTIMES.keySet()) {
            UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(playerId.toString());
        }
    }

    public static synchronized Component pickupBlockMessage(ServerPlayer player, ItemStack stack) {
        if (isEffectImmune(player)) {
            return null;
        }
        InventoryConstraintEvaluator.GroupLimitViolation violation = UltimateWeightCommon.bootstrap().constraintEvaluator().findAddedStackViolation(
            WeightViews1211.player(player),
            WeightViews1211.stack(stack)
        );
        if (violation != null) {
            return Component.translatable(
                "message.wfweight.group_limit_pickup_blocked",
                violation.label(),
                Integer.valueOf(violation.limit())
            );
        }
        WeightSnapshot snapshot = ensureSnapshot(player);
        double additionalWeightKg = WeightViews1211.weightOf(stack);
        return additionalWeightKg > EPSILON
            && !UltimateWeightCommon.bootstrap().playerWeightTracker().canAcceptAdditionalWeight(snapshot, additionalWeightKg)
            ? Component.translatable("message.wfweight.pickup_blocked")
            : null;
    }

    public static synchronized boolean shouldRejectPickup(ServerPlayer player, ItemStack stack) {
        Component message = pickupBlockMessage(player, stack);
        return message != null;
    }

    public static boolean isTransferAllowedClient(Player player, int slotId, ClickType clickType, int dragType) {
        if (player == null || slotId < 0 || player.containerMenu == null) {
            return true;
        }
        if (slotId >= player.containerMenu.slots.size()) {
            return true;
        }

        return isTransferAllowed(player, player.containerMenu.slots.get(slotId), clickType, dragType);
    }

    public static boolean isTransferAllowed(Player player, Slot slot, ClickType clickType, int dragType) {
        if (player == null || player.getAbilities().instabuild || slot == null) {
            return true;
        }

        // Sophisticated Backpacks "click to stash" routes an item INTO the backpack via
        // overrideStackedOnOther/overrideOtherStackedOnMe instead of a normal slot move, so the
        // predictive swap model below cannot describe it (and would wrongly block or mis-price it).
        // Allow the click and let the authoritative post-click rescan account for the new weight.
        if (WeightViews1211.isDynamicContainer(slot.getItem())
            || WeightViews1211.isDynamicContainer(carriedStack(player))) {
            return true;
        }

        ItemStack removedStack = removedStack(player, slot, clickType, dragType);
        ItemStack addedStack = addedStack(player, slot, clickType, dragType);
        InventoryConstraintEvaluator.GroupLimitViolation violation =
            UltimateWeightCommon.bootstrap().constraintEvaluator().findDeltaViolation(
                WeightViews1211.player(player),
                removedStack.isEmpty() ? null : WeightViews1211.stack(removedStack),
                addedStack.isEmpty() ? null : WeightViews1211.stack(addedStack)
            );
        if (violation != null) {
            player.displayClientMessage(
                Component.translatable(
                    "message.wfweight.group_limit_transfer_blocked",
                    violation.label(),
                    Integer.valueOf(violation.limit())
                ),
                true
            );
            return false;
        }

        double additionalWeightKg = additionalWeightKg(player, slot, clickType, dragType);
        if (additionalWeightKg <= EPSILON) {
            return true;
        }

        boolean allowed =
            currentWeightKg(player) + additionalWeightKg < UltimateWeightCommon.bootstrap().config().hardLockWeightKg() - EPSILON;
        if (!allowed) {
            player.displayClientMessage(Component.translatable("message.wfweight.transfer_blocked"), true);
        }
        return allowed;
    }

    public static synchronized void onClientInventoryDelta(
        Player player,
        int slotIndex,
        ItemStack oldStack,
        ItemStack newStack
    ) {
        if (player == null || !player.level().isClientSide() || !player.isLocalPlayer() || sameStack(oldStack, newStack)) {
            return;
        }

        // A backpack moving in/out of the inventory only shows half of the move here (its worn
        // counterpart lives in a capability/Curios slot), and its weight is dynamic - so do not
        // predict client-side; let the authoritative server weight update drive it.
        if (WeightViews1211.isDynamicContainer(oldStack) || WeightViews1211.isDynamicContainer(newStack)) {
            return;
        }

        WeightUpdatePacket1211 latest = UltimateWeightClientState1211.latest();
        if (latest.carryCapacityKg() <= EPSILON) {
            return;
        }

        double totalWeightKg = Math.max(
            0.0D,
            latest.totalWeightKg() + (WeightViews1211.weightOf(newStack) - WeightViews1211.weightOf(oldStack))
        );
        ThresholdEffect thresholdEffect = resolveThreshold(totalWeightKg, latest.carryCapacityKg());
        UltimateWeightClientState1211.apply(
            new WeightUpdatePacket1211(
                totalWeightKg,
                latest.carryCapacityKg(),
                thresholdEffect.speedMultiplier(),
                thresholdEffect.jumpMultiplier(),
                totalWeightKg >= UltimateWeightCommon.bootstrap().config().hardLockWeightKg()
            )
        );
    }

    public static synchronized void onServerInventoryDelta(
        ServerPlayer player,
        int slotIndex,
        ItemStack oldStack,
        ItemStack newStack
    ) {
        if (player == null || sameStack(oldStack, newStack)) {
            return;
        }

        PlayerRuntime runtime = runtime(player);
        runtime.lastInventoryChanges = player.getInventory().getTimesChanged();
        runtime.lastMenuStateId = player.containerMenu.getStateId();

        // A backpack's weight is dynamic and only half of an equip/unequip move is visible as an
        // inventory-slot delta (the worn half lives in a capability/Curios slot). A numeric delta
        // would double-count or deduct its contents, so fall back to an authoritative full rescan,
        // triggered only by this change event.
        if (WeightViews1211.isDynamicContainer(oldStack) || WeightViews1211.isDynamicContainer(newStack)) {
            UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(player.getStringUUID());
            return;
        }

        WeightUpdate update = UltimateWeightCommon.bootstrap().playerWeightTracker().applyDelta(
            WeightViews1211.player(player),
            WeightViews1211.weightOf(oldStack),
            WeightViews1211.weightOf(newStack),
            serverTicks
        );
        if (!update.updated()) {
            UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(player.getStringUUID());
            return;
        }

        boolean effectImmune = isEffectImmune(player);
        boolean immunityChanged = runtime.lastEffectImmune != effectImmune;
        runtime.lastEffectImmune = effectImmune;
        WeightSnapshot snapshot = effectImmune ? suppressEffects(update.snapshot()) : update.snapshot();
        if (runtime.lastThresholdIndex != snapshot.thresholdEffect().thresholdIndex() || immunityChanged) {
            applySpeedModifier(player, snapshot.thresholdEffect().speedMultiplier());
            runtime.lastThresholdIndex = snapshot.thresholdEffect().thresholdIndex();
        }

        runtime.snapshot = snapshot;
        enforceMovementLock(player, snapshot.thresholdEffect().speedMultiplier(), snapshot.thresholdEffect().jumpMultiplier());
        stateListener.onSnapshot(player, snapshot, effectImmune);
        if (update.weightChanged() || update.thresholdChanged() || immunityChanged) {
            transport.sendWeightUpdate(player, WeightUpdatePacket1211.fromSnapshot(snapshot));
        }
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
            WeightViews1211.totalWeight(serverPlayer),
            clickedSlot,
            UltimateWeightCommon.bootstrap().constraintEvaluator().resolveGroupLimitState(WeightViews1211.player(serverPlayer))
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
        double afterWeightKg = WeightViews1211.totalWeight(serverPlayer);
        InventoryConstraintEvaluator.GroupLimitState afterGroups =
            UltimateWeightCommon.bootstrap().constraintEvaluator().resolveGroupLimitState(WeightViews1211.player(serverPlayer));
        InventoryConstraintEvaluator.GroupLimitViolation violation =
            UltimateWeightCommon.bootstrap().constraintEvaluator().findWorsenedViolation(snapshot.beforeGroups(), afterGroups);
        boolean overweight = afterWeightKg + EPSILON >= config.hardLockWeightKg()
            && afterWeightKg > snapshot.beforeWeightKg() + EPSILON;
        if (!overweight && violation == null) {
            return;
        }

        // If the click stashed into / pulled from a backpack, the changed contents live in a
        // capability/SavedData, not in a restorable menu slot - rolling back the slots would dupe or
        // lose the stashed item. Accept the change and let the weight reflect reality, matching how
        // the mod treats other backpack-internal changes; the hard lock still blocks world pickups.
        if (involvesDynamicContainer(menu, snapshot)) {
            UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(serverPlayer.getStringUUID());
            syncPlayer(serverPlayer, true);
            return;
        }

        restoreMenu(serverPlayer, menu, snapshot);
        if (violation != null) {
            serverPlayer.displayClientMessage(
                Component.translatable(
                    "message.wfweight.group_limit_transfer_blocked",
                    violation.label(),
                    Integer.valueOf(violation.limit())
                ),
                true
            );
        } else {
            serverPlayer.displayClientMessage(Component.translatable("message.wfweight.transfer_blocked"), true);
        }
        UltimateWeightCommon.bootstrap().playerWeightTracker().markDirty(serverPlayer.getStringUUID());
        syncPlayer(serverPlayer, true);
    }

    public static synchronized void resyncMenu(ServerPlayer player, AbstractContainerMenu menu, int clickedSlot) {
        menu.broadcastFullState();
        if (clickedSlot >= 0 && clickedSlot < menu.slots.size()) {
            player.connection.send(
                new ClientboundContainerSetSlotPacket(
                    menu.containerId,
                    menu.incrementStateId(),
                    clickedSlot,
                    menu.getSlot(clickedSlot).getItem()
                )
            );
        }
        player.connection.send(
            new ClientboundContainerSetSlotPacket(-1, menu.incrementStateId(), -1, menu.getCarried())
        );
    }

    public static synchronized void receiveConfigFragment(ConfigFragmentPacket1211 packet) {
        UltimateWeightServices services = UltimateWeightCommon.bootstrap();
        String yaml = services.configReassembler().accept(packet.toFragment());
        if (yaml != null) {
            UltimateWeightCommon.applySyncedConfig(yaml);
        }
    }

    public static void receiveWeightUpdate(WeightUpdatePacket1211 packet) {
        UltimateWeightClientState1211.apply(packet);
    }

    public static void receiveStaminaUpdate(StaminaUpdatePacket1211 packet) {
        UltimateWeightClientState1211.applyStamina(packet);
    }

    public static void resetClientState() {
        UltimateWeightClientState1211.reset();
    }

    public static double jumpMultiplier(Player player) {
        if (isEffectImmune(player)) {
            return 1.0D;
        }
        WeightConfig.Stamina stamina = UltimateWeightCommon.bootstrap().config().stamina();
        if (player.level().isClientSide && player.isLocalPlayer()) {
            StaminaUpdatePacket1211 staminaUpdate = UltimateWeightClientState1211.latestStamina();
            if (stamina.drainOnJump()
                && staminaUpdate.staminaEnabled()
                && staminaUpdate.currentStamina() <= EPSILON) {
                return 0.0D;
            }
            return UltimateWeightClientState1211.latest().jumpMultiplier();
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
        appendTooltip(stack, lines, null);
    }

    public static void appendTooltip(ItemStack stack, List<Component> lines, Player player) {
        if (stack.isEmpty()) {
            return;
        }

        double singleWeightKg = UltimateWeightCommon.bootstrap().resolver().resolve(WeightViews1211.stack(stack)).singleItemWeightKg();
        if (singleWeightKg <= EPSILON) {
        } else {
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

        InventoryConstraintEvaluator evaluator = UltimateWeightCommon.bootstrap().constraintEvaluator();
        for (InventoryConstraintEvaluator.GroupLimitDescription group : evaluator.describeStackGroups(
            player == null ? null : WeightViews1211.player(player),
            WeightViews1211.stack(stack)
        )) {
            lines.add(
                Component.translatable(
                    "tooltip.wfweight.group_limit",
                    group.label(),
                    Integer.valueOf(group.limit())
                ).withStyle(ChatFormatting.DARK_AQUA)
            );
        }

        EquipmentBonusRules.EquipmentBonus bonus = evaluator.equipmentBonus(WeightViews1211.stack(stack));
        if (bonus.carryCapacityKg() > EPSILON) {
            lines.add(
                Component.translatable(
                    "tooltip.wfweight.attr_carry_capacity",
                    UltimateWeightCommon.bootstrap().formatter().formatTooltipWeight(bonus.carryCapacityKg())
                ).withStyle(ChatFormatting.BLUE)
            );
        }
        if (bonus.stamina() > EPSILON) {
            lines.add(
                Component.translatable(
                    "tooltip.wfweight.attr_stamina",
                    UltimateWeightCommon.bootstrap().formatter().formatStaminaValue(bonus.stamina())
                ).withStyle(ChatFormatting.BLUE)
            );
        }
        for (Map.Entry<String, Integer> entry : bonus.groupLimitBonuses().entrySet()) {
            InventoryGroupRules.GroupDefinition definition = UltimateWeightCommon.bootstrap().config().inventoryGroupRules().definition(entry.getKey());
            lines.add(
                Component.translatable(
                    "tooltip.wfweight.attr_group_limit",
                    definition == null ? entry.getKey() : definition.label(),
                    Integer.valueOf(entry.getValue().intValue())
                ).withStyle(ChatFormatting.BLUE)
            );
        }
    }

    private static double additionalWeightKg(Player player, Slot slot, ClickType clickType, int dragType) {
        return positiveDelta(weightOf(addedStack(player, slot, clickType, dragType)) - weightOf(removedStack(player, slot, clickType, dragType)));
    }

    private static ItemStack addedStack(Player player, Slot slot, ClickType clickType, int dragType) {
        return switch (clickType) {
            case PICKUP -> pickupAddedStack(player, slot, dragType);
            case QUICK_MOVE -> quickMoveAddedStack(player, slot);
            case SWAP -> swapAddedStack(player, slot, dragType);
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack removedStack(Player player, Slot slot, ClickType clickType, int dragType) {
        return switch (clickType) {
            case PICKUP -> pickupRemovedStack(player, slot, dragType);
            case SWAP -> swapRemovedStack(player, slot, dragType);
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack pickupAddedStack(Player player, Slot slot, int dragType) {
        if (!isPlayerInventorySlot(player, slot)) {
            return ItemStack.EMPTY;
        }

        ItemStack carried = carriedStack(player);
        if (carried.isEmpty() || !slot.mayPlace(carried)) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = slot.getItem();
        if (existing.isEmpty()) {
            return placedStack(slot, carried, dragType);
        }

        if (ItemStack.isSameItemSameComponents(existing, carried)) {
            return mergedStack(slot, existing, carried, dragType);
        }

        ItemStack placed = placedStack(slot, carried, -1);
        return placed.isEmpty() ? ItemStack.EMPTY : placed;
    }

    private static ItemStack pickupRemovedStack(Player player, Slot slot, int dragType) {
        if (!isPlayerInventorySlot(player, slot)) {
            return ItemStack.EMPTY;
        }

        ItemStack carried = carriedStack(player);
        if (carried.isEmpty() || !slot.mayPlace(carried)) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = slot.getItem();
        if (existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, carried)) {
            return ItemStack.EMPTY;
        }
        return existing;
    }

    private static ItemStack quickMoveAddedStack(Player player, Slot slot) {
        if (isPlayerInventorySlot(player, slot) || !slot.hasItem() || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        return slot.getItem();
    }

    private static ItemStack swapAddedStack(Player player, Slot slot, int dragType) {
        if (isPlayerInventorySlot(player, slot)
            || dragType < 0
            || dragType >= 9
            || !slot.hasItem()
            || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        ItemStack outgoing = player.getInventory().getItem(dragType);
        if (!outgoing.isEmpty() && !slot.mayPlace(outgoing)) {
            return ItemStack.EMPTY;
        }
        return slot.getItem();
    }

    private static ItemStack swapRemovedStack(Player player, Slot slot, int dragType) {
        if (isPlayerInventorySlot(player, slot)
            || dragType < 0
            || dragType >= 9
            || !slot.hasItem()
            || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        return player.getInventory().getItem(dragType);
    }

    private static ItemStack placedStack(Slot slot, ItemStack carried, int dragType) {
        if (carried.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int limit = Math.min(slot.getMaxStackSize(carried), carried.getMaxStackSize());
        if (limit <= 0) {
            return ItemStack.EMPTY;
        }

        int count = Math.min(carried.getCount(), limit);
        if (dragType == 1) {
            count = 1;
        }
        return copyWithCount(carried, count);
    }

    private static ItemStack mergedStack(Slot slot, ItemStack existing, ItemStack carried, int dragType) {
        int limit = Math.min(slot.getMaxStackSize(carried), carried.getMaxStackSize());
        int space = limit - existing.getCount();
        if (space <= 0) {
            return ItemStack.EMPTY;
        }

        int count = dragType == 1 ? 1 : Math.min(space, carried.getCount());
        return copyWithCount(carried, count);
    }

    private static ItemStack copyWithCount(ItemStack stack, int count) {
        if (stack.isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    private static ItemStack carriedStack(Player player) {
        return player.containerMenu == null ? ItemStack.EMPTY : player.containerMenu.getCarried();
    }

    private static double currentWeightKg(Player player) {
        if (player.level().isClientSide() && player.isLocalPlayer()) {
            return UltimateWeightClientState1211.latest().totalWeightKg();
        }
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerRuntime runtime = PLAYER_RUNTIMES.get(serverPlayer.getUUID());
            if (runtime != null && runtime.snapshot != null) {
                return runtime.snapshot.totalWeightKg();
            }
        }
        return WeightViews1211.totalWeight(player);
    }

    private static double weightOf(ItemStack stack) {
        return stack == null || stack.isEmpty() ? 0.0D : WeightViews1211.weightOf(stack);
    }

    private static boolean isPlayerInventorySlot(Player player, Slot slot) {
        return slot.container == player.getInventory();
    }

    private static double positiveDelta(double delta) {
        return delta > EPSILON ? delta : 0.0D;
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        if (first == second) {
            return true;
        }
        if (first == null || first.isEmpty()) {
            return second == null || second.isEmpty();
        }
        if (second == null || second.isEmpty()) {
            return false;
        }
        return first.getCount() == second.getCount() && ItemStack.isSameItemSameComponents(first, second);
    }

    private static void sendConfig(ServerPlayer player) {
        String yaml = UltimateWeightCommon.serializeActiveConfig();
        for (ConfigFragment fragment : UltimateWeightCommon.bootstrap().configFragmenter().fragment(yaml)) {
            transport.sendConfigFragment(player, ConfigFragmentPacket1211.fromFragment(fragment));
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
            WeightViews1211.player(player),
            serverTicks
        );
        if (!update.updated() && !forceSend && !immunityChanged) {
            if (runtime.snapshot != null) {
                enforceMovementLock(player, runtime.snapshot.thresholdEffect().speedMultiplier(), runtime.snapshot.thresholdEffect().jumpMultiplier());
            }
            return;
        }

        WeightSnapshot snapshot = effectImmune ? suppressEffects(update.snapshot()) : update.snapshot();
        int thresholdIndex = snapshot.thresholdEffect().thresholdIndex();
        if (runtime.lastThresholdIndex != thresholdIndex) {
            applySpeedModifier(player, snapshot.thresholdEffect().speedMultiplier());
            runtime.lastThresholdIndex = thresholdIndex;
        }

        runtime.snapshot = snapshot;
        enforceMovementLock(player, snapshot.thresholdEffect().speedMultiplier(), snapshot.thresholdEffect().jumpMultiplier());
        stateListener.onSnapshot(player, snapshot, effectImmune);
        if (forceSend || update.weightChanged() || update.thresholdChanged() || immunityChanged) {
            transport.sendWeightUpdate(player, WeightUpdatePacket1211.fromSnapshot(snapshot));
        }
    }

    private static void initializeStamina(ServerPlayer player, PlayerRuntime runtime) {
        WeightConfig.Stamina stamina = UltimateWeightCommon.bootstrap().config().stamina();
        StaminaState persisted = stateListener.loadStamina(player);
        runtime.maxStamina = UltimateWeightCommon.bootstrap().constraintEvaluator().resolveMaxStamina(WeightViews1211.player(player));
        // Creative (effect-immune) players never drain stamina: keep the system inactive for them.
        runtime.staminaEnabled = StaminaMath.isEnabled(stamina, runtime.maxStamina) && !isEffectImmune(player);
        runtime.currentStamina = persisted != null && persisted.maxStamina() > EPSILON
            ? StaminaMath.clamp(persisted.currentStamina(), runtime.maxStamina)
            : runtime.maxStamina;
        runtime.exhausted = resolveExhaustedState(
            runtime.currentStamina,
            runtime.maxStamina,
            runtime.staminaEnabled,
            persisted != null && persisted.exhausted()
        );
        stateListener.onStamina(player, runtime.currentStamina, runtime.maxStamina, runtime.staminaEnabled, runtime.exhausted);
    }

    private static void syncStamina(ServerPlayer player, boolean forceSend) {
        PlayerRuntime runtime = runtime(player);
        WeightConfig.Stamina stamina = UltimateWeightCommon.bootstrap().config().stamina();
        runtime.maxStamina = UltimateWeightCommon.bootstrap().constraintEvaluator().resolveMaxStamina(WeightViews1211.player(player));
        // Creative (effect-immune) players never drain stamina: keep it full and the system inactive.
        runtime.staminaEnabled = StaminaMath.isEnabled(stamina, runtime.maxStamina) && !isEffectImmune(player);

        if (!runtime.staminaEnabled) {
            runtime.currentStamina = runtime.maxStamina;
        } else {
            boolean drained = false;
            if (stamina.drainWhileRunning() && isRunning(player)) {
                runtime.currentStamina = drainStaminaValue(runtime, stamina.sprintStaminaLossRate());
                drained = true;
            }

            if (!drained && runtime.lastStaminaJumpTick != serverTicks) {
                runtime.currentStamina = StaminaMath.clamp(
                    runtime.currentStamina + stamina.staminaGainRate(),
                    runtime.maxStamina
                );
            }
        }

        runtime.currentStamina = StaminaMath.clamp(runtime.currentStamina, runtime.maxStamina);
        runtime.exhausted = resolveExhaustedState(
            runtime.currentStamina,
            runtime.maxStamina,
            runtime.staminaEnabled,
            runtime.exhausted
        );
        if (runtime.exhausted) {
            player.setSprinting(false);
        }
        boolean changed = forceSend
            || Math.abs(runtime.currentStamina - runtime.lastSentStamina) > EPSILON
            || Math.abs(runtime.maxStamina - runtime.lastSentMaxStamina) > EPSILON
            || runtime.staminaEnabled != runtime.lastSentStaminaEnabled
            || runtime.exhausted != runtime.lastSentExhausted;
        if (!changed) {
            return;
        }

        runtime.lastSentStamina = runtime.currentStamina;
        runtime.lastSentMaxStamina = runtime.maxStamina;
        runtime.lastSentStaminaEnabled = runtime.staminaEnabled;
        runtime.lastSentExhausted = runtime.exhausted;
        stateListener.onStamina(player, runtime.currentStamina, runtime.maxStamina, runtime.staminaEnabled, runtime.exhausted);
        transport.sendStaminaUpdate(
            player,
            new StaminaUpdatePacket1211(runtime.currentStamina, runtime.maxStamina, runtime.staminaEnabled, runtime.exhausted)
        );
    }

    /**
     * Whether sprinting must be blocked for this player because stamina is exhausted. Mirrors the
     * 1.12.2 behaviour: the client uses the authoritative exhausted flag synced from the server, and
     * the server enforces its own runtime state (so a stray START_SPRINTING packet cannot re-enable
     * sprinting while exhausted).
     */
    public static boolean isSprintBlocked(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide()) {
            return player.isLocalPlayer() && UltimateWeightClientState1211.isExhausted();
        }
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerRuntime runtime = PLAYER_RUNTIMES.get(serverPlayer.getUUID());
            return runtime != null && runtime.exhausted;
        }
        return false;
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

    private static void enforceMovementLock(Player player, double speedMultiplier, double jumpMultiplier) {
        if (speedMultiplier > EPSILON && jumpMultiplier > EPSILON) {
            return;
        }

        if (speedMultiplier <= EPSILON) {
            player.setDeltaMovement(0.0D, player.getDeltaMovement().y, 0.0D);
            if (player.isSprinting()) {
                player.setSprinting(false);
            }
        }
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

    private static ThresholdEffect resolveThreshold(double totalWeightKg, double carryCapacityKg) {
        double loadPercent = carryCapacityKg <= EPSILON ? 0.0D : totalWeightKg / carryCapacityKg;
        ThresholdEffect effect = ThresholdEffect.defaults(loadPercent);

        for (int index = 0; index < UltimateWeightCommon.bootstrap().config().thresholds().size(); index++) {
            WeightConfig.ThresholdRule rule = UltimateWeightCommon.bootstrap().config().thresholds().get(index);
            if (loadPercent >= rule.percent()) {
                effect = new ThresholdEffect(index, loadPercent, rule.speedMultiplier(), rule.jumpMultiplier());
            }
        }
        return effect;
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
                    amount,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
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

    private static boolean involvesDynamicContainer(AbstractContainerMenu menu, MenuClickSnapshot snapshot) {
        if (WeightViews1211.isDynamicContainer(menu.getCarried())
            || WeightViews1211.isDynamicContainer(snapshot.carried())) {
            return true;
        }
        int clickedSlot = snapshot.clickedSlot();
        if (clickedSlot >= 0
            && clickedSlot < menu.slots.size()
            && WeightViews1211.isDynamicContainer(menu.getSlot(clickedSlot).getItem())) {
            return true;
        }
        return clickedSlot >= 0
            && clickedSlot < snapshot.slotItems().size()
            && WeightViews1211.isDynamicContainer(snapshot.slotItems().get(clickedSlot));
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
        private final InventoryConstraintEvaluator.GroupLimitState beforeGroups;

        private MenuClickSnapshot(
            List<ItemStack> slotItems,
            ItemStack carried,
            double beforeWeightKg,
            int clickedSlot,
            InventoryConstraintEvaluator.GroupLimitState beforeGroups
        ) {
            this.slotItems = slotItems;
            this.carried = carried;
            this.beforeWeightKg = beforeWeightKg;
            this.clickedSlot = clickedSlot;
            this.beforeGroups = beforeGroups;
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

        public InventoryConstraintEvaluator.GroupLimitState beforeGroups() {
            return beforeGroups;
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
        private boolean exhausted;
        private long lastStaminaJumpTick = Long.MIN_VALUE;
        private double lastSentStamina = Double.NaN;
        private double lastSentMaxStamina = Double.NaN;
        private boolean lastSentStaminaEnabled;
        private boolean lastSentExhausted;
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
            public void onStamina(
                ServerPlayer player,
                double currentStamina,
                double maxStamina,
                boolean staminaEnabled,
                boolean exhausted
            ) {
            }

            @Override
            public StaminaState loadStamina(Player player) {
                return null;
            }
        };

        void onSnapshot(ServerPlayer player, WeightSnapshot snapshot, boolean effectImmune);

        void onClone(Player original, Player clone);

        void onPlayerLeave(ServerPlayer player);

        void onStamina(ServerPlayer player, double currentStamina, double maxStamina, boolean staminaEnabled, boolean exhausted);

        StaminaState loadStamina(Player player);
    }

    public static final class StaminaState {
        private final double currentStamina;
        private final double maxStamina;
        private final boolean staminaEnabled;
        private final boolean exhausted;

        public StaminaState(double currentStamina, double maxStamina, boolean staminaEnabled, boolean exhausted) {
            this.currentStamina = currentStamina;
            this.maxStamina = maxStamina;
            this.staminaEnabled = staminaEnabled;
            this.exhausted = exhausted;
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

        public boolean exhausted() {
            return exhausted;
        }
    }
}

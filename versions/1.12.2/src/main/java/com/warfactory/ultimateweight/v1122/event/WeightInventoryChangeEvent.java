package com.warfactory.ultimateweight.v1122.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class WeightInventoryChangeEvent extends Event {

    private final EventType type;
    private final EntityPlayer player;
    private final ItemStack oldStack;
    private final ItemStack newStack;
    private final int slotIndex;
    private final boolean serverSide;

    public static WeightInventoryChangeEvent complex(EntityPlayer player, boolean serverSide) {
        return new WeightInventoryChangeEvent(player, serverSide);
    }

    public static WeightInventoryChangeEvent clientDelta(
        EntityPlayer player,
        int slotIndex,
        ItemStack oldStack,
        ItemStack newStack
    ) {
        return new WeightInventoryChangeEvent(player, slotIndex, oldStack, newStack, false);
    }

    public static WeightInventoryChangeEvent serverDelta(
        EntityPlayer player,
        int slotIndex,
        ItemStack oldStack,
        ItemStack newStack
    ) {
        return new WeightInventoryChangeEvent(player, slotIndex, oldStack, newStack, true);
    }

    private WeightInventoryChangeEvent(EntityPlayer player, boolean serverSide) {
        this.type = EventType.COMPLEX;
        this.slotIndex = -1;
        this.player = player;
        this.oldStack = ItemStack.EMPTY;
        this.newStack = ItemStack.EMPTY;
        this.serverSide = serverSide;
    }

    private WeightInventoryChangeEvent(
        EntityPlayer player,
        int slotIndex,
        ItemStack oldStack,
        ItemStack newStack,
        boolean serverSide
    ) {
        this.type = EventType.DELTA;
        this.slotIndex = slotIndex;
        this.player = player;
        this.oldStack = oldStack;
        this.newStack = newStack;
        this.serverSide = serverSide;
    }

    public EventType getType() {
        return type;
    }

    public boolean isComplex() {
        return type == EventType.COMPLEX;
    }

    public boolean isDelta() {
        return type == EventType.DELTA;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    /**
     * @return The ItemStack that was in the slot *before* the change. Never null.<br>
     * Use {@link ItemStack#copy()} if you don't want to modify the returned stack.<br>
     * Returns {@link ItemStack#EMPTY} for {@link EventType#COMPLEX} events.
     */
    public ItemStack getOldStack() {
        return oldStack;
    }

    /**
     * @return The ItemStack that is in the slot *after* the change. Never null.<br>
     * Use {@link ItemStack#copy()} if you don't want to modify the returned stack.<br>
     * Returns {@link ItemStack#EMPTY} for {@link EventType#COMPLEX} events.
     */
    public ItemStack getNewStack() {
        return newStack;
    }

    /**
     * @return The raw index of the slot that was changed. Only applicable to {@link EventType#DELTA} events.
     */
    public int getRawSlotIndex() {
        return slotIndex;
    }

    public boolean isServerSide() {
        return serverSide;
    }

    /**
     * @return The index of the slot that was changed, normalized to server index. Only applicable to {@link EventType#DELTA} events.<br>
     * On server:
     * <ul>
     *   <li>0-4 is reserved for survival mode inventory crafting.</li>
     *   <li>armor = 5-8</li>
     *   <li>inventory = 9-35</li>
     *   <li>hotbar = 36-44</li>
     *   <li>offhand = 45</li>
     * </ul>
     */
    public int getSlotIndex() {
        return normalizedSlotIndex(slotIndex, serverSide);
    }

    public static int normalizedSlotIndex(int slotIndex, boolean serverSide) {
        if (serverSide) {
            return slotIndex;
        }
        if (slotIndex >= 0 && slotIndex <= 8) {
            return slotIndex + 36;
        }
        if (slotIndex <= 35) {
            return slotIndex;
        }
        if (slotIndex <= 39) {
            return 44 - slotIndex;
        }
        if (slotIndex == 40) {
            return 45;
        }
        return slotIndex;
    }

    public enum EventType {
        /**
         * A single slot was changed. Use {@link #getOldStack()} and {@link #getNewStack()}.
         */
        DELTA,
        /**
         * A complex or unknown change occurred. A full inventory rescan is recommended.
         */
        COMPLEX
    }
}

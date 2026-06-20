package com.warfactory.ultimateweight.v1122;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.api.WeightDataView;
import com.warfactory.ultimateweight.api.WeightItemView;
import com.warfactory.ultimateweight.api.WeightPlayerView;
import com.warfactory.ultimateweight.api.WeightStackView;
import com.warfactory.ultimateweight.core.ResolvedWeight;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public final class WeightViews1122 {
    private static final int MAX_NESTED_DEPTH = 4;

    /**
     * Extra worn-item sources contributed by compat plugins (Baubles slots, a Traveler's Backpack
     * worn in its own capability and its cargo) - items outside the vanilla main/armor inventory
     * that still need their weight counted. Registered once at startup.
     */
    private static final List<InventorySource> INVENTORY_SOURCES = new CopyOnWriteArrayList<InventorySource>();

    /**
     * Identifies stacks whose contents are dynamic / capability-backed (backpacks), so their weight
     * cannot be trusted to a single-slot delta and (for those whose contents live outside the item
     * tag) must not be frozen by the tag-hash cache. Set by compat plugins; defaults to "never".
     */
    private static volatile Predicate<ItemStack> dynamicContainer = stack -> false;

    private WeightViews1122() {
    }

    public static void registerInventorySource(InventorySource source) {
        if (source != null) {
            INVENTORY_SOURCES.add(source);
        }
    }

    public static void setDynamicContainerPredicate(Predicate<ItemStack> predicate) {
        dynamicContainer = predicate == null ? stack -> false : predicate;
    }

    public static boolean isDynamicContainer(ItemStack stack) {
        return stack != null && !stack.isEmpty() && dynamicContainer.test(stack);
    }

    /**
     * Worn-item source contributed by a compat plugin. Lets a plugin add items outside the vanilla
     * main/armor inventory without WeightViews having to know about any specific mod.
     */
    @FunctionalInterface
    public interface InventorySource {
        void collect(EntityPlayer player, InventorySink sink);
    }

    /** Where an {@link InventorySource} deposits the items it finds. */
    public interface InventorySink {
        /** A worn item resolved normally; counts toward both total weight and equipment bonuses. */
        void addWorn(ItemStack stack);

        /** A worn item counted at its base configured weight only (no nested-content resolution). */
        void addWornBase(ItemStack stack);

        /** Cargo stored inside a worn container; counts toward total weight only, not equipment. */
        void addCargo(ItemStack stack);
    }

    private static void collectWornInto(EntityPlayer player, InventorySink sink) {
        for (InventorySource source : INVENTORY_SOURCES) {
            try {
                source.collect(player, sink);
            } catch (Throwable ignored) {
            }
        }
    }

    public static WeightPlayerView player(EntityPlayer player) {
        return new PlayerView(player);
    }

    public static WeightStackView stack(ItemStack stack) {
        return new StackView(stack, 0);
    }

    public static double totalWeight(EntityPlayer player) {
        return UltimateWeightCommon.bootstrap().inventoryCalculator().calculateTotalWeightKg(player(player).inventory());
    }

    public static double weightOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0.0D;
        }
        return stackWeight(stack, 0);
    }

    /**
     * Whether the stack's weight is dynamic / capability-backed and therefore unsafe to track with a
     * single-slot delta. This covers worn Traveler's Backpacks (which move through an invisible
     * capability slot) and any stack whose weight is supplied by a nested-container compat provider.
     * Such changes must trigger a full rescan instead of a numeric delta, otherwise the backpack's
     * contents get double-counted or deducted when it is equipped/unequipped.
     */
    public static boolean isNestedContainer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (isDynamicContainer(stack)) {
            return true;
        }
        return UltimateWeightCommon.bootstrap().resolver().resolve(new StackView(stack, 0)).source()
            == ResolvedWeight.Source.COMPAT_API;
    }

    public static double stackWeight(ItemStack stack, int depth) {
        if (stack == null || stack.isEmpty()) {
            return 0.0D;
        }
        return effectiveSingleWeight(stack, depth) * stack.getCount();
    }

    public static double configuredWeightOf(ItemStack stack, int depth) {
        if (stack == null || stack.isEmpty()) {
            return 0.0D;
        }
        return UltimateWeightCommon.bootstrap().resolver().resolveConfigured(new BaseStackView(stack, depth)).singleItemWeightKg();
    }

    public static int maxNestedDepth() {
        return MAX_NESTED_DEPTH;
    }

    static double effectiveSingleWeight(ItemStack stack, int depth) {
        if (stack == null || stack.isEmpty()) {
            return 0.0D;
        }
        return UltimateWeightCommon.bootstrap().resolver().resolve(new StackView(stack, depth)).singleItemWeightKg();
    }

    private static final class PlayerView implements WeightPlayerView {
        private final EntityPlayer player;

        private PlayerView(EntityPlayer player) {
            this.player = player;
        }

        @Override
        public String playerId() {
            return player.getUniqueID().toString();
        }

        @Override
        public Iterable<? extends WeightStackView> inventory() {
            InventoryPlayer inventory = player.inventory;
            final ArrayList<WeightStackView> views = new ArrayList<WeightStackView>(inventory.getSizeInventory());
            // Vanilla main inventory. A loose backpack item here resolves its own contents through its
            // weight provider; worn items (Baubles, a Traveler's Backpack worn in its capability and
            // that backpack's cargo) are contributed by the registered inventory sources below.
            for (int index = 0; index < inventory.getSizeInventory(); index++) {
                ItemStack stack = inventory.getStackInSlot(index);
                if (!stack.isEmpty()) {
                    views.add(new StackView(stack, 0));
                }
            }
            collectWornInto(player, new InventorySink() {
                @Override
                public void addWorn(ItemStack stack) {
                    if (!stack.isEmpty()) {
                        views.add(new StackView(stack, 0));
                    }
                }

                @Override
                public void addWornBase(ItemStack stack) {
                    if (!stack.isEmpty()) {
                        views.add(new BaseStackView(stack, 0));
                    }
                }

                @Override
                public void addCargo(ItemStack stack) {
                    if (!stack.isEmpty()) {
                        views.add(new StackView(stack, 0));
                    }
                }
            });
            return views;
        }

        @Override
        public Iterable<? extends WeightStackView> equipped() {
            final ArrayList<WeightStackView> equipped = new ArrayList<WeightStackView>();
            for (ItemStack stack : player.inventory.armorInventory) {
                if (!stack.isEmpty()) {
                    equipped.add(new StackView(stack, 0));
                }
            }
            collectWornInto(player, new InventorySink() {
                @Override
                public void addWorn(ItemStack stack) {
                    if (!stack.isEmpty()) {
                        equipped.add(new StackView(stack, 0));
                    }
                }

                @Override
                public void addWornBase(ItemStack stack) {
                    if (!stack.isEmpty()) {
                        equipped.add(new BaseStackView(stack, 0));
                    }
                }

                @Override
                public void addCargo(ItemStack stack) {
                    // Cargo stored in a worn container is not equipment; it is counted in the total
                    // weight via inventory(), not here.
                }
            });
            return equipped;
        }

        @Override
        public double carryCapacityKg() {
            return UltimateWeightCommon.bootstrap().constraintEvaluator().resolveCarryCapacityKg(this);
        }
    }

    private static final class StackView implements WeightStackView {
        private final ItemStack stack;
        private final ItemView item;
        private final DataView data;
        private final int depth;

        private StackView(ItemStack stack, int depth) {
            this.stack = stack;
            this.item = new ItemView(stack);
            this.data = new DataView(stack);
            this.depth = depth;
        }

        @Override
        public WeightItemView item() {
            return item;
        }

        @Override
        public int count() {
            return stack.getCount();
        }

        @Override
        public int metadata() {
            return stack.getMetadata();
        }

        @Override
        public int resolutionDepth() {
            return depth;
        }

        @Override
        public String complexCacheKey() {
            // Dynamic containers (e.g. a Retro Sophisticated Backpack) keep their contents in a
            // capability, not in the regular item tag, so the tag-hash cache key would never change
            // when the contents do and the cached weight would freeze. Returning null bypasses the
            // cache so the live contents are re-read on every resolve.
            if (isDynamicContainer(stack)) {
                return null;
            }
            return complexKey(stack, depth);
        }

        @Override
        public Object unwrap() {
            return stack;
        }

        @Override
        public WeightDataView data() {
            return data;
        }
    }

    private static final class ItemView implements WeightItemView {
        private final String itemId;
        private final Collection<String> matchKeys;

        private ItemView(ItemStack stack) {
            this.itemId = stack.getItem().getRegistryName() == null
                ? "minecraft:air"
                : stack.getItem().getRegistryName().toString();
            this.matchKeys = buildMatchKeys(stack);
        }

        @Override
        public String itemId() {
            return itemId;
        }

        @Override
        public Collection<String> matchKeys() {
            return matchKeys;
        }

        private static Collection<String> buildMatchKeys(ItemStack stack) {
            Set<String> keys = new LinkedHashSet<String>();
            if (stack == null || stack.isEmpty()) {
                return keys;
            }

            int[] oreIds;
            try {
                oreIds = OreDictionary.getOreIDs(stack);
            } catch (IllegalArgumentException ignored) {
                return keys;
            }
            for (int oreId : oreIds) {
                keys.add(OreDictionary.getOreName(oreId));
            }
            return keys;
        }
    }

    private static final class DataView implements WeightDataView {
        private final ItemStack stack;

        private DataView(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public Double getDouble(String key) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey(key, 99)) {
                return Double.valueOf(tag.getDouble(key));
            }
            return null;
        }
    }

    private static final class BaseStackView implements WeightStackView {
        private final ItemView item;
        private final ItemStack stack;
        private final int depth;

        private BaseStackView(ItemStack stack, int depth) {
            this.stack = stack;
            this.item = new ItemView(stack);
            this.depth = depth;
        }

        @Override
        public WeightItemView item() {
            return item;
        }

        @Override
        public int count() {
            return 1;
        }

        @Override
        public int metadata() {
            return stack.getMetadata();
        }

        @Override
        public int resolutionDepth() {
            return depth;
        }
    }

    private static String complexKey(ItemStack stack, int depth) {
        NBTTagCompound tag = stack.getTagCompound();
        return itemId(stack) + "|" + stack.getMetadata() + "|" + depth + "|" + (tag == null ? 0 : tag.hashCode());
    }

    private static String itemId(ItemStack stack) {
        return stack.getItem().getRegistryName() == null
            ? "minecraft:air"
            : stack.getItem().getRegistryName().toString();
    }
}

package com.warfactory.ultimateweight.v1201;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.api.WeightDataView;
import com.warfactory.ultimateweight.api.WeightItemView;
import com.warfactory.ultimateweight.api.WeightPlayerView;
import com.warfactory.ultimateweight.api.WeightStackView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public final class WeightViews1201 {
    private static final int MAX_NESTED_DEPTH = 4;

    /**
     * Extra worn-item sources contributed by the loader layer (e.g. Curios slots and a Traveler's
     * Backpack worn in its own capability) - things that are not part of the vanilla
     * items/armor/offhand inventory but still need their weight counted. Registered once at startup.
     */
    private static final List<InventorySource> INVENTORY_SOURCES = new CopyOnWriteArrayList<>();

    /**
     * Identifies stacks whose contents are dynamic / capability- or savedata-backed (backpacks), so
     * their weight cannot be cached by the item tag hash and a single-slot delta cannot be trusted.
     * Set once by the loader layer; defaults to "never".
     */
    private static volatile Predicate<ItemStack> dynamicContainer = stack -> false;

    private WeightViews1201() {
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

    @FunctionalInterface
    public interface InventorySource {
        void collect(Player player, List<ItemStack> out);
    }

    private static void collectExtraWorn(Player player, List<ItemStack> out) {
        if (INVENTORY_SOURCES.isEmpty()) {
            return;
        }
        for (InventorySource source : INVENTORY_SOURCES) {
            try {
                source.collect(player, out);
            } catch (Throwable ignored) {
            }
        }
    }

    public static WeightPlayerView player(Player player) {
        return new PlayerView(player);
    }

    public static WeightStackView stack(ItemStack stack) {
        return new StackView(stack, 0);
    }

    public static double totalWeight(Player player) {
        return UltimateWeightCommon.bootstrap().inventoryCalculator().calculateTotalWeightKg(player(player).inventory());
    }

    public static double weightOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0.0D;
        }
        return stackWeight(stack, 0);
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

    private record PlayerView(Player player) implements WeightPlayerView {

        @Override
            public String playerId() {
                return player.getStringUUID();
            }

            @Override
            public Iterable<? extends WeightStackView> inventory() {
                Inventory inventory = player.getInventory();
                ArrayList<WeightStackView> views = new ArrayList<>(
                        inventory.items.size() + inventory.armor.size() + inventory.offhand.size()
                );
                addStacks(views, inventory.items);
                addStacks(views, inventory.armor);
                addStacks(views, inventory.offhand);
                addExtraWorn(views);
                return views;
            }

            @Override
            public Iterable<? extends WeightStackView> equipped() {
                Inventory inventory = player.getInventory();
                ArrayList<WeightStackView> equipped = new ArrayList<>(inventory.armor.size());
                addStacks(equipped, inventory.armor);
                addExtraWorn(equipped);
                return equipped;
            }

            private void addExtraWorn(List<WeightStackView> target) {
                if (INVENTORY_SOURCES.isEmpty()) {
                    return;
                }
                ArrayList<ItemStack> extras = new ArrayList<>();
                collectExtraWorn(player, extras);
                addStacks(target, extras);
            }

            @Override
            public double carryCapacityKg() {
                return UltimateWeightCommon.bootstrap().constraintEvaluator().resolveCarryCapacityKg(this);
            }

            private static void addStacks(List<WeightStackView> target, List<ItemStack> source) {
                for (ItemStack stack : source) {
                    if (!stack.isEmpty()) {
                        target.add(new StackView(stack, 0));
                    }
                }
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
        public int resolutionDepth() {
            return depth;
        }

        @Override
        public String complexCacheKey() {
            // Backpack contents live in a capability / world SavedData, not in the item tag, so the
            // tag-hash cache key would never change when the contents do and the cached weight would
            // freeze. Returning null bypasses the cache so the live contents are re-read each resolve.
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
            this.itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
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
            Set<String> keys = new LinkedHashSet<>();
            stack.getTags().map(TagKey::location).forEach((location) -> keys.add(location.toString()));
            return keys;
        }
    }

    private record DataView(ItemStack stack) implements WeightDataView {

        @Override
            public Double getDouble(String key) {
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains(key, Tag.TAG_ANY_NUMERIC)) {
                    return Double.valueOf(tag.getDouble(key));
                }
                return null;
            }
        }

    private static final class BaseStackView implements WeightStackView {
        private final ItemView item;
        private final int depth;

        private BaseStackView(ItemStack stack, int depth) {
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
        public int resolutionDepth() {
            return depth;
        }
    }

    private static String complexKey(ItemStack stack, int depth) {
        CompoundTag tag = stack.getTag();
        return BuiltInRegistries.ITEM.getKey(stack.getItem()) + "|" + depth + "|" + (tag == null ? 0 : tag.hashCode());
    }
}

package com.warfactory.ultimateweight.v1201;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.api.WeightDataView;
import com.warfactory.ultimateweight.api.WeightItemView;
import com.warfactory.ultimateweight.api.WeightPlayerView;
import com.warfactory.ultimateweight.api.WeightStackView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class WeightViews1201 {
    private static final int MAX_NESTED_DEPTH = 4;

    private WeightViews1201() {
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

    private static final class PlayerView implements WeightPlayerView {
        private final Player player;

        private PlayerView(Player player) {
            this.player = player;
        }

        @Override
        public String playerId() {
            return player.getStringUUID();
        }

        @Override
        public Iterable<? extends WeightStackView> inventory() {
            Inventory inventory = player.getInventory();
            ArrayList<WeightStackView> views = new ArrayList<WeightStackView>(
                inventory.items.size() + inventory.armor.size() + inventory.offhand.size()
            );
            addStacks(views, inventory.items);
            addStacks(views, inventory.armor);
            addStacks(views, inventory.offhand);
            return views;
        }

        @Override
        public double carryCapacityKg() {
            return UltimateWeightCommon.bootstrap().config().defaultCarryCapacityKg();
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
            Set<String> keys = new LinkedHashSet<String>();
            stack.getTags().map(TagKey::location).forEach((location) -> keys.add(location.toString()));
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
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString() + "|" + depth + "|" + (tag == null ? 0 : tag.hashCode());
    }
}

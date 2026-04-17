package com.warfactory.ultimateweight.v1201;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.api.WeightDataView;
import com.warfactory.ultimateweight.api.WeightItemView;
import com.warfactory.ultimateweight.api.WeightPlayerView;
import com.warfactory.ultimateweight.api.WeightStackView;
import com.warfactory.ultimateweight.core.ResolvedWeight;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class WeightViews1201 {
    private static final double EPSILON = 0.000001D;
    private static final int MAX_NESTED_DEPTH = 4;
    private static NestedWeightProvider nestedWeightProvider = NestedWeightProvider.NOOP;

    private WeightViews1201() {
    }

    public static void setNestedWeightProvider(NestedWeightProvider provider) {
        nestedWeightProvider = provider == null ? NestedWeightProvider.NOOP : provider;
    }

    public static WeightPlayerView player(Player player) {
        return new PlayerView(player);
    }

    public static WeightStackView stack(ItemStack stack) {
        return new StackView(stack);
    }

    public static double totalWeight(Player player) {
        return UltimateWeightCommon.bootstrap().inventoryCalculator().calculateTotalWeightKg(player(player).inventory());
    }

    public static double weightOf(ItemStack stack) {
        return stackWeight(stack, 0);
    }

    public static double stackWeight(ItemStack stack, int depth) {
        return effectiveSingleWeight(stack, depth) * stack.getCount();
    }

    static double effectiveSingleWeight(ItemStack stack, int depth) {
        Double rawOverride = rawOverride(stack);
        double baseWeightKg = rawOverride != null
            ? rawOverride.doubleValue()
            : UltimateWeightCommon.bootstrap().resolver().resolve(new BaseStackView(stack)).singleItemWeightKg();
        double nestedWeightKg = depth >= MAX_NESTED_DEPTH ? 0.0D : nestedWeightProvider.additionalWeight(stack, depth);
        double total = baseWeightKg + nestedWeightKg;
        return total > EPSILON ? total : 0.0D;
    }

    private static Double rawOverride(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        String key = UltimateWeightCommon.bootstrap().config().componentOverrideKey();
        if (tag != null && tag.contains(key, Tag.TAG_ANY_NUMERIC)) {
            return Double.valueOf(tag.getDouble(key));
        }
        return null;
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
                    target.add(new StackView(stack));
                }
            }
        }
    }

    private static final class StackView implements WeightStackView {
        private final ItemStack stack;
        private final ItemView item;
        private final DataView data;

        private StackView(ItemStack stack) {
            this.stack = stack;
            this.item = new ItemView(stack);
            this.data = new DataView(stack);
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
            stack.getTags().map(TagKey::location).forEach((location) -> {
                keys.add(location.toString());
                String translated = translateTag(location);
                if (translated != null && !translated.isEmpty()) {
                    keys.add(translated);
                }
            });
            return keys;
        }

        private static String translateTag(ResourceLocation location) {
            String path = location.getPath();
            if ("planks".equals(path)) {
                return "plankWood";
            }

            int separator = path.indexOf('/');
            if (separator < 0 || separator + 1 >= path.length()) {
                return null;
            }

            String prefixPath = path.substring(0, separator);
            String material = path.substring(separator + 1);
            String prefix;
            if ("ingots".equals(prefixPath)) {
                prefix = "ingot";
            } else if ("nuggets".equals(prefixPath)) {
                prefix = "nugget";
            } else if ("gems".equals(prefixPath)) {
                prefix = "gem";
            } else if ("storage_blocks".equals(prefixPath) || "storage_block".equals(prefixPath)) {
                prefix = "block";
            } else if ("plates".equals(prefixPath)) {
                prefix = "plate";
            } else if ("dusts".equals(prefixPath)) {
                prefix = "dust";
            } else {
                return null;
            }

            return prefix + toPascal(material);
        }

        private static String toPascal(String value) {
            StringBuilder builder = new StringBuilder(value.length());
            boolean upper = true;
            for (int index = 0; index < value.length(); index++) {
                char character = value.charAt(index);
                if (character == '_' || character == '/' || character == '-') {
                    upper = true;
                } else if (upper) {
                    builder.append(Character.toUpperCase(character));
                    upper = false;
                } else {
                    builder.append(character);
                }
            }
            return builder.toString();
        }
    }

    private static final class DataView implements WeightDataView {
        private final ItemStack stack;

        private DataView(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public Double getDouble(String key) {
            if (UltimateWeightCommon.bootstrap().config().componentOverrideKey().equals(key)) {
                double effectiveWeightKg = effectiveSingleWeight(stack, 0);
                return effectiveWeightKg > EPSILON ? Double.valueOf(effectiveWeightKg) : null;
            }
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(key, Tag.TAG_ANY_NUMERIC)) {
                return Double.valueOf(tag.getDouble(key));
            }
            return null;
        }
    }

    private static final class BaseStackView implements WeightStackView {
        private final ItemView item;

        private BaseStackView(ItemStack stack) {
            this.item = new ItemView(stack);
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
        public WeightDataView data() {
            return WeightDataView.empty();
        }
    }

    public interface NestedWeightProvider {
        NestedWeightProvider NOOP = new NestedWeightProvider() {
            @Override
            public double additionalWeight(ItemStack stack, int depth) {
                return 0.0D;
            }
        };

        double additionalWeight(ItemStack stack, int depth);
    }
}

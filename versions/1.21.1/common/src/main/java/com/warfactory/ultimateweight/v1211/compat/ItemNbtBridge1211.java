package com.warfactory.ultimateweight.v1211.compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Per-version facade isolating the handful of item-data operations that diverge between the 1.20.1
 * NBT model and the 1.21.1 DataComponents model.
 *
 * <p>The NBT <em>traversal</em> API ({@link CompoundTag#contains}, {@code getCompound}, {@code
 * getList}, {@code getInt}) is identical across both versions, so the compat patches keep walking
 * plain {@code CompoundTag} trees exactly as they did on 1.20.1. Only the seams route through here:
 * <ul>
 *   <li>extracting the custom NBT off a stack ({@code stack.getTag()} → {@code CUSTOM_DATA});</li>
 *   <li>extracting block-entity NBT ({@code BlockEntityTag} → {@code BLOCK_ENTITY_DATA});</li>
 *   <li>reading the modern container component (live {@link ItemStack}s, no NBT round-trip);</li>
 *   <li>deserializing a nested stack ({@code ItemStack.of} → registry-aware {@code ItemStack.parse});</li>
 *   <li>computing a content signature for the weight cache key.</li>
 * </ul>
 *
 * <p>A future refactor could give the 1.20.1 module an interface of the same shape; for now this
 * keeps the DataComponents specifics in one place without touching the working 1.20.1 build.
 */
public final class ItemNbtBridge1211 {
    private static volatile Supplier<HolderLookup.Provider> registries = () -> null;

    private ItemNbtBridge1211() {
    }

    /**
     * Installed by the loader so nested-stack deserialization (1.21.1 needs the registries to parse
     * an {@link ItemStack} from NBT). Until set, {@link #loadStack(CompoundTag)} returns empty and
     * NBT-backed nested containers simply contribute no extra weight (graceful degradation).
     */
    public static void setRegistryAccess(Supplier<HolderLookup.Provider> supplier) {
        registries = supplier == null ? () -> null : supplier;
    }

    /** Custom NBT attached to the stack - the 1.20.1 {@code stack.getTag()} analogue. Never null. */
    public static CompoundTag customData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    /** Block-entity NBT stored on the stack - the 1.20.1 {@code BlockEntityTag} analogue. Never null. */
    public static CompoundTag blockEntityData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    /**
     * Live contents stored in the modern {@code minecraft:container} component (vanilla shulker
     * boxes and anything else using {@link ItemContainerContents}). These are already deserialized
     * {@link ItemStack}s, so no registry access or NBT parsing is needed.
     */
    public static List<ItemStack> containerContents(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return List.of();
        }
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents == null) {
            return List.of();
        }
        List<ItemStack> out = new ArrayList<>();
        contents.nonEmptyItems().forEach(out::add);
        return out;
    }

    /** Deserialize a nested stack compound - the 1.20.1 {@code ItemStack.of(tag)} analogue. */
    public static ItemStack loadStack(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return ItemStack.EMPTY;
        }
        HolderLookup.Provider provider = registries.get();
        if (provider == null) {
            return ItemStack.EMPTY;
        }
        return ItemStack.parse(provider, tag).orElse(ItemStack.EMPTY);
    }

    /** Cache signature reflecting the stack's data components - the 1.20.1 tag-hash analogue. */
    public static int signature(ItemStack stack) {
        return stack == null || stack.isEmpty() ? 0 : stack.getComponents().hashCode();
    }
}

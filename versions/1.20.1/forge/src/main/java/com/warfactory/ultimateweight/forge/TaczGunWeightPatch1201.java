package com.warfactory.ultimateweight.forge;

import com.tacz.guns.api.item.IGun;
import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.v1201.WeightViews1201;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalDouble;

/**
 * Per-gun weight provider for TACZ (Timeless &amp; Classics Zero).
 *
 * <p>Every TACZ gun is the same registered item ({@code tacz:modern_kinetic_gun}); the specific gun
 * is stored in the stack's NBT under {@code GunId} - a resource location such as {@code tacz:ak47} -
 * read here through the {@link IGun} API. Because the config resolver keys weights by item id, it
 * cannot tell one gun from another on its own. This provider bridges that gap by resolving a
 * configured weight using the <em>gun id</em> as the lookup key.
 *
 * <p>Server owners therefore set individual gun weights straight in the normal weight config, putting
 * the gun id where an item id would go:
 * <pre>
 * rules:
 *   wildcards:
 *     tacz:modern_kinetic_gun: 4.0   # blanket fallback for any gun (a normal item rule)
 *     tacz:ak47: 8.0                 # per-gun override, matched by GunId
 *     tacz:glock_17: 1.1
 * </pre>
 *
 * <p>When a gun has no gun-id-specific rule the provider abstains ({@link OptionalDouble#empty()}), so
 * resolution falls through to the item-level rule for {@code tacz:modern_kinetic_gun} (or the default
 * weight) exactly as if this plugin were absent.
 *
 * <p>Gated behind {@code requiredMods = "tacz"}, so the direct {@link IGun} reference is only loaded
 * when TACZ is present.
 */
@CompatPlugin(requiredMods = "tacz")
@SuppressWarnings("unused")
public final class TaczGunWeightPatch1201 implements IWeightCompatProvider {

    @Override
    public OptionalDouble getUnitWeight(Object rawStack) {
        if (!(rawStack instanceof ItemStack stack) || stack.isEmpty()) {
            return OptionalDouble.empty();
        }

        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return OptionalDouble.empty();
        }

        ResourceLocation gunId = gun.getGunId(stack);
        if (gunId == null) {
            return OptionalDouble.empty();
        }

        // Look the gun up by its GunId; abstains when no gun-specific rule is configured, letting the
        // normal item-level resolution handle tacz:modern_kinetic_gun.
        return WeightViews1201.configuredWeightForId(gunId.toString());
    }

    @Override
    public int getPriority() {
        // Consulted early alongside the other item-specific patches (Superb Warfare = 300). Guns do
        // not match the generic nested-container providers, so ordering is mostly for clarity.
        return 300;
    }
}

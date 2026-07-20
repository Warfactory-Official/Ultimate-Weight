package com.warfactory.ultimateweight.forge;

import com.tacz.guns.api.item.IAmmo;
import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.v1201.WeightViews1201;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalDouble;

/**
 * Per-ammo weight provider for TACZ (Timeless &amp; Classics Zero).
 *
 * <p>Every TACZ round is the same registered item ({@code tacz:ammo}); the specific ammo type is
 * stored in the stack's NBT under {@code AmmoId} - a resource location such as {@code tacz:12_gauge} -
 * read here through the {@link IAmmo} API. Because the config resolver keys weights by item id, it
 * cannot tell one round from another on its own. This provider bridges that gap by resolving a
 * configured weight using the <em>ammo id</em> as the lookup key. The value is a single-round weight;
 * the framework multiplies it by the stack count as usual.
 *
 * <p>Server owners therefore set individual ammo weights straight in the normal weight config, putting
 * the ammo id where an item id would go:
 * <pre>
 * rules:
 *   wildcards:
 *     tacz:ammo: 0.02        # blanket fallback for any round (a normal item rule)
 *     tacz:12_gauge: 0.05    # per-ammo override, matched by AmmoId
 *     tacz:9mm: 0.012
 * </pre>
 *
 * <p>When a round has no ammo-id-specific rule the provider abstains ({@link OptionalDouble#empty()}),
 * so resolution falls through to the item-level rule for {@code tacz:ammo} (or the default weight)
 * exactly as if this plugin were absent.
 *
 * <p>Gated behind {@code requiredMods = "tacz"}, so the direct {@link IAmmo} reference is only loaded
 * when TACZ is present.
 */
@CompatPlugin(requiredMods = "tacz")
@SuppressWarnings("unused")
public final class TaczAmmoWeightPatch1201 implements IWeightCompatProvider {

    @Override
    public OptionalDouble getUnitWeight(Object rawStack) {
        if (!(rawStack instanceof ItemStack stack) || stack.isEmpty()) {
            return OptionalDouble.empty();
        }

        IAmmo ammo = IAmmo.getIAmmoOrNull(stack);
        if (ammo == null) {
            return OptionalDouble.empty();
        }

        ResourceLocation ammoId = ammo.getAmmoId(stack);
        if (ammoId == null) {
            return OptionalDouble.empty();
        }

        // Look the round up by its AmmoId; abstains when no ammo-specific rule is configured, letting
        // the normal item-level resolution handle tacz:ammo.
        return WeightViews1201.configuredWeightForId(ammoId.toString());
    }

    @Override
    public int getPriority() {
        // Consulted early alongside the other item-specific patches (TACZ guns and Superb Warfare =
        // 300). Ammo does not match the generic nested-container providers, so ordering is mostly for
        // clarity.
        return 300;
    }
}

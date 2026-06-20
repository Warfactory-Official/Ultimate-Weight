# Compatibility Plugin Framework

Ultimate Weight discovers third-party mod support through **annotated plugin classes**. To add
support for a new mod you write one self-contained class, mark it with `@CompatPlugin`, and rebuild.
There is no central list to edit and no entrypoint to wire — the build indexes your plugin and the
mod loads it automatically when (and only when) the target mod is present.

This is how every built-in integration works: GregTech, Storage Drawers, HBM, Superb Warfare,
Traveler's Backpack, Sophisticated Backpacks, Baubles, and Curios are all plugins.

---

## How it works

1. **You** annotate a class with `@CompatPlugin(...)`.
2. **At build time**, an annotation processor (`:compat-processor`) records every annotated class —
   along with its mod gating — into a tiny index resource baked into the jar:
   `META-INF/wfweight/compat-plugins.txt`.
3. **At load time**, each loader entrypoint calls `WeightCompatBootstrap.run(...)`. The bootstrap
   reads the index, keeps only the plugins whose required mods are loaded, sorts them, then loads and
   activates them.

Because the gating lives in the index, a plugin that references classes from a mod you don't have
installed is **never loaded** — so it is always safe to reference the target mod's classes directly
(or via reflection, as some built-ins do for robustness).

> **Discovery is build-time.** After adding or changing a plugin you must rebuild for it to appear.
> The index is regenerated automatically by `compileJava`.

---

## The `@CompatPlugin` annotation

`com.warfactory.ultimateweight.api.CompatPlugin`

| Field          | Type       | Default | Meaning |
|----------------|------------|---------|---------|
| `requiredMods` | `String[]` | `{}`    | The plugin activates only if **all** of these mod ids are loaded. |
| `anyOf`        | `String[]` | `{}`    | The plugin activates only if **at least one** of these mod ids is loaded. Combined with `requiredMods` by AND. |
| `priority`     | `int`      | `0`     | Plugin **activation** order; higher runs first. |
| `id`           | `String`   | `""`    | Stable id shown in debug logs. Defaults to the simple class name. |

`requiredMods` is for single-mod support (`requiredMods = "gtceu"`). `anyOf` is for support shared
between mods — e.g. a worn-slot source that should turn on if *either* Curios *or* Traveler's
Backpack is present (`anyOf = {"curios", "travelersbackpack"}`). An empty annotation (`@CompatPlugin`)
means "always active".

> **Two different priorities.** `@CompatPlugin(priority = …)` controls the order plugins are
> *activated*. The order in which weight providers are *consulted* during weight resolution is still
> governed by `IWeightCompatProvider.getPriority()` (see below). For a plugin that only registers a
> weight provider, the annotation `priority` is irrelevant — set the provider's `getPriority()`.

---

## Two ways to write a plugin

### 1. Just a weight provider (the common case)

If all your plugin does is compute the weight of a particular item (a nested container, an ammo box,
a drawer block…), implement `IWeightCompatProvider` and annotate it. **Nothing else is needed** — the
bootstrap auto-registers any annotated `IWeightCompatProvider`.

```java
package com.warfactory.ultimateweight.v1201.compat;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1201.WeightViews1201;
import net.minecraft.world.item.ItemStack;
import java.util.OptionalDouble;

@CompatPlugin(requiredMods = "mymod")
public final class MyContainerPatch1201 implements IWeightCompatProvider {

    @Override
    public OptionalDouble getUnitWeight(Object rawStack) {
        if (!(rawStack instanceof ItemStack stack) || stack.isEmpty()) {
            return OptionalDouble.empty();
        }
        // Return OptionalDouble.empty() for stacks you don't handle so other providers get a turn.
        // Use WeightResolutionContext.currentDepth() + WeightViews1201.maxNestedDepth() to bound
        // recursion, and WeightViews1201.stackWeight(nested, depth + 1) to weigh contents.
        ...
        return OptionalDouble.of(totalKg);
    }

    @Override
    public int getPriority() {
        return 250; // higher = consulted earlier during weight resolution
    }
}
```

### 2. A full plugin (inventory sources, dynamic containers)

If your plugin needs to do more than weigh one item — for example add items the vanilla inventory
scan misses (extra equip slots, a worn backpack), or mark an item whose contents change invisibly —
implement `WeightCompatPlugin` and use the registration context.

```java
@CompatPlugin(anyOf = {"curios", "travelersbackpack"})
public final class WornSlotsPlugin1201 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        // Cast to the version's context to reach the typed hooks.
        ((CompatContext1201) context).registerInventorySource(BackpackSupport1201::collectWorn);
    }
}
```

`WeightCompatContext` (shared) always offers:

- `registerWeightProvider(IWeightCompatProvider provider)` — same as annotating a provider, but at runtime.
- `boolean isModLoaded(String modId)`

Each Minecraft version's context adds typed hooks on top (cast to it):

| Version | Context class | Extra hooks |
|---------|---------------|-------------|
| 1.20.1  | `CompatContext1201` | `registerInventorySource(WeightViews1201.InventorySource)`, `markDynamicContainer(Predicate<ItemStack>)` |
| 1.12.2  | `CompatContext1122` | `registerInventorySource(WeightViews1122.InventorySource)`, `markDynamicContainer(Predicate<ItemStack>)` |

`markDynamicContainer` predicates are OR-composed, so multiple plugins can each contribute one.

---

## Inventory sources

An **inventory source** contributes items that are not part of the vanilla main/armor/offhand
inventory but should still count toward the player's weight — Baubles/Curios slots, a backpack worn
in its own capability and that backpack's cargo.

The source shape differs slightly per version:

**1.20.1** — collect plain stacks into a list:

```java
public interface InventorySource {           // WeightViews1201.InventorySource
    void collect(Player player, List<ItemStack> out);
}
```

**1.12.2** — deposit into a sink that distinguishes equipment from cargo:

```java
public interface InventorySource {           // WeightViews1122.InventorySource
    void collect(EntityPlayer player, InventorySink sink);
}
public interface InventorySink {
    void addWorn(ItemStack stack);      // worn item: counts toward total weight AND equipment bonuses
    void addWornBase(ItemStack stack);  // worn item at base weight only (no nested-content resolution)
    void addCargo(ItemStack stack);     // cargo inside a worn container: counts toward total weight only
}
```

Use `addWornBase` for a worn container whose contents you enumerate separately as `addCargo` (so the
container isn't counted once for itself and again for its contents).

---

## Dynamic containers

A **dynamic container** is an item whose contents are capability- or save-data-backed rather than
stored in the item tag (most backpacks). Marking one tells the weight system to (a) force a full
rescan when it changes instead of trusting a single-slot numeric delta, and (b) bypass the tag-hash
weight cache when its contents live outside the tag.

```java
@CompatPlugin(anyOf = {"sophisticatedbackpacks", "travelersbackpack"})
public final class DynamicBackpackPlugin1201 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        ((CompatContext1201) context).markDynamicContainer(BackpackSupport1201::isBackpack);
    }
}
```

---

## Worked example: an extra-slot mod (Baubles-style)

Goal: count every item a player has equipped in a mod's extra slots. (This is exactly how
`BaublesWornPlugin1122` works.)

```java
@CompatPlugin(requiredMods = "myslots")
public final class MySlotsPlugin1122 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        ((CompatContext1122) context).registerInventorySource((player, sink) -> {
            for (ItemStack equipped : MySlotsApi.getEquipped(player)) {
                sink.addWorn(equipped); // counts as weight and as equipment
            }
        });
    }
}
```

That's the whole integration. The equipped items now contribute to total weight and to equipment
bonuses, and each resolves its own weight normally (so a backpack in a slot still gets its contents
weighed by whatever provider handles it).

## Worked example: a player-capability backpack (Traveler's-style)

Goal: support a backpack worn in the mod's own capability (invisible to the vanilla inventory),
whose cargo lives in that capability. (This is `TravelersWornPlugin1122` plus
`TravelersBackpackWeightPatch1122`.)

```java
@CompatPlugin(requiredMods = "mypack")
public final class MyPackWornPlugin1122 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        CompatContext1122 ctx = (CompatContext1122) context;
        ctx.registerInventorySource((player, sink) -> {
            ItemStack worn = MyPackApi.getWornPack(player);
            if (!worn.isEmpty()) {
                sink.addWornBase(worn);                       // the pack item itself, base weight
            }
            for (ItemStack cargo : MyPackApi.getContents(player)) {
                sink.addCargo(cargo);                         // its stored cargo
            }
        });
        // Equipping/unequipping moves the pack through an invisible capability slot, so force a
        // rescan rather than trusting a slot delta:
        ctx.markDynamicContainer(MyPackApi::isPack);
    }
}
```

A **loose** pack sitting in the main inventory is a separate concern — handle it with an
`IWeightCompatProvider` that reads the pack's NBT contents, exactly like the worn case above reads
the capability.

---

## Where plugins live & gotchas

- **Module:** put a plugin in the module that has access to the APIs it needs. Loader-agnostic
  providers go in the version's `common` module (`versions/1.20.1/common/.../compat`); plugins that
  need loader-only APIs (Curios, Forge capabilities) go in the loader module
  (`versions/1.20.1/forge/...`). On 1.12.2 there is a single module
  (`versions/1.12.2/.../v1122/compat`).
- **Constructor:** a plugin needs a public no-arg constructor.
- **Be lenient:** an `IWeightCompatProvider` should return `OptionalDouble.empty()` for stacks it
  doesn't recognize, so other providers still get a chance.
- **One bad plugin can't break the rest:** activation is isolated per-plugin; a throwing plugin is
  logged and skipped.
- **Fabric note:** the 1.20.1 Fabric build auto-discovers every common plugin. Curios/Traveler's
  worn-slot support is Forge-only today (the worn-slot plugins live in the Forge module); adding a
  Fabric equivalent is just one more `@CompatPlugin` class in the Fabric module.

## Reference

| Piece | Location |
|-------|----------|
| `@CompatPlugin` annotation | `shared/.../api/CompatPlugin.java` |
| `IWeightCompatProvider` | `shared/.../api/IWeightCompatProvider.java` |
| `WeightCompatPlugin` SPI | `shared/.../api/WeightCompatPlugin.java` |
| `WeightCompatContext` | `shared/.../api/WeightCompatContext.java` |
| Discovery + activation | `shared/.../compat/WeightCompatBootstrap.java`, `CompatPluginIndex.java` |
| Annotation processor | `compat-processor/.../CompatPluginProcessor.java` |
| Index resource | `META-INF/wfweight/compat-plugins.txt` (generated) |

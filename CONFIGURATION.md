# Configuration Reference

`WarFactory Ultimate Weight` uses version-specific configuration files.

- `1.12.2` uses `config/wfweight/weight_config_1_12.yaml`
- `1.20.1` uses `config/wfweight/weight_config_modern.yaml`

There is no unified parser between legacy and modern Minecraft versions. The file formats are intentionally different where the game model is different.

## Contents

1. Configuration Model
2. Resolution Behavior
3. Shared Sections
4. Legacy `1.12.2` Rules
5. Modern `1.20.1+` Rules
6. Inventory Group Limits
7. Equipment Bonuses
8. Tooltips And HUD
9. Full Examples
10. Practical Notes

## Configuration Model

The server configuration is authoritative.

- On multiplayer servers, the server sends the active configuration to clients.
- Client HUD and tooltip behavior follow the synced server config while connected.
- Local config files are loaded again after disconnect.

Both config files share the same top-level section layout:

- `precision`
- `limits`
- `rules`
- `groupLimits`
- `equipmentBonuses`
- `movement`
- `fallDamage`
- `stamina`

The difference is inside `rules` and in how item identity is written.

## Resolution Behavior

Weight resolution follows this order:

1. Hardcoded `uWeight` numeric NBT tag on the stack
2. Complex item cache for previously scanned NBT-heavy stacks
3. Registered compat providers for nested inventories and similar items
4. Exact item rule
5. Wildcard item rule
6. OreDictionary match on `1.12.2`, tag match on modern versions
7. Fallback to `0`

Important behavior:

- `uWeight` is hardcoded and not configurable.
- A matching `uWeight` value is treated as the final single-item weight.
- Nested inventory items are cached to avoid rescanning deep NBT structures on every inventory update.
- Equipment bonuses do not change rule matching order. They are applied after item weights are resolved.

## Shared Sections

These sections exist in both config formats.

### `precision`

Controls formatting only. It does not affect calculations.

```yaml
precision:
  hudDecimals: 1
  tooltipDecimals: 1
  stackDecimals: 2
```

Fields:

- `hudDecimals`
  Decimal places used in the HUD for total weight and stamina values.
- `tooltipDecimals`
  Decimal places used for per-item tooltip weight values.
- `stackDecimals`
  Decimal places used for full-stack tooltip weight values.

### `limits`

Controls global carry limits and full-scan safety.

```yaml
limits:
  defaultCarryCapacityKg: 120.0
  hardLockWeightKg: 220.0
  enableFailsafeFullScan: true
  fullScanIntervalTicks: 600
```

Fields:

- `defaultCarryCapacityKg`
  Base carry capacity before equipment bonuses.
- `hardLockWeightKg`
  Hard pickup and transfer block threshold.
- `enableFailsafeFullScan`
  Enables periodic full inventory recalculation even when delta tracking is active.
- `fullScanIntervalTicks`
  Number of ticks between forced full scans when the failsafe is enabled.

Behavior:

- If total inventory weight reaches or exceeds `hardLockWeightKg`, pickups and transfers are blocked.
- Equipment `carryCapacityKg` bonuses do not change `hardLockWeightKg`.
- Equipment `carryCapacityKg` bonuses do change load percentage and therefore movement and stamina scaling.

### `movement`

Controls slowdown and jump penalties by load percentage.

```yaml
movement:
  thresholds:
    - percent: 0.5
      speedMultiplier: 0.92
      jumpMultiplier: 0.96
    - percent: 0.75
      speedMultiplier: 0.78
      jumpMultiplier: 0.86
    - percent: 1.0
      speedMultiplier: 0.55
      jumpMultiplier: 0.65
    - percent: 1.2
      speedMultiplier: 0.18
      jumpMultiplier: 0.25
```

Fields:

- `percent`
  Load percentage threshold. Example: `1.0` means `100%` of current effective carry capacity.
- `speedMultiplier`
  Movement speed multiplier at or above this threshold.
- `jumpMultiplier`
  Jump multiplier at or above this threshold.

Behavior:

- Thresholds are evaluated in ascending order.
- The last matching threshold wins.
- Load percentage is `totalWeightKg / effectiveCarryCapacityKg`.
- Effective carry capacity includes equipped item bonuses.

### `fallDamage`

Controls additional fall damage caused by high load.

```yaml
fallDamage:
  enabled: true
  startLoadPercent: 0.85
  extraDamageMultiplierPerLoadPercent: 1.2
  hardLockMultiplierBonus: 0.75
  maxDamageMultiplier: 3.5
```

Fields:

- `enabled`
  Enables or disables the system.
- `startLoadPercent`
  Extra fall damage starts only above this load percentage.
- `extraDamageMultiplierPerLoadPercent`
  Extra multiplier added per `1.0` load above the start threshold.
- `hardLockMultiplierBonus`
  Extra fall damage added while hard-locked.
- `maxDamageMultiplier`
  Hard cap for the final fall damage multiplier.

### `stamina`

Controls sprint drain, jump drain, regeneration, exhaustion, and weight-scaled stamina use.

```yaml
stamina:
  totalStamina: 100.0
  sprintStaminaLossRate: 0.1
  jumpStaminaLoss: 2.0
  staminaGainRate: 0.08
  exhaustionThreshold: 1.0
  recoveryPercent: 0.3
  drainWhileRunning: true
  drainOnJump: true
  penalties:
    - percent: 0.5
      useMultiplier: 1.1
    - percent: 0.75
      useMultiplier: 1.35
    - percent: 1.0
      useMultiplier: 1.7
    - percent: 1.2
      useMultiplier: 2.2
```

Fields:

- `totalStamina`
  Base maximum stamina before equipment bonuses.
- `sprintStaminaLossRate`
  Per-tick stamina drain while sprinting.
- `jumpStaminaLoss`
  Stamina consumed on jump.
- `staminaGainRate`
  Per-tick stamina regeneration while not draining.
- `exhaustionThreshold`
  Absolute stamina value at or below which the player becomes exhausted.
- `recoveryPercent`
  Fraction of max stamina required to recover from exhaustion.
- `drainWhileRunning`
  Enables sprint drain.
- `drainOnJump`
  Enables jump drain.
- `penalties`
  Load-percentage-based multipliers applied to stamina costs.

Penalty behavior:

- The last matching penalty rule wins.
- Load percentage uses effective carry capacity, including equipment bonuses.
- Equipment stamina bonuses increase max stamina.
- If effective max stamina becomes `0` or lower, stamina is effectively disabled.

## Legacy `1.12.2` Rules

Legacy uses metadata-sensitive exact rules and OreDictionary matching.

```yaml
rules:
  exact:
    minecraft:water_bucket@0: 4.5
    minecraft:lava_bucket@0: 4.8
  wildcards:
    minecraft:shulker_box: 8.0
  oredict:
    ingotIron: 0.9
    logWood: 2.4
```

### `rules.exact`

Maps `item_id@meta` to single-item weight in kilograms.

Examples:

- `minecraft:wool@0`
- `minecraft:stone@3`
- `modid:item_name@12`

Notes:

- The canonical legacy exact format is `item_id@meta`.
- The loader also accepts legacy-style exact entries with `@*` and treats them as wildcards.
- Exact rules ignore NBT.

### `rules.wildcards`

Maps `item_id` to single-item weight in kilograms.

Examples:

- `minecraft:shulker_box`
- `minecraft:potion`
- `modid:machine_casing`

Behavior:

- Matches every metadata value for that item id.
- Still ignores NBT.

### `rules.oredict`

Maps OreDictionary keys to single-item weight in kilograms.

Examples:

- `ingotIron`
- `plankWood`
- `dustRedstone`

Behavior:

- Applied only after exact and wildcard rules fail.
- A stack can have multiple OreDictionary keys.
- The first matching key encountered by the stack view wins.

## Modern `1.20.1+` Rules

Modern uses flattened ids and item tags.

```yaml
rules:
  exact:
    minecraft:water_bucket: 4.5
    minecraft:elytra: 6.0
  wildcards: {}
  tags:
    minecraft:planks: 0.45
    forge:ingots/iron: 0.9
    c:gems/diamond: 0.35
```

### `rules.exact`

Maps `item_id` to single-item weight in kilograms.

Examples:

- `minecraft:diamond`
- `minecraft:netherite_ingot`
- `modid:steel_plate`

Behavior:

- Metadata no longer exists as a separate matching key in modern versions.
- Exact rules ignore NBT.

### `rules.wildcards`

Maps `item_id` to single-item weight in kilograms.

Behavior:

- On modern versions, wildcard entries are functionally the same shape as exact ids because there is no metadata split.
- This section still exists for schema parity and future flexibility.

### `rules.tags`

Maps full item tag ids to single-item weight in kilograms.

Examples:

- `minecraft:planks`
- `forge:ingots/iron`
- `c:ingots/iron`
- `forge:dusts/redstone`

Behavior:

- Applied only after exact and wildcard rules fail.
- The stack can contribute multiple tag ids.
- The first matching tag encountered wins.

## Inventory Group Limits

Group limits let you define shared counted pools across unrelated items.

Example use case:

- Put all potions and all leaf blocks into a group called `fragile_supplies`
- Set the limit to `10`
- A player can carry any combination up to `10` total items from that group
- `2` potions plus `8` leaf blocks is allowed
- Adding one more matching item is blocked

Counts are based on item counts, not occupied slots.

If an item stack has `64` items and belongs to a group, it contributes `64` to that group total.

### Legacy format

```yaml
groupLimits:
  fragile_supplies:
    label: Fragile Supplies
    limit: 10
    exact:
      - minecraft:potion@0
    wildcards:
      - minecraft:leaves
    oredict:
      - treeLeaves
```

### Modern format

```yaml
groupLimits:
  fragile_supplies:
    label: Fragile Supplies
    limit: 10
    exact:
      - minecraft:potion
    wildcards: []
    tags:
      - minecraft:leaves
      - forge:potions
```

Fields:

- `label`
  Display name used in tooltips and block messages.
- `limit`
  Base maximum total item count for this group.
- `exact`
  Exact members of the group.
- `wildcards`
  Wildcard members of the group.
- `oredict`
  Legacy-only OreDictionary membership.
- `tags`
  Modern-only tag membership.

Example snippets:

Legacy `1.12.2` alchemy and ammo caps:

```yaml
groupLimits:
  alchemy:
    label: Alchemy Supplies
    limit: 16
    exact:
      - minecraft:potion@0
      - minecraft:splash_potion@0
    wildcards: []
    oredict:
      - dustRedstone
      - dustGlowstone

  ammunition:
    label: Ammunition
    limit: 192
    exact: []
    wildcards:
      - minecraft:arrow
      - minecraft:spectral_arrow
      - minecraft:tipped_arrow
    oredict: []
```

Modern `1.20.1+` medicine and explosives caps:

```yaml
groupLimits:
  field_medicine:
    label: Field Medicine
    limit: 12
    exact:
      - minecraft:potion
      - minecraft:splash_potion
      - minecraft:lingering_potion
    wildcards: []
    tags:
      - c:golden_apples
      - forge:golden_apples

  explosives:
    label: Explosives
    limit: 48
    exact:
      - minecraft:tnt
      - minecraft:end_crystal
    wildcards: []
    tags:
      - c:gunpowder
      - forge:gunpowder
```

Combined behavior example:

- If `explosives.limit` is `48`, then `32` TNT plus `16` gunpowder is allowed.
- Trying to add one more matching explosive item is blocked.
- If a worn item grants `groupLimits.explosives: 16`, the effective cap becomes `64`.

Behavior:

- Group checks happen before or during transfer validation depending on the version hook.
- Group checks also apply to item pickup.
- If an item matches multiple groups, it counts toward all of them.
- Group limits are increased by equipped item bonuses.
- Group limits count the entire inventory set used by that version.

Version-specific inventory scope:

- `1.12.2`
  Main inventory, armor, offhand, and baubles if the Baubles mod is present.
- `1.20.1`
  Main inventory, armor, and offhand.

## Equipment Bonuses

Equipment bonuses apply when the item is worn or equipped, not merely carried.

Supported bonus types:

- extra carry capacity
- extra max stamina
- extra group limits

### Legacy format

```yaml
equipmentBonuses:
  exact:
    minecraft:diamond_chestplate@0:
      carryCapacityKg: 25.0
      stamina: 10.0
      groupLimits:
        fragile_supplies: 5
  wildcards:
    baubles:ring:
      carryCapacityKg: 5.0
      stamina: 0.0
      groupLimits: {}
```

### Modern format

```yaml
equipmentBonuses:
  exact:
    minecraft:diamond_chestplate:
      carryCapacityKg: 25.0
      stamina: 10.0
      groupLimits:
        fragile_supplies: 5
  wildcards:
    minecraft:elytra:
      carryCapacityKg: 0.0
      stamina: 15.0
      groupLimits: {}
```

Fields:

- `carryCapacityKg`
  Added to `limits.defaultCarryCapacityKg` while equipped.
- `stamina`
  Added to `stamina.totalStamina` while equipped.
- `groupLimits`
  Map of `group_id -> bonus amount`.

Example snippets:

Legacy `1.12.2` armor and bauble bonuses:

```yaml
equipmentBonuses:
  exact:
    minecraft:diamond_chestplate@0:
      carryCapacityKg: 25.0
      stamina: 10.0
      groupLimits:
        ammunition: 64

    minecraft:elytra@0:
      carryCapacityKg: 0.0
      stamina: 20.0
      groupLimits: {}

  wildcards:
    baubles:ring:
      carryCapacityKg: 8.0
      stamina: 5.0
      groupLimits:
        alchemy: 4
```

Modern `1.20.1+` armor bonuses:

```yaml
equipmentBonuses:
  exact:
    minecraft:netherite_chestplate:
      carryCapacityKg: 35.0
      stamina: 12.0
      groupLimits:
        explosives: 16

    minecraft:turtle_helmet:
      carryCapacityKg: 5.0
      stamina: 8.0
      groupLimits:
        field_medicine: 2

  wildcards:
    minecraft:elytra:
      carryCapacityKg: 0.0
      stamina: 20.0
      groupLimits: {}
```

Stacking example:

- A player with `limits.defaultCarryCapacityKg: 120.0`, a `+35.0` chestplate, and a `+5.0` helmet has `160.0` effective carry capacity.
- If both items add to the same group, their bonuses stack. `explosives: 16` plus `explosives: 8` becomes `24`.
- Equipment bonuses only apply while the item is actually equipped in a supported slot.

Behavior:

- Bonuses are matched by exact item id or wildcard item id only.
- Equipment bonus rules do not use OreDictionary or tags.
- Bonuses stack across multiple equipped items.
- Group limit bonuses are additive per group.
- Carry capacity bonuses affect load percentage, movement, jump penalties, fall damage scaling, and stamina usage multipliers.
- Stamina bonuses increase max stamina and recovery thresholds.

Equipped-slot coverage:

- `1.12.2`
  Armor and baubles if Baubles is installed.
- `1.20.1`
  Armor only.

Weight behavior:

- Equipped armor still has weight.
- Equipped baubles on `1.12.2` also have weight.
- Equipment bonuses do not cancel equipment weight unless you explicitly lower the item’s configured weight.

## Tooltips And HUD

Item tooltips can show:

- item weight
- stack weight
- group memberships and current configured cap
- carry capacity bonus
- stamina bonus
- group limit bonus

Examples:

- `Weight: 2.5kg`
- `Stack Weight: 40.0kg`
- `Group: Fragile Supplies (10 max)`
- `Carry Capacity Bonus: +25.0kg`
- `Max Stamina Bonus: +10.0`
- `Group Limit Bonus: Fragile Supplies +5`

The HUD displays:

- current total weight
- current load percent
- stamina values when stamina is enabled

## Full Examples

### Example `1.12.2` Config

```yaml
precision:
  hudDecimals: 1
  tooltipDecimals: 1
  stackDecimals: 2

limits:
  defaultCarryCapacityKg: 120.0
  hardLockWeightKg: 150.0
  enableFailsafeFullScan: false
  fullScanIntervalTicks: 600

rules:
  exact:
    minecraft:potion@0: 0.6
    minecraft:diamond_chestplate@0: 8.0
  wildcards:
    minecraft:leaves: 0.15
  oredict:
    treeLeaves: 0.15

groupLimits:
  fragile_supplies:
    label: Fragile Supplies
    limit: 10
    exact:
      - minecraft:potion@0
    wildcards:
      - minecraft:leaves
    oredict:
      - treeLeaves

equipmentBonuses:
  exact:
    minecraft:diamond_chestplate@0:
      carryCapacityKg: 25.0
      stamina: 10.0
      groupLimits:
        fragile_supplies: 5
  wildcards:
    baubles:ring:
      carryCapacityKg: 5.0
      stamina: 0.0
      groupLimits: {}

movement:
  thresholds:
    - percent: 0.5
      speedMultiplier: 0.92
      jumpMultiplier: 0.96
    - percent: 1.0
      speedMultiplier: 0.55
      jumpMultiplier: 0.65

fallDamage:
  enabled: true
  startLoadPercent: 0.85
  extraDamageMultiplierPerLoadPercent: 1.2
  hardLockMultiplierBonus: 0.75
  maxDamageMultiplier: 3.5

stamina:
  totalStamina: 100.0
  sprintStaminaLossRate: 0.1
  jumpStaminaLoss: 2.0
  staminaGainRate: 0.08
  exhaustionThreshold: 1.0
  recoveryPercent: 0.3
  drainWhileRunning: true
  drainOnJump: true
  penalties:
    - percent: 0.5
      useMultiplier: 1.1
    - percent: 1.0
      useMultiplier: 1.7
```

### Example Modern Config

```yaml
precision:
  hudDecimals: 1
  tooltipDecimals: 1
  stackDecimals: 2

limits:
  defaultCarryCapacityKg: 120.0
  hardLockWeightKg: 220.0
  enableFailsafeFullScan: true
  fullScanIntervalTicks: 600

rules:
  exact:
    minecraft:potion: 0.6
    minecraft:diamond_chestplate: 8.0
  wildcards: {}
  tags:
    minecraft:leaves: 0.15
    forge:potions: 0.6

groupLimits:
  fragile_supplies:
    label: Fragile Supplies
    limit: 10
    exact:
      - minecraft:potion
    wildcards: []
    tags:
      - minecraft:leaves
      - forge:potions

equipmentBonuses:
  exact:
    minecraft:diamond_chestplate:
      carryCapacityKg: 25.0
      stamina: 10.0
      groupLimits:
        fragile_supplies: 5
  wildcards: {}

movement:
  thresholds:
    - percent: 0.5
      speedMultiplier: 0.92
      jumpMultiplier: 0.96
    - percent: 1.0
      speedMultiplier: 0.55
      jumpMultiplier: 0.65

fallDamage:
  enabled: true
  startLoadPercent: 0.85
  extraDamageMultiplierPerLoadPercent: 1.2
  hardLockMultiplierBonus: 0.75
  maxDamageMultiplier: 3.5

stamina:
  totalStamina: 100.0
  sprintStaminaLossRate: 0.1
  jumpStaminaLoss: 2.0
  staminaGainRate: 0.08
  exhaustionThreshold: 1.0
  recoveryPercent: 0.3
  drainWhileRunning: true
  drainOnJump: true
  penalties:
    - percent: 0.5
      useMultiplier: 1.1
    - percent: 1.0
      useMultiplier: 1.7
```

## Practical Notes

### Choosing between exact, wildcard, and dictionary or tag rules

Use:

- `exact` when a single item should have a custom weight
- `wildcards` when every metadata variant of a legacy item or a whole item id should share a weight
- `oredict` or `tags` when many mods should be covered by shared material categories

### Group limit design

Keep groups focused and intentional.

Good examples:

- potions
- explosives
- food supplies
- rare ores
- leaves and botanicals

Avoid putting huge unrelated categories into the same group unless you want them to compete directly.

### Equipment bonus design

Recommended pattern:

- make heavy armor actually heavy
- then give it carry or stamina bonuses if the design calls for it

This produces meaningful tradeoffs instead of free power.

### Legacy Baubles support

On `1.12.2`, baubles are included only when the Baubles mod is actually installed.

That affects:

- inventory weight
- group counting
- equipped bonus evaluation

### Invalid or missing sections

Missing sections fall back to built-in defaults.

Empty maps such as:

```yaml
groupLimits: {}
equipmentBonuses:
  exact: {}
  wildcards: {}
```

mean the feature is enabled structurally but has no configured entries.

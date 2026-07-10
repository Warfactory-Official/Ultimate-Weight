# WarFactory Ultimate Weight

`WarFactory Ultimate Weight` is a multi-version Minecraft mod focused on inventory weight, movement penalties, and stamina.

It is designed around performance-first inventory tracking rather than the brute-force full inventory iteration used by many older weight mods and predecessors.

It is built around a shared core with version-specific runtime hooks. The mod currently targets:

| Minecraft | Loaders                    |
|-----------|----------------------------|
| `1.12.2`  | Legacy Forge               |
| `1.20.1`  | Fabric, Forge              |
| `1.21.1`  | NeoForge, LexForge, Fabric |

[//]: # (| `26.1` | Fabric |)

## Features

- Configurable item and inventory weight system
- HUD and tooltip weight display
- Weight-based movement and jump penalties
- Hard-lock threshold for blocking pickups and transfers
- Config sync from server to client
- Delta-based inventory weight updates where supported
- Stamina system with:
  `totalStamina`, `staminaLossRate`, `staminaGainRate`
- Weight-aware stamina drain penalties
- Optional stamina drain from sprinting and jumping
- Nested inventory weight support
- Mod compatibility patches and API for mod developers

## Supported Mods

Ultimate Weight reads the contents of modded storage items so an item's weight scales with what it holds, and counts items worn in extra equipment slots (Baubles / Curios). **Any item that exposes a standard item-handler capability is supported automatically**; the mods below additionally get dedicated handling for their custom storage. Compatibility patches load only when the target mod is present and are safe when it is absent.

> Support is modular: each integration is a single annotated `@CompatPlugin` class that the build auto-discovers. Adding a new mod — a nested container, an extra equip slot, or a capability-backed backpack — is one drop-in class. See **[`COMPATIBILITY.md`](./COMPATIBILITY.md)**.

### 1.12.2 (Legacy Forge)

| Mod | Supported |
|---|---|
| Baubles | Worn bauble items counted toward weight |
| Traveler's Backpack | Worn + carried backpack contents |
| Retro Sophisticated Backpacks | Backpack contents and installed upgrades |
| GregTech (CE Unofficial) | Machine / item inventories |
| HBM's Nuclear Tech | Storage crate contents |
| Storage Drawers | Drawer contents |
| *Any item with an `IItemHandler`* | Nested contents counted generically |

### 1.20.1 (Forge)

| Mod | Supported |
|---|---|
| Curios | Worn curio items counted toward weight |
| Sophisticated Backpacks | Worn (Curios/chest) + carried contents, click-to-stash |
| Traveler's Backpack | Worn + carried backpack contents |
| GregTechCEu Modern | Machine inventories, including crates |
| Superb Warfare | Ammo box (per-type ammo counts) |
| Storage Drawers | Drawer contents |
| TACZ (Timeless & Classics Zero) | Per-gun weight by `GunId` — all guns are one item, weighed individually |
| *Any item with an `IItemHandler`* | Nested contents counted generically |

Every integration above is a `@CompatPlugin` discovered at load time, and the same hooks are exposed as a public API so other mods (or you) can add support without forking — see [`COMPATIBILITY.md`](./COMPATIBILITY.md). On the 1.20.1 **Fabric** build, vanilla nested containers are counted, but the mod-specific handlers and the generic item-handler reader above are Forge-only.

### 1.21.1 (NeoForge, LexForge, Fabric)

| Mod | Supported |
|---|---|
| Curios | Worn curio items counted toward weight |
| Sophisticated Backpacks | Worn (Curios) + carried backpack contents |
| Traveler's Backpack | Worn + carried backpack contents |
| Storage Drawers | Drawer contents |
| KubeJS | Define item / inventory weights from scripts (NeoForge) |
| *Any item with an `IItemHandler`* | Nested contents counted generically (NeoForge, LexForge) |

Stored contents are read through the standard item-handler capability and the modern `minecraft:container` data component, so most modded containers and vanilla shulker boxes are counted automatically. On the 1.21.1 **Fabric** build there are no backpack-mod integrations bundled and the Forge item-handler reader is absent, but vanilla nested containers are still counted via the `minecraft:container` component; stamina persists across death, dimension change and relog through Fabric data attachments.

## Configuration

Configuration is version-specific:

- `1.12.2`
  `config/wfweight/weight_config_1_12.yaml`
- `1.20.1`
  `config/wfweight/weight_config_modern.yaml`
- `1.21.1`
  `config/wfweight/weight_config_modern.yaml`

Full reference:

- [`CONFIGURATION.md`](./CONFIGURATION.md)

## Project Layout

```text
shared/                         shared non-Minecraft logic (incl. compat plugin framework)
compat-processor/               build-time @CompatPlugin index generator
versions/
  1.12.2/                       Legacy Forge implementation
  1.20.1/
    common/                     shared 1.20.1 runtime
    fabric/
    forge/
  1.21.1/
    common/                     shared 1.21.1 runtime (DataComponents bridge)
    neoforge/
    lexforge/
    fabric/
```

## Building

```bash
./gradlew build
./gradlew :1.12.2:build
./gradlew :1.20.1:common:build
./gradlew :1.20.1:fabric:build
./gradlew :1.20.1:forge:build
./gradlew :1.21.1:common:build
./gradlew :1.21.1:neoforge:build
./gradlew :1.21.1:lexforge:build
./gradlew :1.21.1:fabric:build
```

For targeted compile checks during development:

```bash
./gradlew :1.12.2:compileJava
./gradlew :1.20.1:common:compileJava
./gradlew :1.20.1:fabric:compileJava
./gradlew :1.20.1:forge:compileJava
./gradlew :1.21.1:common:compileJava
./gradlew :1.21.1:neoforge:compileJava
./gradlew :1.21.1:lexforge:compileJava
./gradlew :1.21.1:fabric:compileJava
```


## License

This project is licensed under `GPLv3`.

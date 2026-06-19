# WarFactory Ultimate Weight

`WarFactory Ultimate Weight` is a multi-version Minecraft mod focused on inventory weight, movement penalties, and stamina.

It is designed around performance-first inventory tracking rather than the brute-force full inventory iteration used by many older weight mods and predecessors.

It is built around a shared core with version-specific runtime hooks. The mod currently targets:

| Minecraft          | Loaders          |
|--------------------|------------------|
| `1.12.2`           | Legacy Forge     |
| `1.20.1`           | Fabric, Forge    |
| `1.21.1 (planned)` | NeoForge, LexForge |

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
| *Any item with an `IItemHandler`* | Nested contents counted generically |

The same registry the patches use is exposed as a public API, so other mods can register their own weight providers. On the 1.20.1 **Fabric** build, vanilla nested containers are counted, but the mod-specific handlers and the generic item-handler reader above are Forge-only.

## Configuration

Configuration is version-specific:

- `1.12.2`
  `config/wfweight/weight_config_1_12.yaml`
- `1.20.1`
  `config/wfweight/weight_config_modern.yaml`

Full reference:

- [`CONFIGURATION.md`](./CONFIGURATION.md)

## Project Layout

```text
shared/                         shared non-Minecraft logic
versions/
  1.12.2/                       Legacy Forge implementation
  1.20.1/
    common/                     shared 1.20.1 runtime
    fabric/
    forge/
  1.21.1/
    neoforge/
    lexforge/
  26.1/
    src/
```

## Building

```bash
./gradlew build
./gradlew :1.12.2:build
./gradlew :1.20.1:common:build
./gradlew :1.20.1:fabric:build
./gradlew :1.20.1:forge:build
```

For targeted compile checks during development:

```bash
./gradlew :1.12.2:compileJava
./gradlew :1.20.1:common:compileJava
./gradlew :1.20.1:fabric:compileJava
./gradlew :1.20.1:forge:compileJava
```


## License

This project is licensed under `GPLv3`.

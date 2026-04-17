# WarFactory Ultimate Weight

`WarFactory Ultimate Weight` is a multi-version Minecraft mod focused on inventory weight, movement penalties, and stamina.

It is designed around performance-first inventory tracking rather than the brute-force full inventory iteration used by many older weight mods and predecessors.

It is built around a shared core with version-specific runtime hooks. The mod currently targets:

| Minecraft | Loaders |
|---|---|
| `1.12.2` | Legacy Forge |
| `1.20.1` | Fabric, Forge |
| `1.21.1` | NeoForge, LexForge |

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

## Current Focus

`1.12.2` is the frontier version.

That branch includes:

- player capability storage
- server/client sync for weight and stamina
- event-driven inventory delta updates
- nested item/container handling
- client-side transfer blocking to reduce ghosting
- compatibility patch loading without accidental optional-class crashes

`1.20.1` is still a work in progress.

It already has the shared runtime, HUD, syncing, stamina systems, and the current inventory hook refactor, but it should still be treated as an active port rather than the stable reference implementation.

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

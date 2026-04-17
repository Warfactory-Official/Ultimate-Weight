# WarFactory Ultimate Weight

`WarFactory Ultimate Weight` is a multi-version Minecraft mod focused on inventory weight, movement penalties, and stamina.

It is built around a shared core with version-specific runtime hooks. The mod currently targets:

| Minecraft | Loaders |
|---|---|
| `1.12.2` | Legacy Forge |
| `1.20.1` | Fabric, Forge |
| `1.21.1` | NeoForge, LexForge |
| `26.1` | Fabric |

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
- Mod compatibility patch system with safe mod detection

## Current Focus

The most complete implementation is currently `1.12.2`.

That branch includes:

- player capability storage
- server/client sync for weight and stamina
- event-driven inventory delta updates
- nested item/container handling
- client-side transfer blocking to reduce ghosting
- compatibility patch loading without accidental optional-class crashes

`1.20.1` has the core runtime, HUD, syncing, Forge capability support, and stamina ported over, but compatibility-specific nested integrations are intentionally separate.

## Configuration

The main config file is [`shared/src/main/resources/weight_config.yml`](/home/rawhav0kk/projects/Warfactory-Ultimate-Weight/shared/src/main/resources/weight_config.yml).

Key sections:

- `limits`
  carry capacity, hard lock, full-scan failsafe
- `rules`
  exact item weights, group weights, prefix weights, component override key
- `movement`
  slowdown and jump penalty thresholds
- `fallDamage`
  extra fall damage scaling from load
- `stamina`
  total stamina, drain/regen rates, run/jump toggles, load-based usage penalties

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

Current project metadata declares `GPLv3`.

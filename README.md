# ⚔️ Military Arsenal Legacy

[![Paper](https://img.shields.io/badge/Paper%20%2F%20Purpur-26.2%2B-blue)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25-orange)](https://adoptium.net/)
[![Slimefun Legacy](https://img.shields.io/badge/Slimefun-Legacy-brightgreen)](https://github.com/wickidcow/Slimefun-Legacy)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

Military Arsenal is a combat-focused Slimefun addon with military weapons, ammunition, advanced crafting, defensive turrets, war machines, upgrade systems, Void/antimatter progression, and coordinate-based bombardment.

This repository is a **Slimefun Legacy compatibility fork** maintained for the AlbionMC.com server environment. It preserves the core gameplay work of the original project while keeping the addon usable on current Paper/Purpur 26.2 and the Slimefun Legacy API surface.

> **Original project:** [Chagui68/Military-Arsenal-addon-for-Slimefun4](https://github.com/Chagui68/Military-Arsenal-addon-for-Slimefun4)  
> **Original developer:** [Chagui68](https://github.com/Chagui68)

## ❤️ Homage to Chagui68

**Military Arsenal exists because of the work of Chagui68.** The original concept, military progression, weapons, ammunition, machines, defensive turret systems, crafting systems, models, structures, and foundation of this addon were created by **Chagui68**.

This Slimefun Legacy repository is a compatibility and maintenance fork. Its purpose is to preserve that original work and keep it playable on newer Paper/Purpur and Slimefun Legacy environments; it is **not** intended to replace, take ownership of, or diminish the original project or its creator.

A sincere thank-you to **Chagui68** for creating Military Arsenal and making the project available to the Slimefun community. If you enjoy this addon, please visit the original project and give the original developer the recognition they deserve.

### Upstream collaboration

Chagui68's turret progression work remains part of this fork, including the four-stage Attack and Machine Gun turret progression introduced through upstream collaboration. The Legacy fork intentionally removes the separate military mob/boss entity subsystem while retaining the weapons, armor, machines, turrets, crafting, vouchers, Void progression, and war-machine gameplay.

## 🆕 Legacy 1.1.2 — Security & Entity-System Cleanup

Version **1.1.2** focuses on safer public-server operation:

- Removes the Military Arsenal mob/boss entity system, boss spawn eggs, Spawn Negator, cinematic mob logic, and related combat listeners
- Removes `/weapons summon` and `/weapons give`; `/weapons` is limited to admin maintenance actions
- Preserves explicit CI checks against privilege-escalation/backdoor residue such as `setOp`, console command dispatch, ban pardon hooks, permission attachments, and the old encoded marker names
- Adds one-player-at-a-time locks for copied machine inventories to close two-player duplication routes
- Prevents active copied-inventory machines from being broken or destroyed by explosions
- Tightens vanilla ingredient matching so custom items cannot satisfy recipes solely by sharing a base Material
- Hardens Bombardment Terminal targeting with range, cooldown, world-border, loaded-chunk, and terminal-world validation
- Attributes bombardment TNT to the firing player for protection/logging compatibility
- Prevents delayed airstrike effects from force-loading remote chunks
- Fixes the Networks availability check
- Adds Minecraft Java **1.21.11** version detection support

## 🧰 Legacy compatibility

- Paper / Purpur **26.2+** target
- Java **25** runtime
- Java **21-compatible plugin bytecode**; CI builds with Java 25
- **Slimefun Legacy** is the only hard addon dependency
- Replaces the upstream Drake-only compile dependency with the public Slimefun4-compatible API surface used by Slimefun Legacy
- Direct, versioned GitHub Actions JAR output: `SF_MilitaryArsenal_Legacy_v1.1.2.jar`

### Optional Networks compatibility

**Networks is not required.** Military Arsenal loads before Networks so its Slimefun items and recipes are available when Networks builds its indexes. The compatibility layer only runs when Networks is actually present.

**Infinity Expansion / IE1 is not a dependency of Military Arsenal. IE2 is therefore not required.** The Void and antimatter progression in this addon belongs to Military Arsenal itself and does not use Infinity Expansion item IDs.

## 📦 Requirements

| Requirement | Status |
|---|---|
| Slimefun Legacy | **Required** |
| Paper or Purpur 26.2+ | **Required / supported target** |
| Java 25 | **Required by Paper 26.2** |
| Networks | Optional |
| Infinity Expansion / IE1 | Not required |
| Infinity Expansion 2 / IE2 | Not required |
| Folia | Not declared supported |

## 🚀 Features

### 🔫 Weapons & ammunition
- Machine Gun with dedicated ammunition and burst-fire combat
- Antimatter Rifle and late-game combat progression
- Military components and tiered crafting materials
- Weapon Upgrade Table and upgrade modules

### 🛡️ Defensive systems
- Attack, Sniper, Melee, and Machine Gun turrets
- Wraith-class mountable turret / war-machine system
- Four-stage Attack and Machine Gun turret progression
- Multi-level NBT turret structures with protected upgrade/dismantle handling

### 💣 War machines & crafting
- Bombardment Terminal with coordinate-targeted strikes and public-server safety limits
- Military Crafting Table
- Military Machine Fabricator
- Ammunition Workshop
- Antimatter Pedestal and Ritual progression

### ✨ Progression
- Military vouchers
- Void armor
- Antimatter materials and weapons
- Upgrade modules and advanced crafting components

## 🔒 Public-server security notes

The Legacy build intentionally does **not** include Military Arsenal's custom mob/boss entity subsystem. This means there are no automatic Purple Guy/elite mob conversions, boss arena AI, boss spawn eggs, or Spawn Negator machine in this branch.

Persistent crafting GUIs are session-locked so two players cannot open independent copies of the same stored machine inventory. Active sessions also block machine destruction until the GUI closes.

Bombardment defaults are configurable in `plugins/WeaponsAddon/config.yml`:

```yaml
bombardment:
  max_horizontal_range: 256
  cooldown_seconds: 30
```

Targets must be in the terminal's world, inside the world border, within range, and in an already-loaded chunk. Delayed missile execution rechecks those conditions before spawning effects or TNT.

## 📥 Installation

1. Install **Slimefun Legacy** on a Paper/Purpur 26.2+ server running Java 25.
2. Download the latest `SF_MilitaryArsenal_Legacy_v1.x.x.jar` from this repository's Actions or Releases.
3. Place the JAR in the server's `plugins/` directory.
4. Optionally install a compatible **Networks** build.
5. Restart the server fully.

No separate Dough, Drake Slimefun6, IE1, or IE2 plugin is required by this Legacy build.

## 🛠️ Building

Build with JDK 25:

```bash
mvn -B -Dmaven.test.skip=true clean package
```

The finished JAR is written to `target/SF_MilitaryArsenal_Legacy_v1.1.2.jar`. GitHub Actions publishes the versioned JAR directly and tagged builds attach the raw JAR to the GitHub Release.

## 📜 License & attribution

Military Arsenal remains distributed under the **GNU General Public License v3.0** included in this repository. This fork does not remove or replace the original project's licensing or attribution.

All original Military Arsenal authorship and project credit remains with **Chagui68**. Changes in this repository are focused on Slimefun Legacy compatibility, platform maintenance, security fixes, bug fixes, and continued usability on modern server software.

Minecraft is a trademark of Microsoft/Mojang. This project is an independent community addon and is **not affiliated with, endorsed by, or sponsored by Microsoft or Mojang**. Slimefun and other referenced projects belong to their respective authors and maintainers.

The Slimefun Legacy compatibility work in this fork is maintained for **AlbionMC.com**.

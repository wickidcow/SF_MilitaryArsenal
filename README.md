# ⚔️ Military Arsenal Legacy

[![Paper](https://img.shields.io/badge/Paper%20%2F%20Purpur-26.2%2B-blue)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25-orange)](https://adoptium.net/)
[![Slimefun Legacy](https://img.shields.io/badge/Slimefun-Legacy-brightgreen)](https://github.com/wickidcow/Slimefun-Legacy)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

Military Arsenal is a combat-focused Slimefun addon with military weapons, ammunition, advanced crafting, defensive turrets, war machines, upgrade systems, Void/antimatter progression, and coordinate-based bombardment.

This repository is a **Slimefun Legacy compatibility fork** maintained for the AlbionMC.com server environment. It preserves the gameplay work of the original project while keeping the addon usable on current Paper/Purpur 26.2+ and the Slimefun Legacy API surface.

> **Upstream project:** [Chagui68/SF_MilitaryArsenal](https://github.com/Chagui68/SF_MilitaryArsenal)  
> **Original developer:** [Chagui68](https://github.com/Chagui68)

## ❤️ Homage to Chagui68

**Military Arsenal exists because of the work of Chagui68.** The original concept, gameplay direction, military progression, weapons, ammunition, machines, defensive turret systems, crafting systems, models, structures, and the foundation of this addon were created by **Chagui68**.

This Slimefun Legacy repository is a compatibility and maintenance fork. Its purpose is to preserve that work and keep it playable on newer Paper/Purpur and Slimefun Legacy environments; it is not intended to replace, take ownership of, or diminish the original project or its creator.

If you enjoy the addon, please visit the upstream repository and give the original project and developer the recognition they deserve.

## 🆕 Legacy 1.1.3 — Upstream Sync & Security Hardening

Version **1.1.3** synchronizes the Legacy fork with Chagui68's current `main` branch and adds a focused public-server security pass:

- Current upstream `main` is merged with real Git ancestry preserved.
- Upstream removal of the custom Military mob/boss entity system is incorporated.
- Minecraft Java **1.21.11** version detection is incorporated.
- `/weapons` remains restricted to maintenance/admin actions; old `give` and `summon` paths are removed.
- The previously identified hidden unban/backdoor path is absent and blocked by CI regression scans.
- Persistent virtual machine inventories are single-session per block.
- Active copied inventories cannot be broken or destroyed by explosions.
- Shift-click and drag-transfer bypasses are blocked in sensitive custom inventories.
- 4×4 and 6×6 crafting result slots are take-only and cannot be overwritten by a second craft.
- Unclaimed crafting results are returned safely when the GUI closes.
- Vanilla recipe matching requires item similarity instead of accepting custom items merely because they share a Material.
- Bombardment targeting is range-limited, world-bound, world-border checked, and restricted to already-loaded chunks.
- Bombardment TNT is attributed to the firing player for protection/logging compatibility.
- CI checks the source and compiled JAR for known privilege-escalation/backdoor signatures and anti-dupe safeguards.

## 🔄 Upstream synchronization policy

The fork intentionally keeps **Chagui68's upstream commits in Git history**. Legacy-specific changes are layered on top instead of rewriting or squashing upstream history.

For future updates:

1. Fetch `Chagui68/SF_MilitaryArsenal` as upstream.
2. Merge upstream `main` into a short-lived integration branch.
3. Resolve only genuine Legacy-specific conflicts.
4. Run Java 25 build, security, anti-dupe, and JAR audits.
5. Merge the integration branch into this repository's `main` with normal merge ancestry preserved.

Avoid squashing the upstream synchronization merge itself. Preserving its parentage is what lets Git recognize already-integrated upstream work the next time the developer updates the project.

## 🧰 Legacy compatibility

- Paper / Purpur **26.2+** target
- Minecraft Java **1.21.11+** version detection
- Java **25** runtime target
- Java **21-compatible plugin bytecode**; CI builds with Java 25
- **Slimefun Legacy** is the only hard addon dependency
- Networks remains optional
- Direct, versioned GitHub Actions JAR output: `SF_MilitaryArsenal_Legacy_v1.1.3.jar`

### Optional Networks compatibility

**Networks is not required.** Military Arsenal is configured to load before Networks so its Slimefun items and recipes are registered before Networks builds its indexes. The optional integration code only runs when Networks is present.

**Infinity Expansion / IE1 and IE2 are not dependencies of Military Arsenal.** The Void and antimatter progression in this addon belongs to Military Arsenal itself.

## 📦 Requirements

| Requirement | Status |
|---|---|
| Slimefun Legacy | **Required** |
| Paper or Purpur 26.2+ | **Required / supported target** |
| Java 25 | **Required by the Paper 26.2 target** |
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

### 💣 Machines & progression
- Bombardment Terminal with coordinate-targeted strikes
- Military Crafting Table
- Military Machine Fabricator
- Ammunition Workshop
- Antimatter Pedestal and Ritual progression
- Military vouchers
- Void armor and antimatter materials

## Removed upstream entity subsystem

The current upstream synchronization removes the old custom Military mob/boss implementation, including the boss AI/listeners, automatic Military mob conversions, Boss Spawn Egg, Spawn Negator, cinematic join behavior, and the old entity-spawning admin commands.

The weapons, armor, machines, turrets, crafting, vouchers, Void progression, and war-machine systems remain.

## 🔐 Security / anti-dupe policy

The build fails if known privilege-escalation or backdoor APIs/signatures reappear, including server command dispatch, console command access, OP modification, ban pardoning, dynamic permission attachment, and the previously identified hidden signature fields.

Persistent copied machine inventories are locked to one player per block. A second player cannot open another snapshot of the same stored contents, and an active copied-inventory machine is protected from breaking and explosions.

Sensitive custom GUIs also block shift-click and drag-transfer bypasses. Crafting output slots are take-only, and persisted machine slot data is cleared after legitimate break-time drops.

## 💥 Bombardment safety defaults

```yaml
bombardment:
  max_horizontal_range: 256
  cooldown_seconds: 30
```

Targets must be in the terminal's world, inside the world border, within range, and in an already-loaded chunk. Delayed missile execution rechecks those conditions before spawning effects or TNT.

## 📥 Installation

1. Install **Slimefun Legacy** on a Paper/Purpur 26.2+ server running Java 25.
2. Download `SF_MilitaryArsenal_Legacy_v1.1.3.jar` from this repository's Releases.
3. Place the JAR in the server's `plugins/` directory.
4. Optionally install a compatible Networks build.
5. Restart the server fully.

No separate Dough, Drake Slimefun6, IE1, or IE2 plugin is required by this Legacy build.

## 🛠️ Building

Build with JDK 25:

```bash
mvn -B -Dmaven.test.skip=true clean package
```

The finished JAR is written to `target/SF_MilitaryArsenal_Legacy_v1.1.3.jar`. GitHub Actions publishes the raw JAR directly, and tagged builds attach the same JAR to the GitHub Release.

## 📜 License & attribution

Military Arsenal remains distributed under the **GNU General Public License v3.0** included in this repository. This fork does not remove or replace the original project's licensing or attribution.

All original Military Arsenal authorship and project credit remains with **Chagui68**. Changes in this repository are focused on Slimefun Legacy compatibility, platform maintenance, security, bug fixes, and continued usability on modern server software.

Minecraft is a trademark of Microsoft/Mojang. This project is an independent community addon and is **not affiliated with, endorsed by, or sponsored by Microsoft or Mojang**. Slimefun and other referenced projects belong to their respective authors and maintainers.

The Slimefun Legacy compatibility work in this fork is maintained for **AlbionMC.com**.

# ⚔️ Military Arsenal Legacy

[![Paper](https://img.shields.io/badge/Paper%20%2F%20Purpur-26.2%2B-blue)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25-orange)](https://adoptium.net/)
[![Slimefun Legacy](https://img.shields.io/badge/Slimefun-Legacy-brightgreen)](https://github.com/wickidcow/Slimefun-Legacy)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

Military Arsenal is a combat-focused Slimefun addon with military weapons, ammunition, workbenches, war machines, defensive turrets, bosses, elite combat mobs, upgrade systems, Void/antimatter progression, and coordinate-based bombardment.

This repository is a **Slimefun Legacy compatibility fork** maintained for the AlbionMC.com server environment. It preserves the gameplay work of the original project while keeping the addon usable on the current Paper/Purpur 26.2 platform and the Slimefun Legacy API surface.

> **Original project:** [Chagui68/Military-Arsenal-addon-for-Slimefun4](https://github.com/Chagui68/Military-Arsenal-addon-for-Slimefun4)  
> **Original developer:** [Chagui68](https://github.com/Chagui68)

## ❤️ Homage to Chagui68

**Military Arsenal exists because of the work of Chagui68.** The original concept, gameplay direction, military progression, weapons, ammunition, machines, defensive turret systems, bosses, crafting systems, models, structures, and the foundation of this addon were created by **Chagui68**.

This Slimefun Legacy repository is a compatibility and maintenance fork. Its purpose is to preserve that original work and keep it playable on newer Paper/Purpur and Slimefun Legacy environments; it is **not** intended to replace, take ownership of, or diminish the original project or its creator.

A sincere thank-you to **Chagui68** for creating Military Arsenal and making the project available to the Slimefun community. If you enjoy this addon, please visit the [original Military Arsenal repository](https://github.com/Chagui68/Military-Arsenal-addon-for-Slimefun4) and give the original project and developer the recognition they deserve.

### Upstream collaboration

Chagui68's current turret progression and elite-combat adaptations are intentionally retained in this fork. His **PR #3** was merged into the Legacy branch history, including the four-stage turret progression interface and the new elite combat systems. Legacy-specific reliability fixes are layered on top rather than replacing his gameplay direction.

## 🆕 Legacy 1.1.0 — Turret Reliability Overhaul

Version **1.1.0** combines Chagui68's latest upstream adaptations with a focused Slimefun Legacy reliability pass:

- Four-stage Attack and Machine Gun turret progression retained from Chagui68
- Level scaling for range, damage, energy capacity, energy cost, and cooldown behavior
- Progression GUI with current/next-level stats, XP/item requirements, and growth-space checks
- NBT turret upgrades now validate the exact next-level footprint instead of rejecting the turret's own blocks
- Turret dismantling removes only blocks that still match the turret's NBT structure, protecting nearby/player-replaced blocks
- Missing NBT structure pieces can repair safely without overwriting foreign blocks
- Interaction hitboxes recover automatically after entity loss/chunk reload instead of depending on entities inside the NBT parser
- Attack and Machine Gun turret damage now reads progression from the actual base block instead of the elevated muzzle location
- Line-of-sight targeting now originates from the real turret muzzle/sensor height
- Wraith mountable turret now actually consumes **150 J per shot** and is capped at **4 shots/second**
- Sniper turret displayed stats now match its real **55-block range / 100 HP damage** behavior
- Sniper projectile visuals cover the full firing range and use the elevated sensor for line-of-sight
- Melee turret displayed damage now matches its real **50 HP** attack
- Melee attacks no longer consume power when the Guardian model cannot begin an attack, and overlapping attack animations are prevented
- Chagui68's elite combat mob additions from PR #3 remain included
- **Automatic Military Arsenal mob spawning is now disabled by default and must be explicitly enabled by the server owner**

## 🧰 Legacy compatibility

- Paper / Purpur **26.2+** target
- Java **25** runtime, matching Paper 26.2 requirements
- Java **21-compatible plugin bytecode** for a conservative addon ABI; CI builds with Java 25
- **Slimefun Legacy** is the only hard addon dependency
- Includes Chagui68's latest merged turret progression and elite-combat adaptations
- Replaces the upstream Drake-only compile dependency with the public Slimefun4-compatible API surface used by Slimefun Legacy
- Keeps upstream source easy to synchronize by preparing a Legacy-compatible generated source tree during Maven builds
- Direct, versioned GitHub Actions JAR output: `SF_MilitaryArsenal_Legacy_v1.1.0.jar`

### Optional Networks compatibility

**Networks is not required.** When Networks is installed, Paper loads Military Arsenal first so its Slimefun items and recipes are already registered when Networks builds its supported recipe indexes. This uses normal Slimefun registry discovery rather than a private or reflection-based Networks API, which keeps the addons independently updatable.

**Infinity Expansion / IE1 is not a dependency of Military Arsenal. IE2 is therefore not required.** The Void and antimatter progression in this addon belongs to Military Arsenal itself and does not use Infinity Expansion item IDs.

## 📦 Requirements

| Requirement | Status |
|---|---|
| Slimefun Legacy | **Required** |
| Paper or Purpur 26.2+ | **Required / supported target** |
| Java 25 | **Required by Paper 26.2** |
| Networks | Optional; automatic load-order compatibility |
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
- Spawn Negator for military-entity control

### 💣 War machines
- Bombardment Terminal with coordinate-targeted strikes
- Military Crafting Table
- Military Machine Fabricator
- Ammunition Workshop
- Antimatter Pedestal and Ritual progression

### ☠️ Encounters & progression
- Military bosses and custom mob behavior
- Chagui68's expanded elite combat mobs and tactical behaviors
- Boss reward systems
- Vouchers
- Void armor and antimatter materials

## 🧟 Custom mob spawning is opt-in

**Automatic Military Arsenal mob conversions are disabled by default.** Installing the addon should not cause vanilla mobs to begin turning into Military Arsenal mobs unless the server owner intentionally enables them.

The following automatic conversions ship with `spawn_chance: 0.0`:

- Elite Ranger
- Elite Killer
- The King
- Pusher
- Battle Witch
- Juan
- Rusty Crab
- Purple Guy

To enable a specific mob, edit `plugins/WeaponsAddon/config.yml` and raise only that mob's `spawn_chance` above `0.0`.

For example, to give Purple Guy a 5% conversion chance:

```yaml
mobs:
  purple_guy:
    spawn_chance: 0.05
```

`0.0` means disabled, `0.05` means 5%, `0.10` means 10%, and `1.0` means 100%.

> **Upgrading an existing server:** Bukkit normally preserves an existing `plugins/WeaponsAddon/config.yml`. If an older installation already has non-zero spawn chances, change those values to `0.0` manually. The new safe defaults apply automatically to fresh/generated configs, not by overwriting an administrator's existing configuration.

Manual boss systems, boss spawn eggs, weapons, armor, machines, turrets, crafting, and other non-random-spawn features remain available independently of these automatic mob conversion chances.

## 📥 Installation

1. Install **Slimefun Legacy** on a Paper/Purpur 26.2+ server running Java 25.
2. Download the latest `SF_MilitaryArsenal_Legacy_v1.x.x.jar` from this repository's Actions or Releases.
3. Place the JAR in the server's `plugins/` directory.
4. Optionally install a compatible **Networks** build; no additional bridge plugin or configuration is required.
5. Restart the server fully.

No separate Dough, Drake Slimefun6, IE1, or IE2 plugin is required by this Legacy build.

## 🛠️ Building

Build with JDK 25:

```bash
mvn -B -Dmaven.test.skip=true clean package
```

The finished JAR is written to `target/SF_MilitaryArsenal_Legacy_v1.1.0.jar`. GitHub Actions publishes the same JAR directly and tagged builds attach the raw JAR to the GitHub Release.

## 📜 License & attribution

Military Arsenal remains distributed under the **GNU General Public License v3.0** included in this repository. This fork does not remove or replace the original project's licensing or attribution.

All original Military Arsenal authorship and project credit remains with **Chagui68**. Changes in this repository are focused on Slimefun Legacy compatibility, platform maintenance, bug fixes, and continued usability on modern server software.

Minecraft is a trademark of Microsoft/Mojang. This project is an independent community addon and is **not affiliated with, endorsed by, or sponsored by Microsoft or Mojang**. Slimefun and other referenced projects belong to their respective authors and maintainers.

The Slimefun Legacy compatibility work in this fork is maintained for **AlbionMC.com**.

# ⚔️ Military Arsenal Legacy

[![Paper](https://img.shields.io/badge/Paper%20%2F%20Purpur-26.2%2B-blue)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21%2B-orange)](https://adoptium.net/)
[![Slimefun Legacy](https://img.shields.io/badge/Slimefun-Legacy-brightgreen)](https://github.com/wickidcow/Slimefun-Legacy)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

Military Arsenal is a combat-focused Slimefun addon with military weapons, ammunition, workbenches, war machines, defensive turrets, bosses, upgrade systems, Void/antimatter progression, and coordinate-based bombardment.

This repository is a **Slimefun Legacy compatibility fork** maintained for the AlbionMC.com server environment. It preserves the gameplay work of the original project while keeping the addon usable on the current Paper/Purpur 26.2 platform and the Slimefun Legacy API surface.

> **Original project:** [Chagui68/Military-Arsenal-addon-for-Slimefun4](https://github.com/Chagui68/Military-Arsenal-addon-for-Slimefun4)  
> **Original developer:** Chagui68  
> Please give the original project and developer credit for creating Military Arsenal.

## 🧰 Legacy compatibility

- Paper / Purpur **26.2+** target
- Java **21+** bytecode; CI builds with Java 25
- **Slimefun Legacy** is the required runtime dependency
- Preserves the upstream 2.4.8 turret and structure overhaul
- Replaces the upstream Drake-only compile dependency with the public Slimefun4-compatible API surface used by Slimefun Legacy
- Keeps upstream source easy to synchronize by preparing a Legacy-compatible generated source tree during Maven builds
- Direct, versioned GitHub Actions JAR output: `SF_MilitaryArsenal_Legacy_v1.0.0.jar`

### Optional addon integration

**Networks** is optional. If a compatible Networks build is present, Military Arsenal attempts to register compatible recipes through a reflection-based bridge. If the Networks API is unavailable or changes, Military Arsenal continues loading without that bridge.

**Infinity Expansion / IE1 is not a dependency of Military Arsenal. IE2 is therefore not required.** The Void and antimatter progression in this addon belongs to Military Arsenal itself and does not use Infinity Expansion item IDs.

## 📦 Requirements

| Requirement | Status |
|---|---|
| Slimefun Legacy | **Required** |
| Paper or Purpur 26.2+ | **Required / supported target** |
| Java 21+ | **Required** |
| Networks | Optional integration |
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
- Mountable turret / war-machine systems
- Multi-level turret structures and upgrades
- Spawn Negator for military-entity control

### 💣 War machines
- Bombardment Terminal with coordinate-targeted strikes
- Military Crafting Table
- Military Machine Fabricator
- Ammunition Workshop
- Antimatter Pedestal and Ritual progression

### ☠️ Encounters & progression
- Military bosses and custom mob behavior
- Boss reward systems
- Vouchers
- Void armor and antimatter materials

## 📥 Installation

1. Install **Slimefun Legacy** on a Paper/Purpur 26.2+ server.
2. Download the latest `SF_MilitaryArsenal_Legacy_v1.x.x.jar` from this repository's Actions or Releases.
3. Place the JAR in the server's `plugins/` directory.
4. Optionally install a compatible **Networks** build for the recipe integration bridge.
5. Restart the server fully.

No separate Dough, Drake Slimefun6, IE1, or IE2 plugin is required by this Legacy build.

## 🛠️ Building

```bash
mvn -B -Dmaven.test.skip=true clean package
```

The finished JAR is written to `target/SF_MilitaryArsenal_Legacy_v1.0.0.jar`. GitHub Actions also publishes the same JAR directly rather than wrapping it in an artifact ZIP.

## 📜 License & attribution

Military Arsenal remains distributed under the **GNU General Public License v3.0** included in this repository. This fork does not remove or replace the original project's licensing or attribution.

Minecraft is a trademark of Microsoft/Mojang. This project is an independent community addon and is **not affiliated with, endorsed by, or sponsored by Microsoft or Mojang**. Slimefun and other referenced projects belong to their respective authors and maintainers.

The Slimefun Legacy compatibility work in this fork is maintained for **AlbionMC.com**.

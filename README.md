# ⚔️ Military Arsenal Legacy

[![Paper](https://img.shields.io/badge/Paper%20%2F%20Purpur-26.2%2B-blue)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25-orange)](https://adoptium.net/)
[![Slimefun Legacy](https://img.shields.io/badge/Slimefun-Legacy-brightgreen)](https://github.com/wickidcow/Slimefun-Legacy)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

Military Arsenal is a combat-focused Slimefun addon with military weapons, ammunition, upgrade systems, defensive turrets, war machines, advanced crafting, Void/antimatter progression, and coordinate-based bombardment.

This repository is a **Slimefun Legacy compatibility fork** maintained for the AlbionMC.com server environment. It preserves the gameplay work of the original project while keeping the addon usable on current Paper/Purpur 26.2+ and the Slimefun Legacy API surface.

> **Upstream project:** [Chagui68/SF_MilitaryArsenal](https://github.com/Chagui68/SF_MilitaryArsenal)  
> **Original developer:** [Chagui68](https://github.com/Chagui68)

## ❤️ Homage to Chagui68

**Military Arsenal exists because of the work of Chagui68.** The original concept, gameplay direction, military progression, weapons, ammunition, machines, defensive turret systems, crafting systems, models, structures, and the foundation of this addon were created by **Chagui68**.

This Slimefun Legacy repository is a compatibility and maintenance fork. Its purpose is to preserve that work and keep it playable on newer Paper/Purpur and Slimefun Legacy environments; it is not intended to replace, take ownership of, or diminish the original project or its creator.

If you enjoy the addon, please visit the upstream repository and give the original project and developer the recognition they deserve.

## 🆕 Legacy 1.1.3 — Upstream Sync & Security Hardening

Version **1.1.3** synchronizes the Legacy fork with Chagui68's current `main` branch and adds a focused anti-abuse pass:

- Merged the current upstream `main` using a real Git merge parent instead of copying/cherry-picking the changes, preserving ancestry for easier future upstream merges.
- Integrated Chagui68's removal of the custom Military mob/boss entity system and the old Spawn Negator path.
- Integrated the upstream Minecraft Java **1.21.11** version-detection update.
- Retained the reduced `/weapons` administration surface: configuration reload and turret cleanup only.
- Verified that the previously identified hidden unban/backdoor path is absent.
- Retained CI checks that reject command-dispatch, OP elevation, ban/pardon, permission-attachment, and known backdoor-signature residue in both source and compiled classes.
- Added a shared per-block session lock for the Military Crafting Table, Machine Fabricator, and Ammunition Workshop.
- Prevented two players from opening independent copies of the same stored machine inventory.
- Prevented workshop/fabricator breaking while their virtual inventory is open, closing the stored-item drop + GUI-copy duplication route.
- Runs persistent-machine dupe-safeguard checks in GitHub Actions so those protections cannot be accidentally removed without failing the build.

## 🔄 Upstream synchronization policy

The fork intentionally keeps **Chagui68's upstream commits in Git history**. Legacy-specific fixes are layered on top of upstream rather than rewriting upstream commits.

For future updates, the preferred flow is:

1. Fetch `Chagui68/SF_MilitaryArsenal` as upstream.
2. Merge upstream `main` into a short-lived integration branch.
3. Resolve only genuine Legacy-specific conflicts.
4. Run the security, dupe-safety, and Java 25 build checks.
5. Merge the integration branch into this repository's `main` with a normal merge commit.

Avoid squashing the upstream synchronization merge itself; preserving its parentage is what allows Git to recognize already-integrated upstream work on the next update.

## 🧰 Legacy compatibility

- Paper / Purpur **26.2+** target
- Minecraft Java **1.21.11+** version detection
- Java **25** runtime, matching the Paper 26.2 target
- Java **21-compatible plugin bytecode**; CI builds with Java 25
- **Slimefun Legacy** is the only hard addon dependency
- Networks remains optional and is handled through normal Slimefun registry/load ordering
- Direct, versioned GitHub Actions JAR output: `SF_MilitaryArsenal_Legacy_v1.1.3.jar`

### Optional Networks compatibility

**Networks is not required.** When Networks is installed, Paper loads Military Arsenal first so its Slimefun items and recipes are registered before Networks builds its supported recipe indexes. This avoids a private/reflection-based bridge and keeps the addons independently updatable.

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
- Voucher and Void/antimatter item progression retained where still referenced by the addon

### Removed upstream systems

As of the current upstream synchronization, the old custom Military mob/boss entity implementation is no longer part of the active addon. This includes the old boss AI/listeners, random Military mob conversions, boss spawn egg, cinematic join behavior, and Spawn Negator integration.

## 🔐 Security / anti-dupe policy

The build fails if known privilege-escalation or backdoor APIs/signatures reappear, including server command dispatch, console command access, OP modification, ban pardoning, dynamic permission attachment, and the previously identified hidden signature fields.

Persistent virtual machine inventories are single-session per block. A second player cannot open a second snapshot of the same stored contents, and machines with persisted contents cannot be broken while their virtual inventory is active.

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

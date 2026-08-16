# Military Arsenal Legacy v1.1.3

This release synchronizes the Slimefun Legacy fork with Chagui68's current upstream `main` branch and adds a focused security / item-duplication hardening pass for Paper/Purpur 26.2+.

## Upstream changes merged

- Merged the current `Chagui68/SF_MilitaryArsenal` `main` branch with preserved Git ancestry so later upstream updates can be merged normally instead of re-applied as unrelated patches.
- Integrated upstream removal of the custom Military mob/boss entity system.
- Removed the old boss AI/listeners, automatic Military mob conversions, Boss Spawn Egg, cinematic join behavior, and Spawn Negator path as part of that upstream change.
- Integrated Minecraft Java 1.21.11 version-detection support.
- Kept the reduced `/weapons` administrative surface: reload and turret cleanup only.

## Security review

- Confirmed the previously identified unsafe hidden unban path is absent. The old code could silently pardon name/IP bans for tracked event participants.
- Confirmed no `setOp`, console command dispatch, dynamic permission attachment, ban pardon, or known hidden-signature residue is present in the reviewed source.
- Retained source and compiled-JAR CI scans that fail the build if those privilege-escalation/backdoor patterns return.

## Item-duplication fixes

The audit found a reproducible design flaw in the persistent virtual machine inventories: stored `BlockStorage` contents were copied into a separate per-player GUI without locking the underlying machine. Two players could therefore open the same machine and receive independent snapshots of the same stored contents. Breaking some machines while a GUI was open could also drop the stored copy while the GUI copy still existed.

v1.1.3 adds:

- A shared per-block `MachineSessionGuard`.
- Single-user sessions for the Military Crafting Table.
- Single-user sessions for the Military Machine Fabricator.
- Single-user sessions for the Ammunition Workshop.
- Block-break protection while those persistent virtual inventories are open.
- `HIGHEST` / `ignoreCancelled` break handling for persisted-item drop logic where needed, preventing protection-plugin cancellation from creating a drop-without-break duplication condition.
- CI regression checks that require these guards to remain in place.

## Compatibility / build

- Paper / Purpur 26.2+
- Minecraft Java 1.21.11+ version detection
- Java 25 runtime target
- Java 21-compatible plugin bytecode
- Slimefun Legacy required
- Networks optional

## Download

Use the raw release asset:

`SF_MilitaryArsenal_Legacy_v1.1.3.jar`

Military Arsenal remains GPL-3.0 licensed. Original project credit remains with Chagui68; this fork contains Slimefun Legacy compatibility, maintenance, security, and modern Paper/Purpur support work.

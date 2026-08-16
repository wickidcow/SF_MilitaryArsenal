# Military Arsenal Legacy v1.1.3

This release synchronizes the Slimefun Legacy fork with Chagui68's current upstream `main` branch and adds a focused security, anti-duplication, and public-server hardening pass for Paper/Purpur 26.2+.

## Upstream synchronization

- Merged the current `Chagui68/SF_MilitaryArsenal` `main` branch with preserved Git ancestry instead of copying or squashing upstream changes.
- This makes future upstream updates substantially easier for Git to recognize and merge, while keeping Legacy-specific changes layered on top.
- Integrated upstream removal of the custom Military mob/boss entity system.
- Integrated Minecraft Java 1.21.11 version-detection support.
- Kept `/weapons` limited to maintenance actions instead of player-item or entity spawning commands.

## Removed entity subsystem

The current upstream cleanup removes the old Military mob/boss implementation, including:

- MilitaryMobHandler
- EliteMobHandler
- BossRewardHandler
- MilitaryCombatHandler
- BossAIHandler
- EliteMobCombatListener
- BossSpawnEgg
- SpawnNegator
- CinematicJoinListener / CinematicUtils
- `/weapons summon` and `/weapons give`

Weapons, ammunition, crafting, turrets, war machines, upgrades, vouchers, Void armor, antimatter progression, and bombardment remain.

## Security review

- Confirmed the previously identified hidden unban/backdoor path is absent. The removed code could silently pardon name/IP bans for tracked participants.
- Confirmed no `setOp`, console-command dispatch, dynamic permission attachment, ban-pardon path, or known hidden backdoor signature remains in reviewed source.
- CI scans both source and the compiled JAR for those privilege-escalation/backdoor patterns.
- CI now also fails if the removed admin `give` / `summon` command paths or removed mob subsystem return unexpectedly.

## Item-duplication and GUI hardening

The audit found that persistent virtual machine inventories were copied out of BlockStorage into player-specific GUIs without sufficient transaction locking. That could let two players open independent snapshots of the same stored contents, or let a machine be destroyed while a copied GUI inventory was still active.

v1.1.3 adds:

- A shared per-block `MachineSessionManager`.
- One active user at a time for the Military Crafting Table, Military Machine Fabricator, and Ammunition Workshop.
- Break and explosion protection while those copied inventories are active.
- Highest-priority machine guards so later listeners cannot accidentally re-enable a protected break/explosion.
- Stored-slot cleanup after legitimate block-break item drops.
- Shift-click blocking and drag-transfer blocking for sensitive custom inventories.
- A centralized second-line transfer guard for the Weapon Upgrade Table, Ammunition Workshop, Military Crafting Table, Machine Fabricator, and Bombardment Terminal.
- Take-only crafting result handling for the 4x4 and 6x6 crafting machines.
- Crafted results are returned safely on GUI close instead of being silently lost.
- Crafting refuses to consume another recipe while an unclaimed result is still present.
- Ammunition Workshop output is take-only and remains safely persistent until claimed.
- Vanilla recipe matching now uses full item similarity instead of accepting any custom/enchanted item solely because it shares a Material.

## Bombardment Terminal hardening

- Default maximum horizontal target range: 256 blocks.
- Default per-player cooldown: 30 seconds.
- Target is bound to the terminal's world.
- World-border validation.
- Target chunk must already be loaded; bombardment cannot be used to force-load remote chunks.
- Delayed missile execution rechecks chunk and border state before spawning effects/TNT.
- Bombardment TNT is attributed to the firing player for protection/logging compatibility.
- Arbitrary items placed in terminal input slots are returned instead of being deleted on close.
- Shift-click and drag transfer bypasses are blocked.

Default configuration:

```yaml
bombardment:
  max_horizontal_range: 256
  cooldown_seconds: 30
```

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

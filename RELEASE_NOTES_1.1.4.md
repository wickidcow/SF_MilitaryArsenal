# Military Arsenal Legacy v1.1.4

Military Arsenal Legacy 1.1.4 adds optional ItemsAdder / `iaweapons` visual integration while keeping Military Arsenal fully authoritative for item identity and gameplay.

## ItemsAdder integration

- Adds `ItemsAdder` as a soft dependency. Military Arsenal still works normally without ItemsAdder installed.
- Waits for ItemsAdder custom item data to finish loading before Military Arsenal registers its Slimefun items.
- Uses the ItemsAdder load-data event as the primary readiness signal with registry probing as a fallback.
- Includes a 30-second safe fallback so a broken or incompatible ItemsAdder install cannot permanently prevent Military Arsenal from enabling.
- Copies only the visual model metadata needed for the resource-pack appearance.
- Preserves Military Arsenal Slimefun IDs, persistent data, damage bonuses, upgrade data, recipes, cooldowns, ammunition logic, handlers, and progression.

## Default iaweapons mappings

```yaml
itemsadder:
  visuals:
    enabled: true
    mappings:
      MA_MACHINE_GUN: "iaweapons:ak47"
      MA_ANTIMATTER_RIFLE: "iaweapons:ak47"
      MA_MACHINE_GUN_AMMO: "iaweapons:projectile"
```

These mappings are configurable and can be blanked or replaced with other ItemsAdder namespaced IDs.

## Compatibility

- Slimefun Legacy required
- ItemsAdder optional
- `iaweapons` optional visual pack
- Networks optional
- Towny optional
- Paper / Purpur 26.2+
- Java 25 runtime target
- Java 21-compatible plugin bytecode

## Artifact

`SF_MilitaryArsenal1.1.4.jar`

Military Arsenal remains GPL-3.0 licensed. Original project credit remains with Chagui68; this fork contains Slimefun Legacy compatibility, maintenance, security, modern Paper/Purpur support, and optional ItemsAdder visual integration.

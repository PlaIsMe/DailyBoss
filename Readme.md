# DailyBoss

**DailyBoss** is a Minecraft mod that introduces a Colosseum-style boss challenge system.

## Features

- Adds a Colosseum structure and an interactive "Key" item.
- Right-clicking the key will spawn a random boss.
- The key resets every 1 real-life day (24 hours), but only **after defeating** the current boss in-game.
- ⚠️ If you **do not defeat** the boss within 24 hours, the boss will be **discarded**, and the key will reset without reward.
- You must defeat a boss manually before the key can summon it in the future.  
  _For example: to unlock Warden as a summonable boss, you need to summon and kill the Warden first._
- Custom boss entries can be added via datapacks.
- A built-in UI displays a **paginated** list of available bosses.
- Resource packs can be used to customize boss posters shown in the UI.

## How to Play

1. Find the **Colosseum** structure in your world.
2. Right-click the **Key Entity** in the center of the Colosseum to summon a boss.
3. Defeat the boss in-game.
4. Once defeated, the **Key** will reset after **24 hours of real-life time**, allowing you to summon a new boss.
5. If you **don’t defeat** the boss within 24 hours, it will be **discarded**, and the Key will reset with no reward.
6. To unlock a boss for summoning, you must first **defeat it manually** at least once using other in-game methods (e.g., command or natural spawn).

## Customization

### Creating a Datapack

You can create a datapack to define which bosses can be spawned by the key. The file should be located in the following structure:
```
data/
└── pladailyboss/
    └── dailyboss/
        └── yourmodid/
            └── monsterid.json
```
Replace `yourmodid` with your mod or namespace ID, and `monsterid` with the unique ID of your boss entity.

### JSON Format

Each boss entry must define a list of loot table IDs. Spawn tag is optional for the boss.

Example `data/pladailyboss/dailyboss/minecraft/wither.json`:

```json
{
  "loot_table": [
    "minecraft:chests/bastion_treasure",
    "minecraft:chests/bastion_other"
  ],
  "nbt": {
    "Invul": 202
  }
}
```

### Boss Posters (Resource Pack)
To display posters in the UI, add images using the following file path format in your resource pack:
```
assets/pladailyboss/textures/gui/entity_posters/<namespace>/<entity_name>_enabled.png
assets/pladailyboss/textures/gui/entity_posters/<namespace>/<entity_name>_disabled.png
assets/pladailyboss/textures/gui/entity_posters/<namespace>/<entity_name>_corrupted.png
```
- _enabled.png will be shown after the boss is defeated.
- _disabled.png is shown before the boss is unlocked.
- _corrupted.png is used when the boss entry refers to a mod that is not currently installed.
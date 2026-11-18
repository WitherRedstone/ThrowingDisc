# Throwing Disc

English | [简体中文](README.md)

A port of the "Throwing Disc" weapon from Minecraft 27.1's pre-release content to **Minecraft 1.21.1 NeoForge**.

Right-click to throw. The disc pierces mobs, bounces between blocks, and automatically returns to the player after traveling a certain distance or time. It comes in five tiers: copper, iron, gold, diamond, and netherite.

## Features

- Right-click in either the main hand or off hand to throw; the item in that hand is temporarily cleared.
- Pierces mobs in flight and does not disappear on hit.
- Bounces off blocks according to the incoming direction, and can ricochet repeatedly between walls, floors, and ceilings.
- Each bounce extends the remaining flight distance and time.
- Hitting a mob deals the tier's damage and consumes 1 durability.
- Adds 7 disc-exclusive enchantments; see the Enchantments section below.
- Supports general enchantments such as Unbreaking and Fire Aspect at the enchanting table; Mending and Curse of Vanishing can be applied via enchanted books or an anvil.
- Golden discs are picked up and admired by piglins, and can be used instead of gold ingots for bartering.
- Returns automatically when its distance or time runs out, and is caught when it reaches the player.
- Returns to the original hand if that hand is empty; otherwise it enters the inventory; if the inventory is full, it drops near the player.
- Separate sound effects for throwing, bouncing, returning, and catching.
- Netherite discs are fire-resistant; dropped discs and the projectile cannot be destroyed by lava.

## Enchantments

This mod adds 7 disc-exclusive enchantments. All of them are available at the enchanting table and are classified as non-treasure, tradeable enchantments.

### Impaling

- Max level 5.
- Each level adds 1 damage when the disc hits a mob.
- Works in both the main hand and off hand.
- Incompatible with Ramming.

### Ramming

- Max level 1.
- The disc no longer damages mobs; instead, it breaks blocks.
- Break tiers: copper=stone, iron=iron, gold=gold, diamond=diamond, netherite=netherite.
- Each block broken consumes 1 durability.
- Blocks that cannot be broken still cause a normal bounce.
- Incompatible with Impaling.

### Aerodynamics

- Max level 5.
- Each level adds 1 block of base flight distance.
- Flight ticks adjust automatically to the actual distance.

### Expansion

- Max level 2.
- Only works with Ramming; expands the breaking plane around the hit face.
- Level 1 is 3×3; level 2 is 5×5.
- Only breaks blocks the current material tier can break; each block broken consumes 1 durability.
- If the center block cannot be broken, the disc still bounces normally.

### Ricochet

- Max level 5.
- The base maximum number of bounces is 5; +1 per level, up to 10 at max level.
- Once the maximum number of bounces is reached, the disc returns from the hit point.

### Magnetic Return

- Max level 3.
- Each level increases return speed by 20%; +60% at max level.
- With Ramming and at least level 1 Magnetic Return, broken blocks' drops are collected; when the disc is caught, they go into the inventory, or drop near the player if the inventory is full.
- Level only affects return speed, not catch distance.
- Currently collects normal block loot; container contents are not moved.

### Preservation

- Max level 1.
- When durability is about to run out, the disc keeps its last 1 durability and does not disappear, but enters a "Broken" state.
- At 1 durability, the tooltip shows "Broken" in dark red, and the disc cannot be thrown.
- Repairing to 2 or more durability makes it usable again.

### Fire Aspect (vanilla)

- Max level 2; available directly at the enchanting table.
- Ignites each mob hit for 4 seconds per level: 4 seconds at level 1, 8 seconds at level 2.
- When the disc pierces several mobs, every mob hit along the line is ignited separately.
- Leaves a flame trail in flight and a burst of sparks on hit.
- With Ramming, the disc deals no damage, so it does not ignite mobs.

## Flight Mechanics

The relevant constants are in `src/main/java/com/chinaex123/throwing_disc/entity/DiscProjectile.java`.

- `FLIGHT_TICKS_PER_BLOCK`: 1.5 ticks per block, which also determines throw speed.
- `BASE_RANGE`: 16 blocks, the initial flight distance.
- `AERODYNAMICS_RANGE_PER_LEVEL`: 1 block of extra flight distance per Aerodynamics level.
- `BASE_FLIGHT_TICKS`: 24 ticks, derived automatically from `BASE_RANGE`; extended according to the actual distance when Aerodynamics is present.
- `BOUNCE_RANGE_BONUS`: 3 blocks added per bounce.
- `BOUNCE_TICK_BONUS`: 5 ticks added per bounce.
- `BASE_MAX_BOUNCES`: 5, the maximum number of bounces without enchantments; Ricochet adds 1 per level.
- `MAGNETIC_RETURN_SPEED_PER_LEVEL`: 0.2, the return speed multiplier added per Magnetic Return level.
- `MAX_RANGE`: 64 blocks, the cumulative bounce distance cap.
- `MAX_FLIGHT_TICKS`: 96 ticks, the cumulative bounce time cap.

When either distance or time runs out, the disc enters its return state. Changing `BASE_RANGE` automatically adjusts `BASE_FLIGHT_TICKS` and throw speed; there is no need to edit the tick values manually.

## Developer API

See [README_API_EN.md](README_API_EN.md) for integration instructions for other mods.

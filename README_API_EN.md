# Throwing Disc API

English | [简体中文](README_API.md)

This mod provides an API for other NeoForge 1.21.1 mods to add throwing disc tiers.

The API is located in `com.chinaex123.throwing_disc.api`; the main classes are `DiscTier` and `ThrowingDiscAPI`. Discs registered through the API automatically use this mod's item behavior, projectile, renderer, and sound effects. After adding the item to the `throwing_disc:throwing_disc` tag, it also supports all disc enchantments, with no need to register an extra entity or client renderer.

## Quick Start

### 1. Define a Tier

```java
package com.example.mydiscs;

import com.chinaex123.throwing_disc.api.DiscTier;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;

public final class MyDiscTiers {
    public static final DiscTier TIN = new DiscTier(
            "tin",                      // name prefix
            3.0F,                       // damage
            300,                        // durability
            12,                         // enchantment value
            Rarity.COMMON,              // rarity
            false,                      // fire resistant
            () -> MyItems.TIN_INGOT.get(), // crafting and repair material
            Tiers.STONE                 // block tier for Ramming
    );
}
```

If the crafting and repair materials are different:

```java
public static final DiscTier TIN = new DiscTier(
        "tin", 3.0F, 300, 12, Rarity.COMMON, false,
        () -> MyItems.TIN_INGOT.get(),  // crafting material
        () -> MyItems.TIN_NUGGET.get(), // repair material
        Tiers.STONE
);
```

The material supplier may also return an item tag. For example, copper ingots can use NeoForge's `c:ingots/copper` tag:

```java
public static final DiscTier COPPER = new DiscTier(
        "copper", 2.0F, 128, 6, Rarity.COMMON, false,
        () -> Tags.Items.INGOTS_COPPER, // crafting and repair material
        Tiers.STONE
);
```

Items and tags can be used independently for crafting and repair. During recipe data generation, item materials are written as concrete items and tag materials are written as `"tag": "..."`.

### 2. Registration

Call this during your mod's construction phase, before `RegisterEvent`:

```java
import com.chinaex123.throwing_disc.api.ThrowingDiscAPI;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(MyMod.MODID)
public class MyMod {
    public static final String MODID = "my_mod";

    public MyMod(IEventBus modEventBus, ModContainer modContainer) {
        ThrowingDiscAPI.register(MyDiscTiers.TIN);
    }
}
```

Bulk registration:

```java
ThrowingDiscAPI.registerAll(
        MyDiscTiers.TIN,
        MyDiscTiers.BRONZE,
        MyDiscTiers.STEEL
);
```

`register` returns the corresponding `DeferredItem<DiscItem>`:

```java
DeferredItem<DiscItem> tinDisc = ThrowingDiscAPI.register(MyDiscTiers.TIN);
```

The registered item ID is:

```text
throwing_disc:tin_throwing_disc
```

## DiscTier

`DiscTier` only describes disc properties and does not register anything. It implements `StringRepresentable` and provides the following constructors:

```java
DiscTier(String name, float attackDamage, int durability, int enchantmentValue,
         Rarity rarity, boolean fireResistant, Supplier<?> ingredient)

DiscTier(String name, float attackDamage, int durability, int enchantmentValue,
         Rarity rarity, boolean fireResistant,
         Supplier<?> recipeIngredient, Supplier<?> repairIngredient)

DiscTier(String name, float attackDamage, int durability, int enchantmentValue,
         Rarity rarity, boolean fireResistant,
         Supplier<?> ingredient, Tier breakTier)

DiscTier(String name, float attackDamage, int durability, int enchantmentValue,
         Rarity rarity, boolean fireResistant,
         Supplier<?> recipeIngredient, Supplier<?> repairIngredient, Tier breakTier)
```

Parameter descriptions:

- `name`: tier name; only lowercase letters, digits, and underscores, matching `[a-z0-9_]+`.
- `attackDamage`: base damage when hitting a mob; cannot be negative.
- `durability`: maximum durability; must be greater than 0.
- `enchantmentValue`: enchantability; cannot be negative.
- `rarity`: item rarity.
- `fireResistant`: whether the item is fire-resistant.
- `recipeIngredient`: crafting material, used only for data generation; the supplier may return an `Item` or a `TagKey<Item>`.
- `repairIngredient`: anvil repair material; the supplier may likewise return an `Item` or a `TagKey<Item>`.
- `breakTier`: the mining tier used by Ramming, which determines which blocks the disc can break; defaults to `Tiers.IRON` when omitted.

Common `breakTier` mappings:

- Stone: `Tiers.STONE`
- Iron: `Tiers.IRON`
- Gold: `Tiers.GOLD`
- Diamond: `Tiers.DIAMOND`
- Netherite: `Tiers.NETHERITE`

The item ID and translation key are generated automatically:

```text
Item ID:          throwing_disc:<name>_throwing_disc
Translation key:  item.throwing_disc.<name>_throwing_disc
```

## Registration API

```java
ThrowingDiscAPI.register(DiscTier tier);
ThrowingDiscAPI.registerAll(DiscTier... tiers);
ThrowingDiscAPI.tiers();
ThrowingDiscAPI.items();
```

- `register`: registers one tier and returns a `DeferredItem<DiscItem>`. The same `DiscTier` cannot be registered twice.
- `registerAll`: registers multiple tiers in order.
- `tiers`: returns a read-only list of currently registered tiers.
- `items`: returns a list of currently registered items.

This mod's built-in tiers are in `init/TDDiscTiers.java`; get the corresponding item with `TDDiscTiers.COPPER.item().get()`.

Registration must happen before `RegisterEvent`. Calling it from the mod constructor is usually safe; do not call it after the registration events have finished.

## Tag API

Discs use five item tags:

- `throwing_disc:throwing_disc`: all discs. The `supported_items` and `primary_items` of the disc enchantments reference this tag.
- `c:throwing_disc`: a NeoForge common item tag that lets other mods identify discs by a common tag.
- `minecraft:enchantable/durability`: allows durability-related enchantments such as Unbreaking and Mending.
- `minecraft:enchantable/vanishing`: allows Curse of Vanishing.
- `minecraft:enchantable/fire_aspect`: allows Fire Aspect.

Corresponding API methods:

```java
ThrowingDiscAPI.discTag();
ThrowingDiscAPI.commonDiscTag();
ThrowingDiscAPI.durabilityTag();
ThrowingDiscAPI.vanishingTag();
ThrowingDiscAPI.fireAspectTag();
```

### Data Generation

Item tags are datapack content and cannot be modified dynamically at runtime. `register()` only registers the item; it does not automatically add the item to any tags.

In your own `ItemTagsProvider`, you can generate them like this:

```java
public class MyItemTagsProvider extends ItemTagsProvider {
    public MyItemTagsProvider(PackOutput output,
                              CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags,
                              @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, MyMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var discTag = tag(ThrowingDiscAPI.discTag());
        var commonTag = tag(ThrowingDiscAPI.commonDiscTag());
        var durabilityTag = tag(ThrowingDiscAPI.durabilityTag());
        var vanishingTag = tag(ThrowingDiscAPI.vanishingTag());
        var fireAspectTag = tag(ThrowingDiscAPI.fireAspectTag());

        ThrowingDiscAPI.addToAllTags(discTag, durabilityTag, vanishingTag, fireAspectTag, MyDiscTiers.TIN);
        ThrowingDiscAPI.addToTags(commonTag, MyDiscTiers.TIN);
    }
}
```

`addToAllTags` has three overloads:

```java
// Adds only the disc and durability tags; kept for backward compatibility.
addToAllTags(discTag, durabilityTag, tier);

// Adds the disc, durability, and vanishing tags.
addToAllTags(discTag, durabilityTag, vanishingTag, tier);

// Recommended: adds the disc, durability, vanishing, and Fire Aspect tags.
addToAllTags(discTag, durabilityTag, vanishingTag, fireAspectTag, tier);
```

`addToTags` adds a disc to any tag, such as `c:throwing_disc`.

### Hand-written Tag JSON

If you are not using data generation, you can provide the files directly:

`data/throwing_disc/tags/item/throwing_disc.json`

```json
{
  "values": [
    "throwing_disc:tin_throwing_disc"
  ]
}
```

`data/c/tags/item/throwing_disc.json`

```json
{
  "values": [
    "throwing_disc:tin_throwing_disc"
  ]
}
```

`data/minecraft/tags/item/enchantable/durability.json`

```json
{
  "values": [
    "throwing_disc:tin_throwing_disc"
  ]
}
```

`data/minecraft/tags/item/enchantable/vanishing.json`

```json
{
  "values": [
    "throwing_disc:tin_throwing_disc"
  ]
}
```

`data/minecraft/tags/item/enchantable/fire_aspect.json`

```json
{
  "values": [
    "throwing_disc:tin_throwing_disc"
  ]
}
```

Tag paths use the singular `tags/item`.

## Resource Files

Items registered through the API always use the `throwing_disc` namespace, so textures, models, and language files must also be placed under that namespace.

Using `tin` as an example:

```text
assets/throwing_disc/textures/item/tin_throwing_disc.png
assets/throwing_disc/models/item/tin_throwing_disc.json
assets/throwing_disc/lang/en_us.json
assets/throwing_disc/lang/zh_cn.json
```

Model file:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "throwing_disc:item/tin_throwing_disc"
  }
}
```

Language files:

```json
{
  "item.throwing_disc.tin_throwing_disc": "Tin Throwing Disc"
}
```

```json
{
  "item.throwing_disc.tin_throwing_disc": "锡飞盘"
}
```

Recipes are generated or written by the addon. The recommended recipe is the same ring shape used by this mod, with four materials arranged in a ring:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "equipment",
  "pattern": [
    " A ",
    "A A",
    " A "
  ],
  "key": {
    "A": {
      "item": "my_mod:tin_ingot"
    }
  },
  "result": {
    "id": "throwing_disc:tin_throwing_disc",
    "count": 1
  }
}
```

## Data Generation Notes

This mod's own `runData` only handles its five built-in tiers. Tiers registered by other mods do not automatically appear in this mod's data generation output.

Therefore, addons must generate the following in their own mod:

- Item models
- Recipes and advancements
- The five item tags
- Language files

You can register your tiers during your own `runData`, then iterate `ThrowingDiscAPI.tiers()` to generate them. Do not modify this mod's `ModRecipeProvider` or `ModItemTagsProvider` to hardcode another mod's items.

If you want this mod's enchantments and tags to recognize new discs, at minimum add the item to the `throwing_disc:throwing_disc` tag.

## Behavior

Discs registered through the API automatically receive:

- Right-click throwing from the main hand and off hand.
- Mob piercing, block bouncing, and automatic return when distance or time runs out.
- Throwing, bouncing, returning, and catching sound effects.
- Durability, anvil repair, and fire-resistance behavior.
- After being added to the `throwing_disc:throwing_disc` tag, support for all disc enchantments: Impaling, Ramming, Aerodynamics, Expansion, Ricochet, Magnetic Return, and Preservation.
- After being added to `minecraft:enchantable/fire_aspect`, support for vanilla Fire Aspect; every mob hit along the line is ignited according to the enchantment level.

Ramming's break tier comes from `DiscTier.breakTier()`; addons can set it for their own materials. The other enchantments require no extra configuration.

## Caveats

- Items registered through the API always use the `throwing_disc` namespace.
- `name` must be unique; duplicate registration throws an exception.
- `register()` must be called before `RegisterEvent`.
- Item tags must be supplied through a datapack or data generation; they cannot be modified only at runtime.
- `ThrowingDiscAPI.tiers()` returns a copy of the tiers registered in the current JVM.
- Addon resource files are merged with this mod's resources; avoid overwriting existing files when using the same paths.

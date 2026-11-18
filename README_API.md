# Throwing Disc API

[English](README_API_EN.md) | 简体中文

本模组为其他 NeoForge 1.21.1 模组提供了添加飞盘等级的 API。

API 位于 `com.chinaex123.throwing_disc.api`，主要类是 `DiscTier` 和 `ThrowingDiscAPI`。通过 API 注册的飞盘会自动使用本模组的物品行为、投掷物、渲染和音效；把物品加入 `throwing_disc:throwing_disc` 标签后，也会自动支持全部飞盘附魔，不需要额外注册实体或客户端渲染器。

## 快速开始

### 1. 定义等级

```java
package com.example.mydiscs;

import com.chinaex123.throwing_disc.api.DiscTier;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;

public final class MyDiscTiers {
    public static final DiscTier TIN = new DiscTier(
            "tin",                      // 名称前缀
            3.0F,                       // 伤害
            300,                        // 耐久
            12,                         // 附魔能力
            Rarity.COMMON,              // 稀有度
            false,                      // 是否防火
            () -> MyItems.TIN_INGOT.get(), // 合成和修复材料
            Tiers.STONE                 // “冲撞”可破坏的方块等级
    );
}
```

如果合成材料和修复材料不同：

```java
public static final DiscTier TIN = new DiscTier(
        "tin", 3.0F, 300, 12, Rarity.COMMON, false,
        () -> MyItems.TIN_INGOT.get(),  // 合成材料
        () -> MyItems.TIN_NUGGET.get(), // 修复材料
        Tiers.STONE
);
```

材料供应器也可以返回物品标签，例如铜锭使用 NeoForge 的 `c:ingots/copper` 标签：

```java
public static final DiscTier COPPER = new DiscTier(
        "copper", 2.0F, 128, 6, Rarity.COMMON, false,
        () -> Tags.Items.INGOTS_COPPER, // 合成和修复材料
        Tiers.STONE
);
```

物品和标签可以分别用于合成与修复；配方数据生成时物品材料会写成具体物品，标签材料会写成 `"tag": "..."`。

### 2. 注册

在自己的模组构造阶段调用，必须早于 `RegisterEvent`：

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

批量注册：

```java
ThrowingDiscAPI.registerAll(
        MyDiscTiers.TIN,
        MyDiscTiers.BRONZE,
        MyDiscTiers.STEEL
);
```

`register` 会返回对应的 `DeferredItem<DiscItem>`：

```java
DeferredItem<DiscItem> tinDisc = ThrowingDiscAPI.register(MyDiscTiers.TIN);
```

注册后的物品 ID 为：

```text
throwing_disc:tin_throwing_disc
```

## DiscTier

`DiscTier` 只描述飞盘属性，不负责注册。它实现了 `StringRepresentable`，并提供以下构造方法：

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

参数说明：

- `name`：等级名称，只能包含小写字母、数字和下划线，必须匹配 `[a-z0-9_]+`。
- `attackDamage`：命中生物的基础伤害，不能为负数。
- `durability`：最大耐久，必须大于 0。
- `enchantmentValue`：附魔能力，不能为负数。
- `rarity`：物品稀有度。
- `fireResistant`：物品是否防火。
- `recipeIngredient`：合成材料，仅用于数据生成；供应器可返回 `Item` 或 `TagKey<Item>`。
- `repairIngredient`：铁砧修复材料；供应器同样可返回 `Item` 或 `TagKey<Item>`。
- `breakTier`：“冲撞”附魔使用的挖掘等级，决定飞盘能破坏哪些方块；省略时默认 `Tiers.IRON`。

常用 `breakTier` 对应关系：

- 石：`Tiers.STONE`
- 铁：`Tiers.IRON`
- 金：`Tiers.GOLD`
- 钻石：`Tiers.DIAMOND`
- 下界合金：`Tiers.NETHERITE`

物品 ID 和翻译键会自动生成：

```text
物品 ID:   throwing_disc:<name>_throwing_disc
翻译键:    item.throwing_disc.<name>_throwing_disc
```

## 注册 API

```java
ThrowingDiscAPI.register(DiscTier tier);
ThrowingDiscAPI.registerAll(DiscTier... tiers);
ThrowingDiscAPI.tiers();
ThrowingDiscAPI.items();
```

- `register`：注册一个等级并返回 `DeferredItem<DiscItem>`。同一个 `DiscTier` 不能重复注册。
- `registerAll`：按顺序批量注册。
- `tiers`：返回当前已注册等级的只读列表。
- `items`：返回当前已注册物品的列表。

本模组内置等级位于 `init/TDDiscTiers.java`，可以通过 `TDDiscTiers.COPPER.item().get()` 获取对应物品。

注册必须发生在 `RegisterEvent` 之前。通常放在模组构造方法中是安全的；不要在注册事件结束后再调用。

## 标签 API

飞盘使用五个物品标签：

- `throwing_disc:throwing_disc`：所有飞盘。飞盘附魔的 `supported_items` 和 `primary_items` 都引用这个标签。
- `c:throwing_disc`：NeoForge 通用物品标签，方便其他模组按通用标签识别飞盘。
- `minecraft:enchantable/durability`：允许耐久、经验修补等耐久类附魔。
- `minecraft:enchantable/vanishing`：允许消失诅咒。
- `minecraft:enchantable/fire_aspect`：允许火焰附加。

对应的 API：

```java
ThrowingDiscAPI.discTag();
ThrowingDiscAPI.commonDiscTag();
ThrowingDiscAPI.durabilityTag();
ThrowingDiscAPI.vanishingTag();
ThrowingDiscAPI.fireAspectTag();
```

### 数据生成

物品标签是数据包内容，不能在游戏运行时动态修改。`register()` 只注册物品，不会自动把物品加入标签。

在自己的 `ItemTagsProvider` 中可以这样生成：

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

`addToAllTags` 有三个重载：

```java
// 只加入飞盘标签和耐久附魔标签，保留用于兼容旧调用
addToAllTags(discTag, durabilityTag, tier);

// 加入飞盘、耐久附魔和消失诅咒三个标签
addToAllTags(discTag, durabilityTag, vanishingTag, tier);

// 推荐：加入飞盘、耐久、消失诅咒和火焰附加四个标签
addToAllTags(discTag, durabilityTag, vanishingTag, fireAspectTag, tier);
```

`addToTags` 用于把飞盘加入任意一个标签，例如 `c:throwing_disc`。

### 手写标签 JSON

如果不用数据生成，也可以直接提供：

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

标签路径使用单数 `tags/item`。

## 资源文件

通过 API 注册的物品统一使用 `throwing_disc` 命名空间，因此材质、模型和语言文件也要放在这个命名空间下。

以 `tin` 为例：

```text
assets/throwing_disc/textures/item/tin_throwing_disc.png
assets/throwing_disc/models/item/tin_throwing_disc.json
assets/throwing_disc/lang/en_us.json
assets/throwing_disc/lang/zh_cn.json
```

模型文件：

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "throwing_disc:item/tin_throwing_disc"
  }
}
```

语言文件：

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

配方由接入方自行生成或手写。推荐使用与本模组一致的环形配方，四个材料围绕成环：

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

## 数据生成说明

本模组自己的 `runData` 只会处理本模组内置的五个等级。其他模组注册的等级不会自动出现在本模组的数据生成结果里。

因此接入方需要在自己的模组中生成：

- 物品模型
- 配方和进度
- 五个物品标签
- 语言文件

可以在自己的 `runData` 中注册等级，然后用 `ThrowingDiscAPI.tiers()` 遍历生成；不要修改本模组的 `ModRecipeProvider` 或 `ModItemTagsProvider` 来硬编码其他模组的物品。

如果希望本模组的附魔和标签自动识别新飞盘，至少要把物品加入 `throwing_disc:throwing_disc` 标签。

## 行为说明

通过 API 注册的飞盘会自动获得：

- 主手和副手右键投掷。
- 穿透生物、方块反弹、距离或时间耗尽后返回。
- 投掷、反弹、返回、接住音效。
- 耐久、铁砧修复和防火逻辑。
- 加入 `throwing_disc:throwing_disc` 标签后，支持全部飞盘附魔：贯穿、冲撞、空气动力学、扩展、回旋、磁返、保留。
- 加入 `minecraft:enchantable/fire_aspect` 后支持原版火焰附加，沿途每个被命中的生物都会按等级着火。

“冲撞”的破坏等级来自 `DiscTier.breakTier()`，接入方可以按自己的材料设置。其余附魔不需要额外配置。

## 注意事项

- API 注册的物品固定使用 `throwing_disc` 命名空间。
- `name` 必须唯一，重复注册会抛出异常。
- `register()` 必须早于 `RegisterEvent`。
- 物品标签必须通过数据包或数据生成提供，不能只在运行时修改。
- `ThrowingDiscAPI.tiers()` 返回的是当前 JVM 中已注册等级的副本。
- 接入方的资源文件会和本模组资源合并；如果使用相同路径，需要避免覆盖本模组已有文件。

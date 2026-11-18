package com.chinaex123.throwing_disc.init;

import com.chinaex123.throwing_disc.ThrowingDisc;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface TDItemTags {

    /** 所有飞盘。 */
    TagKey<Item> THROWING_DISC = TagKey.create(Registries.ITEM, ThrowingDisc.id("throwing_disc"));
    /** c:throwing_disc。 */
    TagKey<Item> COMMON_THROWING_DISC = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "throwing_disc")
    );
    /** 耐久附魔。 */
    TagKey<Item> ENCHANTABLE_DURABILITY = ItemTags.DURABILITY_ENCHANTABLE;
    /** 消失诅咒附魔。 */
    TagKey<Item> ENCHANTABLE_VANISHING = ItemTags.VANISHING_ENCHANTABLE;
    /** 火焰附加附魔，允许通过附魔书或铁砧应用到飞盘。 */
    TagKey<Item> ENCHANTABLE_FIRE_ASPECT = ItemTags.FIRE_ASPECT_ENCHANTABLE;
}

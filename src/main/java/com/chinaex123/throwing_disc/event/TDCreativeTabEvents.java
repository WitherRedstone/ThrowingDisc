package com.chinaex123.throwing_disc.event;

import com.chinaex123.throwing_disc.ThrowingDisc;
import com.chinaex123.throwing_disc.init.TDCreativeTabs;
import com.chinaex123.throwing_disc.init.TDDiscTiers;
import com.chinaex123.throwing_disc.init.TDEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * 飞盘创造模式物品栏事件类。
 * <p>
 * 在构建飞盘物品栏时，于下界合金飞盘之后插入各飞盘附魔的全部等级附魔书，
 * 方便玩家直接取用测试各档附魔效果。
 */
@EventBusSubscriber(modid = ThrowingDisc.MODID)
public final class TDCreativeTabEvents {

    /**
     * 处理创造模式物品栏内容构建事件。
     * <p>
     * 仅处理飞盘物品栏；在下界合金飞盘之后依次插入
     * 贯穿、冲撞、空气动力学、扩展、回旋、磁返与保留附魔的全等级附魔书。
     *
     * @param event 创造模式物品栏内容构建事件
     */
    @SubscribeEvent
    public static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(TDCreativeTabs.THROWING_DISC_TAB.getKey())) {
            return;
        }

        HolderLookup.RegistryLookup<Enchantment> lookup =
                event.getParameters().holders().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack anchor = new ItemStack(TDDiscTiers.NETHERITE.item().get());

        addAllLevelEnchantmentBooksAfter(event, lookup, TDEnchantments.IMPALING, anchor);
        addAllLevelEnchantmentBooksAfter(event, lookup, TDEnchantments.RAMMING, anchor);
        addAllLevelEnchantmentBooksAfter(event, lookup, TDEnchantments.AERODYNAMICS, anchor);
        addAllLevelEnchantmentBooksAfter(event, lookup, TDEnchantments.EXPANSION, anchor);
        addAllLevelEnchantmentBooksAfter(event, lookup, TDEnchantments.RICOCHET, anchor);
        addAllLevelEnchantmentBooksAfter(event, lookup, TDEnchantments.MAGNETIC_RETURN, anchor);
        addAllLevelEnchantmentBooksAfter(event, lookup, TDEnchantments.PRESERVATION, anchor);
    }

    /**
     * 在指定物品之后插入某附魔的全部等级附魔书。
     * <p>
     * 从附魔注册表读取该附魔并遍历其全部等级，
     * 逐个生成附魔书并插入到锚点物品之后，同时显示在父标签与搜索结果中。
     *
     * @param event  创造模式物品栏内容构建事件
     * @param lookup 附魔注册表查询
     * @param key    附魔资源键
     * @param anchor 锚点物品
     */
    private static void addAllLevelEnchantmentBooksAfter(
            BuildCreativeModeTabContentsEvent event,
            HolderLookup.RegistryLookup<Enchantment> lookup,
            ResourceKey<Enchantment> key,
            ItemStack anchor) {
        lookup.get(key).ifPresent(holder -> {
            int maxLevel = holder.value().getMaxLevel();
            for (int level = 1; level <= maxLevel; level++) {
                ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, level));
                event.insertAfter(anchor, book, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        });
    }
}

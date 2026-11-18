package com.chinaex123.throwing_disc.data.enchantments;

import com.chinaex123.throwing_disc.init.TDEnchantments;
import com.chinaex123.throwing_disc.init.TDItemTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 模组附魔数据生成器。
 * <p>
 * 通过注册表构建器生成飞盘相关附魔定义：
 * 以飞盘物品标签为可附魔目标，依次注册穿刺、空气动力学、扩展、
 * 弹射、磁力回收与保存附魔；撞击附魔额外需要穿刺附魔作为依赖。
 */
public final class ModEnchantmentProvider {

    /** 附魔注册表构建器。 */
    public static final RegistrySetBuilder DATA_BUILDER = new RegistrySetBuilder()
            .add(Registries.ENCHANTMENT, ModEnchantmentProvider::bootstrap);

    /**
     * 引导附魔注册。
     * <p>
     * 以飞盘物品标签作为可附魔物品集合，注册各类飞盘附魔；
     * 撞击附魔还需以穿刺附魔作为依赖。
     *
     * @param context 引导上下文
     */
    private static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderSet<Item> discs = items.getOrThrow(TDItemTags.THROWING_DISC);

        register(context, TDEnchantments.IMPALING, TDEnchantmentBuilders.impaling(discs));
        register(context, TDEnchantments.AERODYNAMICS, TDEnchantmentBuilders.aerodynamics(discs));
        register(context, TDEnchantments.EXPANSION, TDEnchantmentBuilders.expansion(discs));
        register(context, TDEnchantments.RICOCHET, TDEnchantmentBuilders.ricochet(discs));
        register(context, TDEnchantments.MAGNETIC_RETURN, TDEnchantmentBuilders.magneticReturn(discs));
        register(context, TDEnchantments.PRESERVATION, TDEnchantmentBuilders.preservation(discs));

        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        HolderSet<Enchantment> impaling = HolderSet.direct(enchantments.getOrThrow(TDEnchantments.IMPALING));
        register(context, TDEnchantments.RAMMING, TDEnchantmentBuilders.ramming(discs, impaling));
    }

    /**
     * 注册单个附魔。
     *
     * @param context 引导上下文
     * @param key     附魔资源键
     * @param builder 附魔构建器
     */
    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.location()));
    }
}

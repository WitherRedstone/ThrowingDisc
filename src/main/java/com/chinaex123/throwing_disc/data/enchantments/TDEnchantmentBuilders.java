package com.chinaex123.throwing_disc.data.enchantments;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 飞盘附魔构建器。
 * <p>
 * 集中定义各飞盘附魔的注册参数：可附魔物品、权重、最大等级、
 * 等级消耗、铁砧消耗与生效槽位，并为每个附魔设置自定义名称颜色。
 */
public final class TDEnchantmentBuilders {

    /**
     * 贯穿：飞盘每次命中额外造成 1 点伤害，最高 5 级。
     */
    public static Enchantment.Builder impaling(HolderSet<Item> discs) {
        return Enchantment.enchantment(Enchantment.definition(
                discs, discs,
                2, 5,
                Enchantment.dynamicCost(1, 8),
                Enchantment.dynamicCost(21, 8),
                4,
                EquipmentSlotGroup.HAND
        )).withCustomName(name -> name.withStyle(style -> style.withColor(ChatFormatting.AQUA)));
    }

    /**
     * 冲撞：飞盘不再伤害生物，改为按材质等级破坏方块。
     * <p>
     * 与贯穿附魔互斥。
     */
    public static Enchantment.Builder ramming(HolderSet<Item> discs, HolderSet<Enchantment> impaling) {
        return Enchantment.enchantment(Enchantment.definition(
                        discs, discs,
                        2, 1,
                        Enchantment.constantCost(1),
                        Enchantment.constantCost(41),
                        4,
                        EquipmentSlotGroup.HAND
                ))
                .exclusiveWith(impaling)
                .withCustomName(name -> name.withStyle(style -> style.withColor(ChatFormatting.GOLD)));
    }

    /**
     * 空气动力学：每级增加 1 格飞行距离，最高 5 级。
     */
    public static Enchantment.Builder aerodynamics(HolderSet<Item> discs) {
        return Enchantment.enchantment(Enchantment.definition(
                discs, discs,
                2, 5,
                Enchantment.dynamicCost(1, 8),
                Enchantment.dynamicCost(21, 8),
                4,
                EquipmentSlotGroup.HAND
        )).withCustomName(name -> name.withStyle(style -> style.withColor(ChatFormatting.BLUE)));
    }

    /**
     * 扩展：配合冲撞使用，每级扩大 1 格破坏范围，最高 2 级。
     */
    public static Enchantment.Builder expansion(HolderSet<Item> discs) {
        return Enchantment.enchantment(Enchantment.definition(
                discs, discs,
                2, 2,
                Enchantment.dynamicCost(1, 8),
                Enchantment.dynamicCost(21, 8),
                4,
                EquipmentSlotGroup.HAND
        )).withCustomName(name -> name.withStyle(style -> style.withColor(ChatFormatting.GREEN)));
    }

    /**
     * 回旋：每级增加 1 次可反弹次数，最高 5 级。
     */
    public static Enchantment.Builder ricochet(HolderSet<Item> discs) {
        return Enchantment.enchantment(Enchantment.definition(
                discs, discs,
                2, 5,
                Enchantment.dynamicCost(1, 8),
                Enchantment.dynamicCost(21, 8),
                4,
                EquipmentSlotGroup.HAND
        )).withCustomName(name -> name.withStyle(style -> style.withColor(ChatFormatting.YELLOW)));
    }

    /**
     * 磁返：每级提高返回速度，最高 3 级；配合冲撞可带回方块掉落物。
     */
    public static Enchantment.Builder magneticReturn(HolderSet<Item> discs) {
        return Enchantment.enchantment(Enchantment.definition(
                discs, discs,
                2, 3,
                Enchantment.dynamicCost(1, 8),
                Enchantment.dynamicCost(21, 8),
                4,
                EquipmentSlotGroup.HAND
        )).withCustomName(name -> name.withStyle(style -> style.withColor(ChatFormatting.LIGHT_PURPLE)));
    }

    /**
     * 保留：飞盘耐久将要耗尽时保留 1 点耐久，不会消失，但会进入已损坏状态。
     */
    public static Enchantment.Builder preservation(HolderSet<Item> discs) {
        return Enchantment.enchantment(Enchantment.definition(
                discs, discs,
                2, 1,
                Enchantment.constantCost(1),
                Enchantment.constantCost(41),
                4,
                EquipmentSlotGroup.HAND
        )).withCustomName(name -> name.withStyle(style -> style.withColor(ChatFormatting.DARK_AQUA)));
    }
}

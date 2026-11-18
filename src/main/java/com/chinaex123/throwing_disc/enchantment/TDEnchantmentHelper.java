package com.chinaex123.throwing_disc.enchantment;

import com.chinaex123.throwing_disc.init.TDEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * 飞盘附魔查询工具类。
 * <p>
 * 提供各类飞盘附魔的等级查询与存在性判断，包括贯穿、冲撞、
 * 空气动力学、扩展、回旋、磁返与保留附魔；
 * 并提供不依赖世界注册表的保留附魔检测与「保留损坏」状态判断。
 */
public final class TDEnchantmentHelper {

    private TDEnchantmentHelper() {
    }

    /**
     * 获取贯穿附魔等级。
     *
     * @param disc  飞盘物品堆叠
     * @param level 世界
     * @return 附魔等级，无附魔时为 0
     */
    public static int getImpalingLevel(ItemStack disc, Level level) {
        return getLevel(disc, level, TDEnchantments.IMPALING);
    }

    /**
     * 获取冲撞附魔等级。
     *
     * @param disc  飞盘物品堆叠
     * @param level 世界
     * @return 附魔等级，无附魔时为 0
     */
    public static int getRammingLevel(ItemStack disc, Level level) {
        return getLevel(disc, level, TDEnchantments.RAMMING);
    }

    /**
     * 判断是否具有冲撞附魔。
     *
     * @param disc  飞盘物品堆叠
     * @param level 世界
     * @return 具有冲撞附魔返回 true
     */
    public static boolean hasRamming(ItemStack disc, Level level) {
        return getRammingLevel(disc, level) > 0;
    }

    /**
     * 获取空气动力学附魔等级。
     *
     * @param disc  飞盘物品堆叠
     * @param level 世界
     * @return 附魔等级，无附魔时为 0
     */
    public static int getAerodynamicsLevel(ItemStack disc, Level level) {
        return getLevel(disc, level, TDEnchantments.AERODYNAMICS);
    }

    /**
     * 获取扩展附魔等级。
     *
     * @param disc  飞盘物品堆叠
     * @param level 世界
     * @return 附魔等级，无附魔时为 0
     */
    public static int getExpansionLevel(ItemStack disc, Level level) {
        return getLevel(disc, level, TDEnchantments.EXPANSION);
    }

    /**
     * 获取回旋附魔等级。
     *
     * @param disc  飞盘物品堆叠
     * @param level 世界
     * @return 附魔等级，无附魔时为 0
     */
    public static int getRicochetLevel(ItemStack disc, Level level) {
        return getLevel(disc, level, TDEnchantments.RICOCHET);
    }

    /**
     * 获取磁返附魔等级。
     *
     * @param disc  飞盘物品堆叠
     * @param level 世界
     * @return 附魔等级，无附魔时为 0
     */
    public static int getMagneticReturnLevel(ItemStack disc, Level level) {
        return getLevel(disc, level, TDEnchantments.MAGNETIC_RETURN);
    }

    /**
     * 获取保留附魔等级。
     *
     * @param disc  飞盘物品堆叠
     * @param level 世界
     * @return 附魔等级，无附魔时为 0
     */
    public static int getPreservationLevel(ItemStack disc, Level level) {
        return getLevel(disc, level, TDEnchantments.PRESERVATION);
    }

    /**
     * 获取火焰附加附魔等级。
     *
     * @param disc  飞盘物品堆叠
     * @param level 世界
     * @return 附魔等级，无附魔时为 0
     */
    public static int getFireAspectLevel(ItemStack disc, Level level) {
        return getLevel(disc, level, Enchantments.FIRE_ASPECT);
    }

    /**
     * 判断是否具有保留附魔。
     *
     * @param disc  飞盘物品堆叠
     * @param level 世界
     * @return 具有保留附魔返回 true
     */
    public static boolean hasPreservation(ItemStack disc, Level level) {
        return getPreservationLevel(disc, level) > 0;
    }

    /**
     * 判断是否具有保留附魔（不依赖世界注册表）。
     * <p>
     * 直接遍历物品堆叠上的附魔数据，匹配保留附魔且等级大于 0。
     *
     * @param disc 飞盘物品堆叠
     * @return 具有保留附魔返回 true
     */
    public static boolean hasPreservation(ItemStack disc) {
        if (disc.isEmpty()) {
            return false;
        }

        for (var entry : disc.getTagEnchantments().entrySet()) {
            if (entry.getKey().is(TDEnchantments.PRESERVATION) && entry.getIntValue() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断飞盘是否处于「保留损坏」状态。
     * <p>
     * 物品可损坏、耐久只剩最后一点且具有保留附魔时视为损坏但未消失。
     *
     * @param disc 飞盘物品堆叠
     * @return 处于保留损坏状态返回 true
     */
    public static boolean isPreservedBroken(ItemStack disc) {
        return disc.isDamageableItem()
                && disc.getDamageValue() >= disc.getMaxDamage() - 1
                && hasPreservation(disc);
    }

    /**
     * 查询指定附魔在飞盘上的等级。
     * <p>
     * 物品为空时返回 0；从世界的附魔注册表查找对应附魔并读取等级，找不到时返回 0。
     *
     * @param disc        飞盘物品堆叠
     * @param level       世界
     * @param enchantment 附魔资源键
     * @return 附魔等级，无附魔时为 0
     */
    private static int getLevel(ItemStack disc, Level level, ResourceKey<Enchantment> enchantment) {
        if (disc.isEmpty()) {
            return 0;
        }

        return level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(enchantment)
                .map(disc::getEnchantmentLevel)
                .orElse(0);
    }
}

package com.chinaex123.throwing_disc.init;

import com.chinaex123.throwing_disc.ThrowingDisc;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public interface TDEnchantments {

    /** 贯穿：每级提高飞盘的穿透伤害。 */
    ResourceKey<Enchantment> IMPALING = key("impaling");
    /** 冲撞：飞盘不再伤害生物，改为按材质等级破坏方块。 */
    ResourceKey<Enchantment> RAMMING = key("ramming");
    /** 空气动力学：每级增加飞盘的飞行距离。 */
    ResourceKey<Enchantment> AERODYNAMICS = key("aerodynamics");
    /** 扩展：每级扩大冲撞破坏方块的范围。 */
    ResourceKey<Enchantment> EXPANSION = key("expansion");
    /** 回旋：每级增加飞盘可以反弹的次数。 */
    ResourceKey<Enchantment> RICOCHET = key("ricochet");
    /** 磁返：提高飞盘的返回速度，并可在冲撞时带回方块掉落物。 */
    ResourceKey<Enchantment> MAGNETIC_RETURN = key("magnetic_return");
    /** 保留：飞盘耐久耗尽时保留 1 点耐久，不会消失，但会进入已损坏状态。 */
    ResourceKey<Enchantment> PRESERVATION = key("preservation");

    static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, ThrowingDisc.id(path));
    }
}

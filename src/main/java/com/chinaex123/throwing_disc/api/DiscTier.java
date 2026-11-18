package com.chinaex123.throwing_disc.api;

import com.chinaex123.throwing_disc.ThrowingDisc;
import com.chinaex123.throwing_disc.item.DiscItem;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 飞盘等级。
 * <p>
 * 描述一种飞盘的完整属性：名称、攻击伤害、耐久、附魔能力、稀有度、
 * 防火属性、合成材料、修复材料与挖掘等级。
 * 构造时对参数做严格校验，注册后通过绑定方法关联对应物品。
 *
 * <p>它只描述飞盘的属性、配方材料和修复材料，不负责注册。
 * 注册请调用 {@link ThrowingDiscAPI#register(DiscTier)}。
 *
 * <p>材料供应器既可以返回单个物品，也可以返回物品标签：
 * <pre>{@code
 * DiscTier tin = new DiscTier(
 *     "tin", 3.0F, 300, 12, Rarity.COMMON, false,
 *     () -> MyItems.TIN_INGOT.get()
 * );
 * DiscTier copper = new DiscTier(
 *     "copper", 2.0F, 128, 6, Rarity.COMMON, false,
 *     () -> Tags.Items.INGOTS_COPPER, Tiers.STONE
 * );
 * ThrowingDiscAPI.register(tin);
 * }</pre>
 */
public final class DiscTier implements StringRepresentable {

    /** 等级名称。 */
    private final String name;

    /** 攻击伤害。 */
    private final float attackDamage;

    /** 耐久值。 */
    private final int durability;

    /** 附魔能力。 */
    private final int enchantmentValue;

    /** 稀有度。 */
    private final Rarity rarity;

    /** 是否防火。 */
    private final boolean fireResistant;

    /** 合成材料。 */
    private final DiscMaterial recipeIngredient;

    /** 修复材料。 */
    private final DiscMaterial repairIngredient;

    /** 挖掘等级。 */
    private final Tier breakTier;

    /** 绑定的飞盘物品。 */
    private DeferredItem<DiscItem> item;

    /**
     * 构造飞盘等级（合成与修复材料相同，挖掘等级为铁）。
     *
     * @param name             等级名称
     * @param attackDamage     攻击伤害
     * @param durability       耐久值
     * @param enchantmentValue 附魔能力
     * @param rarity           稀有度
     * @param fireResistant    是否防火
     * @param ingredient       合成与修复材料
     */
    public DiscTier(String name, float attackDamage, int durability, int enchantmentValue,
                    Rarity rarity, boolean fireResistant, Supplier<?> ingredient) {
        this(name, attackDamage, durability, enchantmentValue, rarity, fireResistant,
                ingredient, ingredient, Tiers.IRON);
    }

    /**
     * 构造飞盘等级（挖掘等级为铁）。
     *
     * @param name             等级名称
     * @param attackDamage     攻击伤害
     * @param durability       耐久值
     * @param enchantmentValue 附魔能力
     * @param rarity           稀有度
     * @param fireResistant    是否防火
     * @param recipeIngredient 合成材料
     * @param repairIngredient 修复材料
     */
    public DiscTier(String name, float attackDamage, int durability, int enchantmentValue,
                    Rarity rarity, boolean fireResistant,
                    Supplier<?> recipeIngredient, Supplier<?> repairIngredient) {
        this(name, attackDamage, durability, enchantmentValue, rarity, fireResistant,
                recipeIngredient, repairIngredient, Tiers.IRON);
    }

    /**
     * 构造飞盘等级（合成与修复材料相同）。
     *
     * @param name             等级名称
     * @param attackDamage     攻击伤害
     * @param durability       耐久值
     * @param enchantmentValue 附魔能力
     * @param rarity           稀有度
     * @param fireResistant    是否防火
     * @param ingredient       合成与修复材料
     * @param breakTier        挖掘等级
     */
    public DiscTier(String name, float attackDamage, int durability, int enchantmentValue,
                    Rarity rarity, boolean fireResistant, Supplier<?> ingredient, Tier breakTier) {
        this(name, attackDamage, durability, enchantmentValue, rarity, fireResistant,
                ingredient, ingredient, breakTier);
    }

    /**
     * 构造飞盘等级。
     * <p>
     * 校验名称格式、攻击伤害非负、耐久为正、附魔能力非负，
     * 并确保稀有度、材料与挖掘等级均非空。
     *
     * @param name             等级名称
     * @param attackDamage     攻击伤害
     * @param durability       耐久值
     * @param enchantmentValue 附魔能力
     * @param rarity           稀有度
     * @param fireResistant    是否防火
     * @param recipeIngredient 合成材料
     * @param repairIngredient 修复材料
     * @param breakTier        挖掘等级
     */
    public DiscTier(String name, float attackDamage, int durability, int enchantmentValue,
                    Rarity rarity, boolean fireResistant,
                    Supplier<?> recipeIngredient, Supplier<?> repairIngredient, Tier breakTier) {
        if (name == null || !name.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("Disc tier name must match [a-z0-9_]+: " + name);
        }
        if (attackDamage < 0.0F) {
            throw new IllegalArgumentException("Attack damage cannot be negative");
        }
        if (durability <= 0) {
            throw new IllegalArgumentException("Durability must be positive");
        }
        if (enchantmentValue < 0) {
            throw new IllegalArgumentException("Enchantment value cannot be negative");
        }

        this.name = name;
        this.attackDamage = attackDamage;
        this.durability = durability;
        this.enchantmentValue = enchantmentValue;
        this.rarity = Objects.requireNonNull(rarity, "rarity");
        this.fireResistant = fireResistant;
        this.recipeIngredient = DiscMaterial.of(recipeIngredient);
        this.repairIngredient = DiscMaterial.of(repairIngredient);
        this.breakTier = Objects.requireNonNull(breakTier, "breakTier");
    }

    /**
     * 获取序列化名称。
     *
     * @return 序列化名称
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    /**
     * 获取对应物品的注册名。
     *
     * @return 物品注册名
     */
    public String itemName() {
        return this.name + "_throwing_disc";
    }

    /**
     * 获取对应物品的翻译键。
     *
     * @return 翻译键
     */
    public String translationKey() {
        return "item." + ThrowingDisc.MODID + "." + this.itemName();
    }

    /**
     * 获取攻击伤害。
     *
     * @return 攻击伤害
     */
    public float attackDamage() {
        return this.attackDamage;
    }

    /**
     * 获取耐久值。
     *
     * @return 耐久值
     */
    public int durability() {
        return this.durability;
    }

    /**
     * 获取附魔能力。
     *
     * @return 附魔能力
     */
    public int enchantmentValue() {
        return this.enchantmentValue;
    }

    /**
     * 获取稀有度。
     *
     * @return 稀有度
     */
    public Rarity rarity() {
        return this.rarity;
    }

    /**
     * 判断是否防火。
     *
     * @return 是否防火
     */
    public boolean fireResistant() {
        return this.fireResistant;
    }

    /**
     * 获取合成材料供应器。
     *
     * @return 合成材料供应器
     */
    public DiscMaterial recipeIngredient() {
        return this.recipeIngredient;
    }

    /**
     * 获取修复材料供应器。
     *
     * @return 修复材料供应器
     */
    public DiscMaterial repairIngredient() {
        return this.repairIngredient;
    }

    /**
     * 获取挖掘等级。
     *
     * @return 挖掘等级
     */
    public Tier breakTier() {
        return this.breakTier;
    }

    /**
     * 获取绑定的飞盘物品。
     *
     * @return 飞盘物品
     */
    public DeferredItem<DiscItem> item() {
        return this.item;
    }

    /**
     * 绑定飞盘物品。
     * <p>
     * 每个等级只允许绑定一次，重复绑定抛出异常。
     *
     * @param item 飞盘物品
     */
    void bindItem(DeferredItem<DiscItem> item) {
        if (this.item != null) {
            throw new IllegalStateException("Disc tier " + this.name + " is already registered");
        }
        this.item = Objects.requireNonNull(item, "item");
    }
}

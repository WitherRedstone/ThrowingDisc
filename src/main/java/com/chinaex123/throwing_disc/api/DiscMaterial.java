package com.chinaex123.throwing_disc.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 飞盘材料。
 * <p>
 * 以延迟供应器描述一种材料，可表示单个物品或物品标签；
 * 首次访问时解析供应器并缓存为配方原料，
 * 提供匹配判断以及按类型读取物品或标签的方法。
 */
public final class DiscMaterial {

    /** 材料供应器，延迟获取物品或物品标签。 */
    private final Supplier<?> supplier;

    /** 是否已解析。 */
    private boolean resolved;

    /** 解析得到的配方原料。 */
    private Ingredient ingredient;

    /** 解析得到的单个物品，标签类型时为 null。 */
    @Nullable
    private Item item;

    /** 解析得到的物品标签，单个物品类型时为 null。 */
    @Nullable
    private TagKey<Item> tag;

    /**
     * 私有构造函数。
     *
     * @param supplier 材料供应器
     */
    private DiscMaterial(Supplier<?> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "materialSupplier");
    }

    /**
     * 创建飞盘材料。
     *
     * @param supplier 返回物品或物品标签的供应器
     * @return 飞盘材料
     */
    public static DiscMaterial of(Supplier<?> supplier) {
        return new DiscMaterial(supplier);
    }

    /**
     * 解析材料供应器。
     * <p>
     * 仅解析一次：供应器返回物品时构造物品原料，返回物品标签时构造标签原料；
     * 返回其他类型或非物品注册表的标签时抛出异常。
     */
    private void resolve() {
        if (this.resolved) {
            return;
        }

        Object value = this.supplier.get();
        if (value instanceof Item resolvedItem) {
            this.ingredient = Ingredient.of(resolvedItem);
            this.item = resolvedItem;
        } else if (value instanceof TagKey<?> rawTag) {
            if (!rawTag.registry().equals(Registries.ITEM)) {
                throw new IllegalArgumentException("Disc material tag must be an item tag: " + rawTag);
            }
            @SuppressWarnings("unchecked")
            TagKey<Item> itemTag = (TagKey<Item>) rawTag;
            this.ingredient = Ingredient.of(itemTag);
            this.tag = itemTag;
        } else {
            throw new IllegalArgumentException(
                    "Disc material supplier must return Item or TagKey<Item>, got "
                            + (value == null ? "null" : value.getClass().getName()));
        }

        this.resolved = true;
    }

    /**
     * 获取配方原料。
     *
     * @return 配方原料
     */
    public Ingredient ingredient() {
        this.resolve();
        return this.ingredient;
    }

    /**
     * 判断物品堆叠是否匹配本材料。
     *
     * @param stack 物品堆叠
     * @return 匹配返回 true
     */
    public boolean test(ItemStack stack) {
        return this.ingredient().test(stack);
    }

    /**
     * 判断材料是否为物品标签。
     *
     * @return 为标签时返回 true
     */
    public boolean isTag() {
        this.resolve();
        return this.tag != null;
    }

    /**
     * 获取单个物品。
     *
     * @return 物品
     */
    public Item item() {
        this.resolve();
        if (this.item == null) {
            throw new IllegalStateException("Disc material is a tag, not a single item: " + this.tag);
        }
        return this.item;
    }

    /**
     * 获取物品标签。
     *
     * @return 物品标签
     */
    public TagKey<Item> tag() {
        this.resolve();
        if (this.tag == null) {
            throw new IllegalStateException("Disc material is a single item, not a tag: " + this.item);
        }
        return this.tag;
    }
}
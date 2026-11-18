package com.chinaex123.throwing_disc.api;

import com.chinaex123.throwing_disc.init.TDItemTags;
import com.chinaex123.throwing_disc.init.TDItems;
import com.chinaex123.throwing_disc.item.DiscItem;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 飞盘 API。
 * <p>
 * 面向模组开发者提供飞盘等级的注册与查询入口：
 * 注册时创建并绑定飞盘物品、维护等级列表，
 * 同时暴露飞盘相关物品标签，并提供把等级物品加入标签的数据生成辅助方法。
 */
public final class ThrowingDiscAPI {

    /** 已注册的飞盘等级列表。 */
    private static final List<DiscTier> TIERS = new ArrayList<>();

    private ThrowingDiscAPI() {
    }

    /**
     * 注册一个飞盘等级并创建对应物品。
     * <p>
     * 等级不可为空且不得重复注册；注册成功后绑定物品并加入等级列表。
     *
     * @param tier 飞盘等级
     * @return 注册后的飞盘物品
     */
    public static DeferredItem<DiscItem> register(DiscTier tier) {
        Objects.requireNonNull(tier, "tier");
        if (tier.item() != null) {
            throw new IllegalStateException("Disc tier " + tier.getSerializedName() + " is already registered");
        }

        DeferredItem<DiscItem> item = TDItems.ITEMS.register(tier.itemName(), () -> new DiscItem(tier));
        tier.bindItem(item);
        TIERS.add(tier);
        return item;
    }

    /**
     * 批量注册多个飞盘等级。
     *
     * @param tiers 飞盘等级
     */
    public static void registerAll(DiscTier... tiers) {
        for (DiscTier tier : tiers) {
            register(tier);
        }
    }

    /**
     * 获取所有已注册的飞盘等级。
     *
     * @return 等级列表（不可变）
     */
    public static List<DiscTier> tiers() {
        return List.copyOf(TIERS);
    }

    /**
     * 获取所有已注册的飞盘物品。
     *
     * @return 飞盘物品列表
     */
    public static List<DeferredItem<DiscItem>> items() {
        return TIERS.stream().map(DiscTier::item).toList();
    }

    /**
     * 获取飞盘物品标签。
     *
     * @return 飞盘物品标签
     */
    public static TagKey<Item> discTag() {
        return TDItemTags.THROWING_DISC;
    }

    /**
     * 获取普通飞盘物品标签。
     *
     * @return 普通飞盘物品标签
     */
    public static TagKey<Item> commonDiscTag() {
        return TDItemTags.COMMON_THROWING_DISC;
    }

    /**
     * 获取可附魔耐久物品标签。
     *
     * @return 可附魔耐久物品标签
     */
    public static TagKey<Item> durabilityTag() {
        return TDItemTags.ENCHANTABLE_DURABILITY;
    }

    /**
     * 获取可附魔消失诅咒物品标签。
     *
     * @return 可附魔消失诅咒物品标签
     */
    public static TagKey<Item> vanishingTag() {
        return TDItemTags.ENCHANTABLE_VANISHING;
    }

    /**
     * 获取可附魔火焰附加物品标签。
     *
     * @return 可附魔火焰附加物品标签
     */
    public static TagKey<Item> fireAspectTag() {
        return TDItemTags.ENCHANTABLE_FIRE_ASPECT;
    }

    /**
     * 将等级对应的物品加入指定标签。
     *
     * @param appender 标签追加器
     * @param tier     飞盘等级
     */
    public static void addToTags(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> appender, DiscTier tier) {
        appender.add(requireItem(tier).get());
    }

    /**
     * 将等级对应的物品加入飞盘标签与耐久附魔标签。
     *
     * @param discTag       飞盘标签追加器
     * @param durabilityTag 耐久附魔标签追加器
     * @param tier          飞盘等级
     */
    public static void addToAllTags(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> discTag,
                                    IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> durabilityTag,
                                    DiscTier tier) {
        addToTags(discTag, tier);
        addToTags(durabilityTag, tier);
    }

    /**
     * 数据生成用：把等级对应的物品加入飞盘标签、耐久附魔标签和消失诅咒标签。
     * <p>
     * 示例：
     * <pre>{@code
     * var discTag = tag(ThrowingDiscAPI.discTag());
     * var durabilityTag = tag(ThrowingDiscAPI.durabilityTag());
     * var vanishingTag = tag(ThrowingDiscAPI.vanishingTag());
     * ThrowingDiscAPI.addToAllTags(discTag, durabilityTag, vanishingTag, myTier);
     * }</pre>
     *
     * @param discTag       飞盘标签追加器
     * @param durabilityTag 耐久附魔标签追加器
     * @param vanishingTag  消失诅咒标签追加器
     * @param tier          飞盘等级
     */
    public static void addToAllTags(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> discTag,
                                    IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> durabilityTag,
                                    IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> vanishingTag,
                                    DiscTier tier) {
        addToTags(discTag, tier);
        addToTags(durabilityTag, tier);
        addToTags(vanishingTag, tier);
    }

    /**
     * 数据生成用：把等级对应的物品加入飞盘、耐久、消失诅咒和火焰附加标签。
     *
     * @param discTag        飞盘标签追加器
     * @param durabilityTag  耐久附魔标签追加器
     * @param vanishingTag   消失诅咒标签追加器
     * @param fireAspectTag  火焰附加标签追加器
     * @param tier           飞盘等级
     */
    public static void addToAllTags(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> discTag,
                                    IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> durabilityTag,
                                    IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> vanishingTag,
                                    IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> fireAspectTag,
                                    DiscTier tier) {
        addToTags(discTag, tier);
        addToTags(durabilityTag, tier);
        addToTags(vanishingTag, tier);
        addToTags(fireAspectTag, tier);
    }

    /**
     * 获取等级对应的飞盘物品，未注册时抛出异常。
     *
     * @param tier 飞盘等级
     * @return 飞盘物品
     */
    private static DeferredItem<DiscItem> requireItem(DiscTier tier) {
        Objects.requireNonNull(tier, "tier");
        DeferredItem<DiscItem> item = tier.item();
        if (item == null) {
            throw new IllegalStateException("Disc tier " + tier.getSerializedName() + " is not registered");
        }
        return item;
    }
}

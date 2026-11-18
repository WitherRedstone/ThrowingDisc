package com.chinaex123.throwing_disc.item;

import com.chinaex123.throwing_disc.entity.DiscProjectile;
import com.chinaex123.throwing_disc.api.DiscTier;
import com.chinaex123.throwing_disc.enchantment.TDEnchantmentHelper;
import com.chinaex123.throwing_disc.init.TDDiscTiers;
import com.chinaex123.throwing_disc.init.TDSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 飞盘物品。
 * <p>
 * 由飞盘等级决定耐久、稀有度、附魔能力、修复材料与防火属性。
 * 使用时生成飞盘弹射物并清空手持物品；保留附魔留下的最后 1 点耐久
 * 视为已损坏，无法继续投掷。金质飞盘可作为猪灵货币。
 */
public class DiscItem extends Item {

    /** 飞盘等级。 */
    private final DiscTier tier;

    /**
     * 构造飞盘物品。
     *
     * @param tier 飞盘等级
     */
    public DiscItem(DiscTier tier) {
        super(createProperties(tier));
        this.tier = tier;
    }

    /**
     * 根据等级创建物品属性。
     * <p>
     * 不可堆叠、按等级设置耐久与稀有度，等级防火时附加防火属性。
     *
     * @param tier 飞盘等级
     * @return 物品属性
     */
    private static Properties createProperties(DiscTier tier) {
        Properties properties = new Properties()
                .stacksTo(1)
                .durability(tier.durability())
                .rarity(tier.rarity());
        if (tier.fireResistant()) {
            properties.fireResistant();
        }
        return properties;
    }

    /**
     * 获取飞盘等级。
     *
     * @return 飞盘等级
     */
    public DiscTier getTier() {
        return this.tier;
    }

    /**
     * 获取附魔能力。
     *
     * @param stack 物品堆叠
     * @return 附魔能力
     */
    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return this.tier.enchantmentValue();
    }

    /**
     * 判断飞盘是否为指定附魔的主要物品。
     * <p>
     * 火焰附加的原版主要物品是剑；这里额外把飞盘视为火焰附加的主要物品，
     * 使其能正常出现在附魔台中，同时不影响其他附魔的原版判定。
     *
     * @param stack       物品堆叠
     * @param enchantment 附魔
     * @return 可作为主要物品附魔时返回 true
     */
    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.FIRE_ASPECT)) {
            return true;
        }

        var definition = enchantment.value().definition();
        return stack.is(definition.supportedItems())
                && (definition.primaryItems().isEmpty() || stack.is(definition.primaryItems().get()));
    }

    /**
     * 使用物品投掷飞盘。
     * <p>
     * 旁观者与保留损坏状态下无法投掷；服务端生成飞盘弹射物、
     * 播放投掷音效、记录统计并清空手持物品。
     *
     * @param level    世界
     * @param player   玩家
     * @param usedHand 使用手
     * @return 交互结果
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (player.isSpectator()) {
            return InteractionResultHolder.pass(stack);
        }

        // 保留附魔留下的最后 1 点耐久视为已损坏，不能再投掷。
        if (TDEnchantmentHelper.isPreservedBroken(stack)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            DiscProjectile disc = new DiscProjectile(level, player, stack.copy(), usedHand);
            disc.setPos(player.getX(), player.getEyeY() - 0.15D, player.getZ());
            disc.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, DiscProjectile.THROW_SPEED, 0.0F);
            level.addFreshEntity(disc);

            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    TDSounds.DISC_THROW.get(),
                    SoundSource.PLAYERS,
                    0.9F,
                    1.0F
            );

            player.awardStat(Stats.ITEM_USED.get(this));
            player.swing(usedHand, true);
            player.setItemInHand(usedHand, ItemStack.EMPTY);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /**
     * 追加物品提示文本。
     * <p>
     * 处于保留损坏状态时显示损坏提示，并附带飞盘用法与返回说明。
     *
     * @param stack   物品堆叠
     * @param context 提示上下文
     * @param tooltip 提示组件列表
     * @param flag    提示标志
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (TDEnchantmentHelper.isPreservedBroken(stack)) {
            tooltip.add(Component.translatable("item.throwing_disc.broken").withStyle(ChatFormatting.DARK_RED));
        }
        tooltip.add(Component.translatable("item.throwing_disc.disc.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.throwing_disc.disc.tooltip_return").withStyle(ChatFormatting.DARK_GRAY));
    }

    /**
     * 判断是否为有效修复材料。
     *
     * @param stack           待修复物品堆叠
     * @param repairCandidate 修复材料堆叠
     * @return 与等级修复材料匹配时返回 true
     */
    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return this.tier.repairIngredient().test(repairCandidate);
    }

    /**
     * 判断是否可作为猪灵货币。
     *
     * @param stack 物品堆叠
     * @return 金质飞盘返回 true
     */
    @Override
    public boolean isPiglinCurrency(ItemStack stack) {
        return this.tier == TDDiscTiers.GOLD;
    }
}

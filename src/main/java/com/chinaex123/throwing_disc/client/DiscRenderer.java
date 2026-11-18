package com.chinaex123.throwing_disc.client;

import com.chinaex123.throwing_disc.entity.DiscProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 飞盘弹射物渲染器。
 * <p>
 * 以物品形式渲染飞行中的飞盘：绕竖直轴持续旋转、翻转至水平并放大，
 * 绘制时使用地面显示上下文；贴图位置固定为方块图集。
 */
public class DiscRenderer extends EntityRenderer<DiscProjectile> {

    /** 飞盘旋转速度（度/刻）。 */
    private static final float SPIN_SPEED = 42.0F;

    /** 物品渲染器。 */
    private final ItemRenderer itemRenderer;

    /**
     * 构造飞盘渲染器。
     *
     * @param context 渲染器上下文
     */
    public DiscRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.25F;
    }

    /**
     * 渲染飞盘。
     * <p>
     * 飞盘物品为空时不渲染；否则按时间计算旋转角度，
     * 绕竖直轴旋转、绕水平轴翻转并放大后以物品形式绘制。
     *
     * @param entity      飞盘弹射物
     * @param entityYaw   实体偏航角
     * @param partialTick 插值刻
     * @param poseStack   姿态栈
     * @param buffer      缓冲区
     * @param packedLight 打包光照
     */
    @Override
    public void render(DiscProjectile entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        float spin = (entity.tickCount + partialTick) * SPIN_SPEED;

        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.scale(1.5F, 1.5F, 1.5F);

        this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    /**
     * 获取实体贴图位置。
     *
     * @param entity 飞盘弹射物
     * @return 方块图集位置
     */
    @Override
    public ResourceLocation getTextureLocation(DiscProjectile entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

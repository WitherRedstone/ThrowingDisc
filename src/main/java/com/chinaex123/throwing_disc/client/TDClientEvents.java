package com.chinaex123.throwing_disc.client;

import com.chinaex123.throwing_disc.ThrowingDisc;
import com.chinaex123.throwing_disc.init.TDEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 飞盘模组客户端事件类。
 * <p>
 * 注册客户端渲染器：为飞盘弹射物绑定对应的实体渲染器。
 */
@EventBusSubscriber(modid = ThrowingDisc.MODID, value = Dist.CLIENT)
public final class TDClientEvents {

    /**
     * 注册实体渲染器。
     * <p>
     * 为飞盘弹射物实体注册飞盘渲染器。
     *
     * @param event 实体渲染器注册事件
     */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TDEntities.DISC_ENTITY.get(), DiscRenderer::new);
    }
}

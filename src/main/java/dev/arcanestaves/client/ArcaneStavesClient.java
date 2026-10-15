package dev.arcanestaves.client;

import dev.arcanestaves.ArcaneStaves;
import dev.arcanestaves.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Только клиент: регистрация рендереров. Класс не загружается на выделенном сервере. */
@EventBusSubscriber(modid = ArcaneStaves.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ArcaneStavesClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // fullBright = true: спрайт светится независимо от освещения
        event.registerEntityRenderer(ModEntities.FIRE_PROJECTILE.get(), ctx -> new ThrownItemRenderer<>(ctx, 1.5F, true));
        event.registerEntityRenderer(ModEntities.SWAP_BOLT.get(), ctx -> new ThrownItemRenderer<>(ctx, 1.2F, true));
        event.registerEntityRenderer(ModEntities.SINGULARITY_ORB.get(), ctx -> new ThrownItemRenderer<>(ctx, 1.5F, true));
        event.registerEntityRenderer(ModEntities.SINGULARITY.get(), SingularityRenderer::new);
    }

    private ArcaneStavesClient() {}
}

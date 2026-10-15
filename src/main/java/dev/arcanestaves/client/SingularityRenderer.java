package dev.arcanestaves.client;

import dev.arcanestaves.entity.SingularityEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Пустой рендерер: весь визуал сингулярности — частицы, которые создаёт сама сущность. */
public class SingularityRenderer extends EntityRenderer<SingularityEntity> {

    public SingularityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SingularityEntity entity) {
        // Не используется (ничего не рисуем), но метод обязан вернуть существующий путь
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}

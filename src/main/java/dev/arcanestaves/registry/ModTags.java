package dev.arcanestaves.registry;

import dev.arcanestaves.ArcaneStaves;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/** Теги мода (JSON лежат в data/arcanestaves/tags/entity_type/). */
public final class ModTags {
    /** Сущности, с которыми нельзя поменяться местами. */
    public static final TagKey<EntityType<?>> SWAP_IMMUNE =
            TagKey.create(Registries.ENTITY_TYPE, ArcaneStaves.id("swap_immune"));
    /** Сущности, которые игнорирует сингулярность (боссы). */
    public static final TagKey<EntityType<?>> SINGULARITY_IMMUNE =
            TagKey.create(Registries.ENTITY_TYPE, ArcaneStaves.id("singularity_immune"));

    private ModTags() {}
}

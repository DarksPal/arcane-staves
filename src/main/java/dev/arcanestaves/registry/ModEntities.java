package dev.arcanestaves.registry;

import dev.arcanestaves.ArcaneStaves;
import dev.arcanestaves.entity.FireStaffProjectile;
import dev.arcanestaves.entity.SingularityEntity;
import dev.arcanestaves.entity.SingularityOrbProjectile;
import dev.arcanestaves.entity.SwapBoltProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ArcaneStaves.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<FireStaffProjectile>> FIRE_PROJECTILE =
            ENTITY_TYPES.register("fire_staff_projectile", () ->
                    EntityType.Builder.<FireStaffProjectile>of(FireStaffProjectile::new, MobCategory.MISC)
                            .sized(0.3F, 0.3F).clientTrackingRange(4).updateInterval(10)
                            .build("fire_staff_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<SwapBoltProjectile>> SWAP_BOLT =
            ENTITY_TYPES.register("swap_bolt", () ->
                    EntityType.Builder.<SwapBoltProjectile>of(SwapBoltProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F).clientTrackingRange(6).updateInterval(5)
                            .build("swap_bolt"));

    public static final DeferredHolder<EntityType<?>, EntityType<SingularityOrbProjectile>> SINGULARITY_ORB =
            ENTITY_TYPES.register("singularity_orb", () ->
                    EntityType.Builder.<SingularityOrbProjectile>of(SingularityOrbProjectile::new, MobCategory.MISC)
                            .sized(0.4F, 0.4F).clientTrackingRange(4).updateInterval(10)
                            .build("singularity_orb"));

    public static final DeferredHolder<EntityType<?>, EntityType<SingularityEntity>> SINGULARITY =
            ENTITY_TYPES.register("singularity", () ->
                    EntityType.Builder.<SingularityEntity>of(SingularityEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F).fireImmune().clientTrackingRange(10).updateInterval(20)
                            .build("singularity"));

    private ModEntities() {}
}

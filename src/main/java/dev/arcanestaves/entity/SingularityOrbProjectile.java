package dev.arcanestaves.entity;

import dev.arcanestaves.registry.ModEntities;
import dev.arcanestaves.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Медленная сфера. Гравитация стандартная для ThrowableProjectile (0.03, как у снежка) —
 * для «слабой» она достаточно мала на дистанции 0.7 * 40 = ~28 блоков.
 * При попадании или через 40 тиков превращается в SingularityEntity.
 */
public class SingularityOrbProjectile extends ThrowableItemProjectile {
    public static final float SPEED = 0.7F;
    public static final int MAX_FLIGHT = 40;

    public SingularityOrbProjectile(EntityType<? extends SingularityOrbProjectile> type, Level level) {
        super(type, level);
    }

    public SingularityOrbProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.SINGULARITY_ORB.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SINGULARITY_ORB_SPRITE.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.PORTAL, getX(), getY(), getZ(), 0, 0, 0);
            this.level().addParticle(ParticleTypes.SQUID_INK, getX(), getY(), getZ(), 0, 0, 0);
        } else if (this.tickCount >= MAX_FLIGHT) {
            spawnSingularity(this.position());
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            return;
        }
        Vec3 loc = result.getLocation();
        if (result instanceof BlockHitResult block) {
            // Смещаем центр аномалии от поверхности, чтобы она не оказалась внутри блока
            loc = loc.add(Vec3.atLowerCornerOf(block.getDirection().getNormal()).scale(0.8));
        }
        spawnSingularity(loc);
        this.discard();
    }

    private void spawnSingularity(Vec3 pos) {
        if (this.level() instanceof ServerLevel level) {
            SingularityEntity singularity = new SingularityEntity(ModEntities.SINGULARITY.get(), level);
            singularity.init(pos, this.getOwner());
            level.addFreshEntity(singularity);
        }
    }
}

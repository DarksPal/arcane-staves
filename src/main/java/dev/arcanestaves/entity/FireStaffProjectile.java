package dev.arcanestaves.entity;

import dev.arcanestaves.config.StaffConfig;
import dev.arcanestaves.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Огненный шар. Выбран ThrowableItemProjectile (а не AbstractHurtingProjectile):
 * готовый рендер через ThrownItemRenderer, готовая детекция попаданий, стабильный API,
 * гравитация отключается одной строкой. AbstractHurtingProjectile ускоряется «силой»
 * и менял конструкторы между версиями, поэтому здесь он только добавил бы риск.
 */
public class FireStaffProjectile extends ThrowableItemProjectile {
    public static final float SPEED = 1.5F;
    public static final int MAX_LIFE = 100;

    public FireStaffProjectile(EntityType<? extends FireStaffProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public FireStaffProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.FIRE_PROJECTILE.get(), shooter, level);
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        // Сопротивление воздуха/воды в ThrowableProjectile тормозит снаряд — возвращаем постоянную скорость
        Vec3 v = this.getDeltaMovement();
        double len = v.length();
        if (len > 1.0E-4) {
            this.setDeltaMovement(v.scale(SPEED / len));
        }

        if (this.level().isClientSide) {
            // Частицы в полёте
            this.level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0, 0, 0);
            this.level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0.02, 0);
        } else if (this.tickCount > MAX_LIFE) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) {
            return;
        }
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        if (target.hurt(this.damageSources().thrown(this, owner), StaffConfig.FIRE_DAMAGE.get().floatValue())) {
            target.igniteForSeconds(StaffConfig.FIRE_IGNITE_SECONDS.get());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel level
                && StaffConfig.FIRE_IGNITE_BLOCKS.get()
                && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            igniteNearestFlammable(level, result);
        }
    }

    /** Ищет ближайший горючий блок в области 3x3x3 вокруг точки попадания и ставит огонь рядом с ним. Блоки не разрушаются. */
    private void igniteNearestFlammable(ServerLevel level, BlockHitResult hit) {
        Vec3 loc = hit.getLocation();
        BlockPos center = hit.getBlockPos();
        BlockPos bestFire = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos cand : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            BlockState state = level.getBlockState(cand);
            if (state.isAir()) {
                continue;
            }
            for (Direction dir : Direction.values()) {
                if (!state.isFlammable(level, cand, dir)) {
                    continue;
                }
                BlockPos firePos = cand.relative(dir);
                if (!level.isEmptyBlock(firePos) || !BaseFireBlock.canBePlacedAt(level, firePos, dir)) {
                    continue;
                }
                if (this.getOwner() instanceof Player p && !level.mayInteract(p, firePos)) {
                    continue; // защита спавна и приваты
                }
                double d = Vec3.atCenterOf(firePos).distanceToSqr(loc);
                if (d < bestDist) {
                    bestDist = d;
                    bestFire = firePos.immutable();
                }
            }
        }
        if (bestFire != null) {
            level.setBlockAndUpdate(bestFire, BaseFireBlock.getState(level, bestFire));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level) {
            Vec3 p = result.getLocation();
            // Вспышка при попадании (без EXPLOSION_EMITTER)
            level.sendParticles(ParticleTypes.EXPLOSION, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 20, 0.2, 0.2, 0.2, 0.05);
            level.sendParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 10, 0.2, 0.2, 0.2, 0.02);
            level.playSound(null, p.x, p.y, p.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            this.discard();
        }
    }
}

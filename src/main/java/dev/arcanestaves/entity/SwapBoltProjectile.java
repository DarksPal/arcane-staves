package dev.arcanestaves.entity;

import dev.arcanestaves.config.StaffConfig;
import dev.arcanestaves.item.SwapStaffItem;
import dev.arcanestaves.registry.ModEntities;
import dev.arcanestaves.registry.ModItems;
import dev.arcanestaves.registry.ModTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** «Эхо-луч» посоха Обмена. */
public class SwapBoltProjectile extends ThrowableItemProjectile {
    public static final float SPEED = 2.5F;
    public static final int MAX_LIFE = 12; // 12 * 2.5 = ~30 блоков

    public SwapBoltProjectile(EntityType<? extends SwapBoltProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public SwapBoltProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.SWAP_BOLT.get(), shooter, level);
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SWAP_BOLT_SPRITE.get();
    }

    /** Луч реагирует только на живые сущности: рамки, лодки и т.п. он пролетает насквозь. */
    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof LivingEntity && super.canHitEntity(entity);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        Vec3 v = this.getDeltaMovement();
        double len = v.length();
        if (len > 1.0E-4) {
            this.setDeltaMovement(v.scale(SPEED / len));
        }

        if (this.level().isClientSide) {
            // Скорость высокая, поэтому рисуем след из нескольких точек за тик
            Vec3 m = this.getDeltaMovement();
            for (int i = 0; i < 3; i++) {
                double f = i / 3.0;
                double px = getX() - m.x * f, py = getY() - m.y * f, pz = getZ() - m.z * f;
                this.level().addParticle(ParticleTypes.REVERSE_PORTAL, px, py, pz, 0, 0, 0);
                this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0, 0);
            }
        } else if (this.tickCount >= MAX_LIFE) {
            // Луч иссяк в воздухе — как промах по блоку
            refundCooldown();
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) {
            return;
        }
        if (!(this.getOwner() instanceof ServerPlayer owner) || !(result.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (trySwap(owner, target)) {
            damageStaff(owner);
        } else {
            refundCooldown();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            // Ничего не происходит, 50% кулдауна возвращается, прочность не тратится
            refundCooldown();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    /** Возврат 50% кулдауна: новый остаток = половина полного кулдауна. */
    private void refundCooldown() {
        if (this.getOwner() instanceof ServerPlayer owner) {
            owner.getCooldowns().addCooldown(ModItems.SWAP_STAFF.get(), StaffConfig.SWAP_COOLDOWN.get() / 2);
        }
    }

    /** Тратит 1 прочности у посоха Обмена в руках стрелка (в творческом режиме hurtAndBreak ничего не делает). */
    private static void damageStaff(ServerPlayer owner) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = owner.getItemInHand(hand);
            if (held.getItem() instanceof SwapStaffItem) {
                held.hurtAndBreak(1, owner, LivingEntity.getSlotForHand(hand));
                return;
            }
        }
    }

    private static boolean holdsSwapStaff(LivingEntity e) {
        return e.getMainHandItem().getItem() instanceof SwapStaffItem
                || e.getOffhandItem().getItem() instanceof SwapStaffItem;
    }

    private static boolean isFree(ServerLevel level, Entity entity, AABB box) {
        return !level.getBlockCollisions(entity, box).iterator().hasNext()
                && level.getWorldBorder().isWithinBounds(box);
    }

    /**
     * Обмен местами. Возвращает true, если обмен состоялся.
     * Отражение: если у цели в руке посох Обмена, позиции меняются как обычно (обмен A<->B симметричен,
     * «обратный порядок» ничего бы не изменил), а вот эффекты отражаются:
     * бонус получает цель, а штраф — стрелок.
     */
    private boolean trySwap(ServerPlayer owner, LivingEntity target) {
        ServerLevel level = (ServerLevel) this.level();

        // Только в пределах одного измерения, без боссов
        if (target.level() != owner.level() || target.getType().is(ModTags.SWAP_IMMUNE)) {
            return false;
        }
        // Транспорт и пассажиры блокируют обмен у обеих сторон
        if (target.isPassenger() || target.isVehicle() || owner.isPassenger() || owner.isVehicle()) {
            return false;
        }

        Vec3 ownerPos = owner.position();
        Vec3 targetPos = target.position();

        // Проверка безопасности: хитбоксы в новых местах не должны пересекаться с блоками
        AABB ownerAtTarget = owner.getBoundingBox().move(targetPos.subtract(ownerPos));
        AABB targetAtOwner = target.getBoundingBox().move(ownerPos.subtract(targetPos));
        if (!isFree(level, owner, ownerAtTarget) || !isFree(level, target, targetAtOwner)) {
            owner.displayClientMessage(Component.translatable("message.arcanestaves.swap_blocked"), true);
            return false;
        }

        // Эффекты в точках ДО перемещения (старые позиции)
        swapFx(level, ownerPos);
        swapFx(level, targetPos);

        teleport(owner, targetPos);
        teleport(target, ownerPos);

        // Эффекты после перемещения
        swapFx(level, ownerPos);
        swapFx(level, targetPos);

        boolean reflect = holdsSwapStaff(target);
        LivingEntity buffed = reflect ? target : owner;
        LivingEntity debuffed = reflect ? owner : target;

        buffed.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                StaffConfig.SWAP_RESISTANCE_SECONDS.get() * 20, 0));
        int debuffTicks = StaffConfig.SWAP_DEBUFF_SECONDS.get() * 20;
        debuffed.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, debuffTicks, 1));
        debuffed.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, debuffTicks, 0));
        return true;
    }

    /**
     * Телепорт внутри измерения. Для игрока используем пакет connection.teleport (надёжно синхронизирует клиент),
     * для остальных — moveTo + сброс скорости и навигации.
     */
    private static void teleport(LivingEntity entity, Vec3 pos) {
        if (entity instanceof ServerPlayer player) {
            player.connection.teleport(pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
        } else {
            entity.moveTo(pos.x, pos.y, pos.z, entity.getYRot(), entity.getXRot());
            entity.setDeltaMovement(Vec3.ZERO);
            if (entity instanceof Mob mob) {
                mob.getNavigation().stop();
            }
        }
        entity.resetFallDistance();
    }

    private static void swapFx(ServerLevel level, Vec3 p) {
        level.sendParticles(ParticleTypes.PORTAL, p.x, p.y + 1.0, p.z, 40, 0.4, 0.8, 0.4, 0.3);
        level.playSound(null, p.x, p.y, p.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}

package dev.arcanestaves.entity;

import dev.arcanestaves.config.StaffConfig;
import dev.arcanestaves.registry.ModTags;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Гравитационная аномалия. Висит на месте, плавно тянет живых существ и предметы через setDeltaMovement,
 * раз в 10 тиков бьёт ядро, в конце схлопывается ударной волной. Блоки не трогает.
 */
public class SingularityEntity extends Entity {
    /** Срок жизни синхронизируется клиенту, потому что COMMON-конфиг на клиенте может отличаться. */
    private static final EntityDataAccessor<Integer> DATA_LIFETIME =
            SynchedEntityData.defineId(SingularityEntity.class, EntityDataSerializers.INT);

    @Nullable
    private UUID ownerUUID;
    /** Серверный возраст (сохраняется в NBT). */
    private int life;
    /** Предметы, дошедшие до центра: вернём кучкой после коллапса. */
    private final List<ItemStack> captured = new ArrayList<>();

    public SingularityEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /** Инициализация сервером при создании из снаряда. */
    public void init(Vec3 pos, @Nullable Entity owner) {
        this.setPos(pos);
        this.ownerUUID = owner == null ? null : owner.getUUID();
        this.entityData.set(DATA_LIFETIME, StaffConfig.SING_LIFETIME.get());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LIFETIME, 80);
    }

    // ------------------------------------------------------------------ тик

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            clientVisuals();
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        Vec3 center = this.position();
        int lifetime = this.entityData.get(DATA_LIFETIME);
        this.life++;

        pullEntities(level, center);

        if (this.life % 10 == 0) {
            pulseDamage(level, center);
        }
        if (this.life % 10 == 1) {
            playHum(level, center, (float) this.life / lifetime);
        }
        if (this.life >= lifetime) {
            collapse(level, center);
        }
    }

    /** Подходит ли сущность для воздействия: живая или предмет, не владелец, не босс, не спектатор. */
    private boolean canAffect(Entity e) {
        if (!e.isAlive() || e.isSpectator()) {
            return false;
        }
        if (!(e instanceof LivingEntity) && !(e instanceof ItemEntity)) {
            return false;
        }
        if (e.getType().is(ModTags.SINGULARITY_IMMUNE)) {
            return false;
        }
        return this.ownerUUID == null || !this.ownerUUID.equals(e.getUUID());
    }

    /** Плавное притяжение: сила растёт при приближении к центру; скорость демпфируется, чтобы не было колебаний. */
    private void pullEntities(ServerLevel level, Vec3 center) {
        double radius = StaffConfig.SING_PULL_RADIUS.get();
        double strength = StaffConfig.SING_PULL_STRENGTH.get();
        AABB box = new AABB(center, center).inflate(radius);

        for (Entity e : level.getEntitiesOfClass(Entity.class, box, this::canAffect)) {
            Vec3 target = e.position().add(0, e.getBbHeight() * 0.5, 0);
            Vec3 delta = center.subtract(target);
            double dist = delta.length();
            if (dist > radius) {
                continue;
            }
            if (e instanceof ItemEntity item && dist < 0.6) {
                // Предмет дошёл до центра: прячем в «карман» аномалии
                this.captured.add(item.getItem().copy());
                item.discard();
                continue;
            }
            double closeness = 1.0 - dist / radius; // 0 на границе, 1 в центре
            double force = strength * (0.3 + 1.7 * closeness);
            force *= Math.min(1.0, dist); // у самого центра гасим силу — без дрожания
            Vec3 dir = dist > 1.0E-3 ? delta.scale(1.0 / dist) : Vec3.ZERO;

            Vec3 motion = e.getDeltaMovement().scale(0.8).add(dir.scale(force));
            if (motion.lengthSqr() > 1.0) {
                motion = motion.normalize(); // потолок скорости 1 блок/тик
            }
            e.setDeltaMovement(motion);
            e.fallDistance = 0;
            if (e instanceof ServerPlayer) {
                e.hurtMarked = true; // принудительная синхронизация скорости игроку
            }
        }
    }

    /** Магический урон всем в малом радиусе вокруг ядра. */
    private void pulseDamage(ServerLevel level, Vec3 center) {
        double r = StaffConfig.SING_PULSE_RADIUS.get();
        float dmg = StaffConfig.SING_PULSE_DAMAGE.get().floatValue();
        Entity owner = getOwnerEntity(level);
        AABB box = new AABB(center, center).inflate(r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, this::canAffect)) {
            if (e.distanceToSqr(center) <= r * r) {
                e.invulnerableTime = 0; // чтобы кадры неуязвимости не съедали импульс
                e.hurt(level.damageSources().indirectMagic(this, owner), dmg);
            }
        }
    }

    /** Гул: громкость и высота растут к концу жизни. */
    private void playHum(ServerLevel level, Vec3 center, float progress) {
        float p = Mth.clamp(progress, 0.0F, 1.0F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.BEACON_AMBIENT,
                SoundSource.PLAYERS, 1.0F + 2.0F * p, 0.5F + 1.0F * p);
    }

    /** Коллапс: ударная волна, урон, отброс, выброс накопленных предметов. Блоки не разрушаются. */
    private void collapse(ServerLevel level, Vec3 center) {
        double r = StaffConfig.SING_COLLAPSE_RADIUS.get();
        float dmg = StaffConfig.SING_COLLAPSE_DAMAGE.get().floatValue();
        double kb = StaffConfig.SING_COLLAPSE_KNOCKBACK.get();
        Entity owner = getOwnerEntity(level);
        AABB box = new AABB(center, center).inflate(r);

        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, this::canAffect)) {
            Vec3 away = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(center);
            double d = away.length();
            if (d > r) {
                continue;
            }
            Vec3 dir;
            if (d < 1.0E-3) {
                double a = this.random.nextDouble() * Math.PI * 2;
                dir = new Vec3(Math.cos(a), 0, Math.sin(a));
            } else {
                dir = away.scale(1.0 / d);
            }
            e.invulnerableTime = 0;
            e.hurt(level.damageSources().indirectMagic(this, owner), dmg);
            if (!e.isAlive()) {
                continue;
            }
            double falloff = 1.0 - 0.5 * (d / r);
            double resist = Math.max(0.0, 1.0 - e.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            double power = kb * falloff * resist;
            e.setDeltaMovement(e.getDeltaMovement().add(dir.x * power, 0.3 + 0.2 * power, dir.z * power));
            if (e instanceof ServerPlayer) {
                e.hurtMarked = true;
            }
        }

        // Кучка предметов в центре
        for (ItemStack stack : this.captured) {
            ItemEntity drop = new ItemEntity(level, center.x, center.y, center.z, stack);
            drop.setDeltaMovement(this.random.nextGaussian() * 0.05, 0.15 + this.random.nextDouble() * 0.1,
                    this.random.nextGaussian() * 0.05);
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
        }
        this.captured.clear();

        // Звук и частицы (EXPLOSION_EMITTER не используем)
        level.playSound(null, center.x, center.y, center.z, SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.PLAYERS, 2.0F, 0.8F);
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 3, 0.4, 0.4, 0.4, 0.0);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 80, 0.3, 0.3, 0.3, 0.6);
        // Кольцо ударной волны: при count = 0 смещения трактуются как направление скорости
        for (int i = 0; i < 36; i++) {
            double a = i * (Math.PI * 2 / 36);
            level.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 0,
                    Math.cos(a), 0.05, Math.sin(a), 0.7);
        }
        this.discard();
    }

    @Nullable
    private Entity getOwnerEntity(ServerLevel level) {
        return this.ownerUUID == null ? null : level.getEntity(this.ownerUUID);
    }

    // ------------------------------------------------------------ клиентский визуал
    // Метод вызывается только на клиенте (проверка isClientSide в tick), но сам использует
    // лишь общий API Level#addParticle, поэтому отдельный Dist.CLIENT-класс не нужен.

    private void clientVisuals() {
        int lifetime = Math.max(1, this.entityData.get(DATA_LIFETIME));
        float progress = Mth.clamp(this.tickCount / (float) lifetime, 0.0F, 1.0F);
        double cx = getX(), cy = getY(), cz = getZ();
        Level level = this.level();

        // Чёрное ядро
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.SQUID_INK,
                    cx + (random.nextDouble() - 0.5) * 0.3,
                    cy + (random.nextDouble() - 0.5) * 0.3,
                    cz + (random.nextDouble() - 0.5) * 0.3, 0, 0, 0);
        }

        // Три закрученных спиральных рукава, сжимающихся к концу жизни
        double radius = 2.4 * (1.0 - 0.5 * progress);
        for (int arm = 0; arm < 3; arm++) {
            double base = this.tickCount * 0.45 + arm * (Math.PI * 2 / 3);
            for (int k = 0; k < 2; k++) {
                double rr = radius * (0.35 + 0.65 * random.nextDouble());
                double ang = base + rr * 1.3; // чем дальше от центра, тем сильнее закрутка
                double px = cx + Math.cos(ang) * rr;
                double pz = cz + Math.sin(ang) * rr;
                double py = cy + (random.nextDouble() - 0.5) * 0.6;
                double vx = (cx - px) * 0.15 - Math.sin(ang) * 0.05;
                double vz = (cz - pz) * 0.15 + Math.cos(ang) * 0.05;
                level.addParticle(arm == 1 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.PORTAL,
                        px, py, pz, vx, (cy - py) * 0.15, vz);
            }
        }
    }

    // ------------------------------------------------------------------ NBT

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.life = tag.getInt("Life");
        if (tag.contains("Lifetime")) {
            this.entityData.set(DATA_LIFETIME, tag.getInt("Lifetime"));
        }
        this.ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        this.captured.clear();
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack.parse(this.level().registryAccess(), list.get(i)).ifPresent(this.captured::add);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Life", this.life);
        tag.putInt("Lifetime", this.entityData.get(DATA_LIFETIME));
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
        ListTag list = new ListTag();
        for (ItemStack stack : this.captured) {
            if (!stack.isEmpty()) {
                list.add(stack.save(this.level().registryAccess()));
            }
        }
        tag.put("Items", list);
    }
}

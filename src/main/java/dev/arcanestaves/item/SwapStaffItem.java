package dev.arcanestaves.item;

import dev.arcanestaves.config.StaffConfig;
import dev.arcanestaves.entity.SwapBoltProjectile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Посох Обмена: «эхо-луч» меняет стрелка местами с целью. */
public class SwapStaffItem extends AbstractStaffItem {

    public SwapStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean castSpell(Level level, Player player, ItemStack stack, InteractionHand hand) {
        Vec3 look = player.getLookAngle();
        SwapBoltProjectile bolt = new SwapBoltProjectile(level, player);
        bolt.shoot(look.x, look.y, look.z, SwapBoltProjectile.SPEED, 0.0F);
        level.addFreshEntity(bolt);
        return true;
    }

    /** Прочность тратит сам снаряд при успешном обмене (при промахе она не расходуется). */
    @Override
    protected boolean damageOnCast() {
        return false;
    }

    @Override
    protected int getCooldownTicks() {
        return StaffConfig.SWAP_COOLDOWN.get();
    }

    @Override
    protected Item getRepairItem() {
        return Items.ECHO_SHARD;
    }

    @Override
    protected void playCastSound(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_EYE_LAUNCH, SoundSource.PLAYERS, 1.0F, 1.4F);
    }
}

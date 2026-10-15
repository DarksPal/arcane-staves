package dev.arcanestaves.item;

import dev.arcanestaves.config.StaffConfig;
import dev.arcanestaves.entity.FireStaffProjectile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Огненный посох: выпускает огненный шар. */
public class FireStaffItem extends AbstractStaffItem {

    public FireStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean castSpell(Level level, Player player, ItemStack stack, InteractionHand hand) {
        Vec3 look = player.getLookAngle();
        FireStaffProjectile projectile = new FireStaffProjectile(level, player);
        projectile.shoot(look.x, look.y, look.z, FireStaffProjectile.SPEED, 0.0F);
        level.addFreshEntity(projectile);
        return true;
    }

    @Override
    protected int getCooldownTicks() {
        return StaffConfig.FIRE_COOLDOWN.get();
    }

    @Override
    protected Item getRepairItem() {
        return Items.BLAZE_ROD;
    }

    @Override
    protected void playCastSound(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}

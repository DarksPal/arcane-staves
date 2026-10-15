package dev.arcanestaves.item;

import dev.arcanestaves.config.StaffConfig;
import dev.arcanestaves.entity.SingularityOrbProjectile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Посох Сингулярности: бросает сферу, которая превращается в гравитационную аномалию. */
public class SingularityStaffItem extends AbstractStaffItem {

    public SingularityStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean castSpell(Level level, Player player, ItemStack stack, InteractionHand hand) {
        Vec3 look = player.getLookAngle();
        SingularityOrbProjectile orb = new SingularityOrbProjectile(level, player);
        orb.shoot(look.x, look.y, look.z, SingularityOrbProjectile.SPEED, 0.0F);
        level.addFreshEntity(orb);
        return true;
    }

    @Override
    protected int getCooldownTicks() {
        return StaffConfig.SING_COOLDOWN.get();
    }

    @Override
    protected Item getRepairItem() {
        return Items.BREEZE_ROD;
    }

    @Override
    protected void playCastSound(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.6F);
    }
}

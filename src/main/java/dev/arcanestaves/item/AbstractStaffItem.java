package dev.arcanestaves.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Базовый класс посохов. Шаблонный метод {@link #use}: кулдаун, анимация замаха,
 * звук, трата прочности и «только сервер» обрабатываются здесь,
 * наследники реализуют лишь {@link #castSpell}.
 */
public abstract class AbstractStaffItem extends Item {

    protected AbstractStaffItem(Properties properties) {
        super(properties);
    }

    /** Суть заклинания. Вызывается ТОЛЬКО на сервере. Вернуть false — каст не состоялся. */
    protected abstract boolean castSpell(Level level, Player player, ItemStack stack, InteractionHand hand);

    /** Кулдаун в тиках (из конфига). */
    protected abstract int getCooldownTicks();

    /** Предмет для ремонта на наковальне. */
    protected abstract Item getRepairItem();

    /** Звук каста (вызывается на сервере). */
    protected abstract void playCastSound(Level level, Player player);

    /** Тратить ли прочность сразу при касте (посох Обмена тратит её только при успешном обмене). */
    protected boolean damageOnCast() {
        return true;
    }

    @Override
    public final InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        // Клиент: только анимация замаха, вся логика на сервере
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!castSpell(level, player, stack, hand)) {
            return InteractionResultHolder.fail(stack);
        }
        playCastSound(level, player);
        player.getCooldowns().addCooldown(this, getCooldownTicks());
        if (damageOnCast()) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(getRepairItem()) || super.isValidRepairItem(stack, repairCandidate);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        // Описание берётся из lang: item.arcanestaves.<id>.desc
        tooltip.add(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}

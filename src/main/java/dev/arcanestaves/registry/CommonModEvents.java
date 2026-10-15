package dev.arcanestaves.registry;

import dev.arcanestaves.ArcaneStaves;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/** События шины мода, общие для клиента и сервера. */
@EventBusSubscriber(modid = ArcaneStaves.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class CommonModEvents {

    /** Дополнительно кладём посохи в стандартную вкладку Combat. */
    @SubscribeEvent
    public static void onBuildCreativeContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.FIRE_STAFF.get());
            event.accept(ModItems.SWAP_STAFF.get());
            event.accept(ModItems.SINGULARITY_STAFF.get());
        }
    }

    private CommonModEvents() {}
}

package dev.arcanestaves.registry;

import dev.arcanestaves.ArcaneStaves;
import dev.arcanestaves.item.FireStaffItem;
import dev.arcanestaves.item.SingularityStaffItem;
import dev.arcanestaves.item.SwapStaffItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ArcaneStaves.MODID);

    public static final DeferredItem<FireStaffItem> FIRE_STAFF = ITEMS.register("fire_staff",
            () -> new FireStaffItem(new Item.Properties().durability(250).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<SwapStaffItem> SWAP_STAFF = ITEMS.register("swap_staff",
            () -> new SwapStaffItem(new Item.Properties().durability(150).rarity(Rarity.RARE)));

    public static final DeferredItem<SingularityStaffItem> SINGULARITY_STAFF = ITEMS.register("singularity_staff",
            () -> new SingularityStaffItem(new Item.Properties().durability(100).rarity(Rarity.EPIC)));

    // Служебные предметы-спрайты для ThrownItemRenderer (в креатив-вкладки не попадают)
    public static final DeferredItem<Item> SWAP_BOLT_SPRITE = ITEMS.register("swap_bolt",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SINGULARITY_ORB_SPRITE = ITEMS.register("singularity_orb",
            () -> new Item(new Item.Properties()));

    private ModItems() {}
}

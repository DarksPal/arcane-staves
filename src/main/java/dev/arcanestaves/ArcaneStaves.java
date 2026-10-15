package dev.arcanestaves;

import dev.arcanestaves.config.StaffConfig;
import dev.arcanestaves.registry.ModCreativeTabs;
import dev.arcanestaves.registry.ModEntities;
import dev.arcanestaves.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

/** Точка входа мода Arcane Staves. Клиентского кода здесь нет. */
@Mod(ArcaneStaves.MODID)
public class ArcaneStaves {
    public static final String MODID = "arcanestaves";

    public ArcaneStaves(IEventBus modEventBus, ModContainer modContainer) {
        // Регистрируем все DeferredRegister на шине мода
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        // Общий конфиг: serverconfig/ не нужен, COMMON лежит в config/arcanestaves-common.toml
        modContainer.registerConfig(ModConfig.Type.COMMON, StaffConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

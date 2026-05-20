package dev.aerolaser;

import dev.aerolaser.registry.AeroLaserBlocks;
import dev.aerolaser.registry.AeroLaserBlockEntities;
import dev.aerolaser.registry.AeroLaserItems;
import dev.aerolaser.registry.AeroLaserMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(AeroLaserMod.MOD_ID)
public class AeroLaserMod {

    public static final String MOD_ID = "aerolaser";

    public AeroLaserMod(IEventBus modEventBus, ModContainer modContainer) {
        AeroLaserBlocks.BLOCKS.register(modEventBus);
        AeroLaserBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        AeroLaserItems.ITEMS.register(modEventBus);
        AeroLaserMenuTypes.MENUS.register(modEventBus);
    }
}

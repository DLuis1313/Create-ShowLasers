package dev.aerolaser.registry;

import dev.aerolaser.AeroLaserMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AeroLaserItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(AeroLaserMod.MOD_ID);

    public static final DeferredItem<BlockItem> SHOW_LASER = ITEMS.register(
            "show_laser", () -> new BlockItem(AeroLaserBlocks.SHOW_LASER.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> VEIL_SPOTLIGHT = ITEMS.register(
            "veil_spotlight", () -> new BlockItem(AeroLaserBlocks.VEIL_SPOTLIGHT.get(), new Item.Properties()));
}

package dev.aerolaser.registry;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.block.ShowLaserBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AeroLaserBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(AeroLaserMod.MOD_ID);

    /**
     * Show Laser Block — laser decorativo com zoom, varredura e modo show.
     * Funciona como addon do simulated:laser_pointer.
     */
    public static final DeferredBlock<ShowLaserBlock> SHOW_LASER = BLOCKS.register(
            "show_laser",
            () -> new ShowLaserBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(2f, 6f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
            )
    );
}

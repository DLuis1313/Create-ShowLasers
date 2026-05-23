package dev.aerolaser.registry;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.block.ShowLaserBlock;
import dev.aerolaser.block.VeilSpotlightBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AeroLaserBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(AeroLaserMod.MOD_ID);

    public static final DeferredBlock<ShowLaserBlock> SHOW_LASER = BLOCKS.register(
            "show_laser",
            () -> new ShowLaserBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(2f, 6f)
                    .sound(SoundType.METAL).noOcclusion())
    );

    public static final DeferredBlock<VeilSpotlightBlock> VEIL_SPOTLIGHT = BLOCKS.register(
            "veil_spotlight",
            () -> new VeilSpotlightBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK).strength(2f, 6f)
                    .sound(SoundType.METAL).noOcclusion()
                    .lightLevel(state -> state.getValue(VeilSpotlightBlock.POWERED) ? 15 : 0))
    );
}

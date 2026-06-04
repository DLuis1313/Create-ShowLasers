package dev.aerolaser.registry;
import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.block.GlowLampBlock;
import dev.aerolaser.block.ShowLaserBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
public class AeroLaserBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AeroLaserMod.MOD_ID);
    public static final DeferredBlock<ShowLaserBlock> SHOW_LASER = BLOCKS.register("show_laser",
            () -> new ShowLaserBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2f,6f).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<GlowLampBlock> GLOW_LAMP = BLOCKS.register("glow_lamp",
            () -> new GlowLampBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1f,4f).sound(SoundType.GLASS).noOcclusion()
                    .lightLevel(state -> state.getValue(GlowLampBlock.POWERED) ? 7 : 0)));
}

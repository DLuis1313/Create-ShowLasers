package dev.aerolaser.registry;

import dev.aerolaser.AeroLaserMod;
import dev.aerolaser.blockentity.ShowLaserBlockEntity;
import dev.aerolaser.blockentity.VeilSpotlightBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AeroLaserBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AeroLaserMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShowLaserBlockEntity>> SHOW_LASER =
            BLOCK_ENTITIES.register("show_laser",
                    () -> BlockEntityType.Builder.of(ShowLaserBlockEntity::new,
                            AeroLaserBlocks.SHOW_LASER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VeilSpotlightBlockEntity>> VEIL_SPOTLIGHT =
            BLOCK_ENTITIES.register("veil_spotlight",
                    () -> BlockEntityType.Builder.of(VeilSpotlightBlockEntity::new,
                            AeroLaserBlocks.VEIL_SPOTLIGHT.get()).build(null));
}

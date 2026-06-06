package dev.aerolaser.block;

import com.mojang.serialization.MapCodec;
import dev.aerolaser.blockentity.GlowLampBlockEntity;
import dev.aerolaser.registry.AeroLaserBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class GlowLampBlock extends BaseEntityBlock {

    // FACING como pistão: a "face" da lâmpada aponta para a direção configurada
    public static final DirectionProperty FACING  = BlockStateProperties.FACING;
    public static final BooleanProperty   POWERED = BlockStateProperties.POWERED;

    public static final MapCodec<GlowLampBlock> CODEC = simpleCodec(GlowLampBlock::new);
    @Override public MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    public GlowLampBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // A lâmpada aponta para onde o jogador está olhando (igual ao pistão)
        return defaultBlockState()
                .setValue(FACING, ctx.getNearestLookingDirection().getOpposite())
                .setValue(POWERED, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                   Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, powered), 3);
                if (level.getBlockEntity(pos) instanceof GlowLampBlockEntity be)
                    be.onPowerChange(powered);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level,
                                                BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp)
            if (level.getBlockEntity(pos) instanceof GlowLampBlockEntity be)
                sp.openMenu(be, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GlowLampBlockEntity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) { return null; }
}

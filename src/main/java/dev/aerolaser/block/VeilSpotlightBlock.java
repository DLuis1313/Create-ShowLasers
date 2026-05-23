package dev.aerolaser.block;

import com.mojang.serialization.MapCodec;
import dev.aerolaser.blockentity.VeilSpotlightBlockEntity;
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

public class VeilSpotlightBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING  = BlockStateProperties.FACING;
    public static final BooleanProperty   POWERED = BlockStateProperties.POWERED;

    public static final MapCodec<VeilSpotlightBlock> CODEC = simpleCodec(VeilSpotlightBlock::new);

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    public VeilSpotlightBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.DOWN)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
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
                if (level.getBlockEntity(pos) instanceof VeilSpotlightBlockEntity be)
                    be.onPowerChange(powered);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level,
                                                BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp)
            if (level.getBlockEntity(pos) instanceof VeilSpotlightBlockEntity be)
                sp.openMenu(be, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VeilSpotlightBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> type) {
        return null; // sem tick necessário — a luz é gerenciada pelo renderer
    }
}

package net.shadowmage.ancientwarfarerecompiled.automation.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.torque.TileDistributor;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IHammerRotatable;

import javax.annotation.Nullable;

/**
 * Distributeur de couple (léger, moyen ou lourd selon le tier). Orienté sur les six faces, rotation au marteau.
 */
public class BlockTorqueDistributor extends Block implements EntityBlock, IHammerRotatable {
	public static final MapCodec<BlockTorqueDistributor> CODEC = simpleCodec(p -> new BlockTorqueDistributor(p, TorqueTier.LIGHT));

	private final TorqueTier tier;

	public BlockTorqueDistributor(Properties properties, TorqueTier tier) {
		super(properties);
		this.tier = tier;
		registerDefaultState(TorqueSides.withDefaults(stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.NORTH)));
	}

	public TorqueTier getTier() {
		return tier;
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.FACING);
		TorqueSides.addProperties(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(BlockStateProperties.FACING, context.getNearestLookingDirection());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileDistributor(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide || type != AWAutomationTileTypes.TORQUE_DISTRIBUTOR.get()) {
			return null;
		}
		return (lvl, pos, blockState, be) -> {
			if (be instanceof TileDistributor distributor) {
				distributor.serverTick();
			}
		};
	}

	@Override
	public boolean rotateBlock(Level level, BlockPos pos, Direction side) {
		BlockState state = level.getBlockState(pos);
		Direction facing = state.getValue(BlockStateProperties.FACING);
		Direction rotated = TorqueRotation.rotateAround(facing, side.getAxis());
		if (rotated == facing) {
			return false;
		}
		level.setBlock(pos, state.setValue(BlockStateProperties.FACING, rotated), Block.UPDATE_ALL);
		return true;
	}
}

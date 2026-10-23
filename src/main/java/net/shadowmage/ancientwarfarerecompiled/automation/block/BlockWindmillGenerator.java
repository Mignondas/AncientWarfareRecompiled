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
import net.shadowmage.ancientwarfarerecompiled.automation.tile.torque.TileWindmillController;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IHammerRotatable;

import javax.annotation.Nullable;

/**
 * Générateur éolien : sortie sur la face FACING, alimenté par le contrôleur de pales derrière lui.
 */
public class BlockWindmillGenerator extends Block implements EntityBlock, IHammerRotatable {
	public static final MapCodec<BlockWindmillGenerator> CODEC = simpleCodec(BlockWindmillGenerator::new);

	public BlockWindmillGenerator(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(BlockStateProperties.FACING, context.getNearestLookingDirection());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileWindmillController(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide || type != AWAutomationTileTypes.WINDMILL_CONTROLLER.get()) {
			return null;
		}
		return (lvl, pos, blockState, be) -> {
			if (be instanceof TileWindmillController controller) {
				controller.serverTick();
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

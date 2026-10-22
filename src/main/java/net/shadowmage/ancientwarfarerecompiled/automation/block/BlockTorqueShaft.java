package net.shadowmage.ancientwarfarerecompiled.automation.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.torque.TileTorqueShaft;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IHammerRotatable;
import net.shadowmage.ancientwarfarerecompiled.core.item.ItemHammer;

import javax.annotation.Nullable;

/**
 * Arbre de couple (léger, moyen ou lourd selon le tier). Orienté sur les six faces, rotation au marteau.
 */
public class BlockTorqueShaft extends Block implements EntityBlock, IHammerRotatable {
	public static final MapCodec<BlockTorqueShaft> CODEC = simpleCodec(p -> new BlockTorqueShaft(p, TorqueTier.LIGHT));

	private final TorqueTier tier;

	public BlockTorqueShaft(Properties properties, TorqueTier tier) {
		super(properties);
		this.tier = tier;
		registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.NORTH));
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
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(BlockStateProperties.FACING, context.getNearestLookingDirection());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileTorqueShaft(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide || type != AWAutomationTileTypes.TORQUE_SHAFT.get()) {
			return null;
		}
		return (lvl, pos, blockState, be) -> {
			if (be instanceof TileTorqueShaft shaft) {
				shaft.serverTick();
			}
		};
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (player.getMainHandItem().getItem() instanceof ItemHammer || player.getOffhandItem().getItem() instanceof ItemHammer) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		if (level.getBlockEntity(pos) instanceof TileTorqueShaft shaft && player instanceof ServerPlayer) {
			shaft.showValues(player);
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public boolean rotateBlock(Level level, BlockPos pos, Direction side) {
		BlockState state = level.getBlockState(pos);
		Direction facing = state.getValue(BlockStateProperties.FACING);
		Direction rotated = facing.getAxis() == side.getAxis() ? facing : facing.getClockwise(side.getAxis());
		if (rotated == facing) {
			return false;
		}
		level.setBlock(pos, state.setValue(BlockStateProperties.FACING, rotated), Block.UPDATE_ALL);
		return true;
	}
}

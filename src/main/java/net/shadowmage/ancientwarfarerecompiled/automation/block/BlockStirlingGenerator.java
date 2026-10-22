package net.shadowmage.ancientwarfarerecompiled.automation.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
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
import net.shadowmage.ancientwarfarerecompiled.automation.menu.StirlingGeneratorMenu;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.torque.TileStirlingGenerator;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IHammerRotatable;
import net.shadowmage.ancientwarfarerecompiled.core.item.ItemHammer;

import javax.annotation.Nullable;

/**
 * Générateur Stirling : ouvre son menu (combustible) au clic droit.
 */
public class BlockStirlingGenerator extends Block implements EntityBlock, IHammerRotatable {
	public static final MapCodec<BlockStirlingGenerator> CODEC = simpleCodec(BlockStirlingGenerator::new);

	public BlockStirlingGenerator(Properties properties) {
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
		return new TileStirlingGenerator(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide || type != AWAutomationTileTypes.STIRLING_GENERATOR.get()) {
			return null;
		}
		return (lvl, pos, blockState, be) -> {
			if (be instanceof TileStirlingGenerator stirling) {
				stirling.serverTick();
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
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) -> new StirlingGeneratorMenu(id, inv, pos),
					Component.translatable("block.ancientwarfarerecompiled.stirling_generator")), buf -> buf.writeBlockPos(pos));
		}
		return InteractionResult.CONSUME;
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TileStirlingGenerator stirling) {
			for (int i = 0; i < stirling.fuelHandler.getSlots(); i++) {
				Block.popResource(level, pos, stirling.fuelHandler.getStackInSlot(i));
			}
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
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

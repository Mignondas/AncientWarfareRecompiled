package net.shadowmage.ancientwarfarerecompiled.core.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IHammerRotatable;
import net.shadowmage.ancientwarfarerecompiled.core.item.ItemHammer;
import net.shadowmage.ancientwarfarerecompiled.core.menu.EngineeringStationMenu;
import net.shadowmage.ancientwarfarerecompiled.core.tile.TileEngineeringStation;

/**
 * Établi d'ingénierie. Tourne au marteau (rotation horizontale).
 */
public class BlockEngineeringStation extends Block implements EntityBlock, IHammerRotatable {
	public static final MapCodec<BlockEngineeringStation> CODEC = simpleCodec(BlockEngineeringStation::new);

	public BlockEngineeringStation(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, net.minecraft.core.Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.HORIZONTAL_FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEngineeringStation(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		// avec un marteau en main, laisse l'objet agir (rotation / mode travail)
		if (player.getMainHandItem().getItem() instanceof ItemHammer || player.getOffhandItem().getItem() instanceof ItemHammer) {
			return InteractionResult.PASS;
		}
		// L'interface graphique sera ouverte ici lors du portage de la GUI.
		if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
			EngineeringStationMenu.open(serverPlayer, pos);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (level.getBlockEntity(pos) instanceof TileEngineeringStation tile) {
				tile.dropContents(level, pos);
			}
			super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	@Override
	public boolean rotateBlock(Level level, BlockPos pos, net.minecraft.core.Direction side) {
		if (side.getAxis().isVertical()) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		level.setBlock(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()), Block.UPDATE_ALL);
		return true;
	}
}

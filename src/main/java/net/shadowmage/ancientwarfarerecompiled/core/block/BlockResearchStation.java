package net.shadowmage.ancientwarfarerecompiled.core.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreTileTypes;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IHammerRotatable;
import net.shadowmage.ancientwarfarerecompiled.core.item.ItemHammer;
import net.shadowmage.ancientwarfarerecompiled.core.menu.ResearchStationMenu;
import net.shadowmage.ancientwarfarerecompiled.core.tile.TileResearchStation;

/**
 * Station de recherche. Tourne au marteau. Le livre et la logique de recherche seront portés ensuite.
 */
public class BlockResearchStation extends Block implements EntityBlock, IHammerRotatable {
	public static final MapCodec<BlockResearchStation> CODEC = simpleCodec(BlockResearchStation::new);
	public static final BooleanProperty HAS_BOOK = BooleanProperty.create("has_book");

	public BlockResearchStation(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, net.minecraft.core.Direction.NORTH).setValue(HAS_BOOK, false));
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.HORIZONTAL_FACING, HAS_BOOK);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite()).setValue(HAS_BOOK, false);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileResearchStation(pos, state);
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
		// L'interface graphique de recherche sera ouverte ici lors du portage de la GUI.
		if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
			ResearchStationMenu.open(serverPlayer, pos);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide) {
			return null;
		}
		return type == AWCoreTileTypes.RESEARCH_STATION.get() ? (lvl, pos, st, tile) -> TileResearchStation.serverTick(lvl, pos, st, (TileResearchStation) tile) : null;
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (level.getBlockEntity(pos) instanceof TileResearchStation tile) {
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

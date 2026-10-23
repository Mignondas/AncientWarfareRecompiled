package net.shadowmage.ancientwarfarerecompiled.automation.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.TileWindmillBlade;

import javax.annotation.Nullable;

/**
 * Pale d'éolienne : le mât est un modèle de bloc, le rotor est dessiné par le renderer.
 */
public class BlockWindmillBlade extends Block implements EntityBlock {
	public static final MapCodec<BlockWindmillBlade> CODEC = simpleCodec(BlockWindmillBlade::new);

	public BlockWindmillBlade(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileWindmillBlade(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide || type != AWAutomationTileTypes.WINDMILL_BLADE.get()) {
			return null;
		}
		return (lvl, pos, blockState, be) -> {
			if (be instanceof TileWindmillBlade blade) {
				blade.serverTick();
			}
		};
	}
}

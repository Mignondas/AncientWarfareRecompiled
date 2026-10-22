package net.shadowmage.ancientwarfarerecompiled.automation.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Chargeur de parcelle simple : garde force chargés les 3x3 chunks autour de lui.
 * Le niveau sauvegarde lui-même les chunks forcés, donc aucun BlockEntity n'est nécessaire.
 */
public class BlockChunkLoaderSimple extends Block {
	public static final MapCodec<BlockChunkLoaderSimple> CODEC = simpleCodec(BlockChunkLoaderSimple::new);

	public BlockChunkLoaderSimple(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level instanceof ServerLevel serverLevel) {
			setForced(serverLevel, pos, true);
		}
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
			setForced(serverLevel, pos, false);
		}
		super.onRemove(state, level, pos, newState, moved);
	}

	private static void setForced(ServerLevel level, BlockPos pos, boolean forced) {
		int cx = pos.getX() >> 4;
		int cz = pos.getZ() >> 4;
		for (int x = cx - 1; x <= cx + 1; x++) {
			for (int z = cz - 1; z <= cz + 1; z++) {
				level.setChunkForced(x, z, forced);
			}
		}
	}
}

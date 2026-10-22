package net.shadowmage.ancientwarfarerecompiled.automation.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.TileChunkLoaderDeluxe;
import net.shadowmage.ancientwarfarerecompiled.core.item.ItemHammer;
import net.minecraft.network.chat.Component;
import net.shadowmage.ancientwarfarerecompiled.automation.menu.ChunkLoaderDeluxeMenu;

/**
 * Chargeur de parcelle deluxe : garde force chargé son propre chunk, plus les chunks cochés dans une grille 5x5.
 */
public class BlockChunkLoaderDeluxe extends Block implements EntityBlock {
	public static final MapCodec<BlockChunkLoaderDeluxe> CODEC = simpleCodec(BlockChunkLoaderDeluxe::new);

	public BlockChunkLoaderDeluxe(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileChunkLoaderDeluxe(pos, state);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
		}
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
			serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) -> new ChunkLoaderDeluxeMenu(id, inv, pos),
					Component.translatable("block.ancientwarfarerecompiled.chunk_loader_deluxe")), buf -> buf.writeBlockPos(pos));
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel
				&& level.getBlockEntity(pos) instanceof TileChunkLoaderDeluxe tile) {
			tile.releaseAll(serverLevel);
		}
		super.onRemove(state, level, pos, newState, moved);
	}
}

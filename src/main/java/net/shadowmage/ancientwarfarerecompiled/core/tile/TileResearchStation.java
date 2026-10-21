package net.shadowmage.ancientwarfarerecompiled.core.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfarerecompiled.core.block.BlockResearchStation;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreTileTypes;

/**
 * Entité de la station de recherche. Emplacement unique pour le livre de recherche.
 * La logique de recherche (énergie, progression, GUI) sera portée dans un lot suivant.
 */
public class TileResearchStation extends BlockEntity {
	public final ItemStackHandler bookSlot = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
			updateBookState();
		}
	};

	public TileResearchStation(BlockPos pos, BlockState state) {
		super(AWCoreTileTypes.RESEARCH_STATION.get(), pos, state);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.put("bookSlot", bookSlot.serializeNBT(registries));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		bookSlot.deserializeNBT(registries, tag.getCompound("bookSlot"));
	}

	@Override
	public void onLoad() {
		super.onLoad();
		updateBookState();
	}

	/**
	 * Affiche le livre sur le modèle lorsqu'un objet est présent dans l'emplacement.
	 */
	private void updateBookState() {
		if (level == null || level.isClientSide) {
			return;
		}
		BlockState state = getBlockState();
		boolean hasBook = !bookSlot.getStackInSlot(0).isEmpty();
		if (state.hasProperty(BlockResearchStation.HAS_BOOK) && state.getValue(BlockResearchStation.HAS_BOOK) != hasBook) {
			level.setBlock(worldPosition, state.setValue(BlockResearchStation.HAS_BOOK, hasBook), Block.UPDATE_ALL);
		}
	}

	public void dropContents(Level level, BlockPos pos) {
		Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), bookSlot.getStackInSlot(0));
	}
}

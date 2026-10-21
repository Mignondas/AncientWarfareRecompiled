package net.shadowmage.ancientwarfarerecompiled.core.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
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

	public void dropContents(Level level, BlockPos pos) {
		Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), bookSlot.getStackInSlot(0));
	}
}

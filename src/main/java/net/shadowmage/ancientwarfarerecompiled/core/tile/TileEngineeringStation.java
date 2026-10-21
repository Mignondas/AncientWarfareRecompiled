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
 * Entité de l'établi d'ingénierie : 18 emplacements d'inventoire supplémentaires.
 * L'interface graphique et les recettes seront portées avec le module Core.
 */
public class TileEngineeringStation extends BlockEntity {
	public static final int EXTRA_SLOTS = 18;

	public final ItemStackHandler extraSlots = new ItemStackHandler(EXTRA_SLOTS) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	};

	public TileEngineeringStation(BlockPos pos, BlockState state) {
		super(AWCoreTileTypes.ENGINEERING_STATION.get(), pos, state);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.put("extraInventory", extraSlots.serializeNBT(registries));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		extraSlots.deserializeNBT(registries, tag.getCompound("extraInventory"));
	}

	/**
	 * Vide l'inventaire dans le monde. Appelé lorsque le bloc est retiré.
	 */
	public void dropContents(Level level, BlockPos pos) {
		for (int slot = 0; slot < extraSlots.getSlots(); slot++) {
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), extraSlots.getStackInSlot(slot));
		}
	}
}

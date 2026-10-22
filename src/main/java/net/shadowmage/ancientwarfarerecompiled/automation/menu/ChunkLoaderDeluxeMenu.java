package net.shadowmage.ancientwarfarerecompiled.automation.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationMenus;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.TileChunkLoaderDeluxe;

/**
 * Menu du chargeur deluxe. Aucun emplacement : la grille est un bouton par case (clickMenuButton),
 * et l'état des cases est transmis par une donnée entière (bitmask).
 */
public class ChunkLoaderDeluxeMenu extends AbstractContainerMenu {
	private final BlockPos pos;
	private final DataSlot mask;

	public ChunkLoaderDeluxeMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
		this(id, inventory, buf.readBlockPos());
	}

	public ChunkLoaderDeluxeMenu(int id, Inventory inventory, BlockPos pos) {
		super(AWAutomationMenus.CHUNK_LOADER_DELUXE.get(), id);
		this.pos = pos;
		BlockEntity be = inventory.player.level().getBlockEntity(pos);
		ContainerData data;
		if (be instanceof TileChunkLoaderDeluxe tile && !inventory.player.level().isClientSide) {
			data = new ContainerData() {
				@Override
				public int get(int index) {
					return tile.getMask();
				}

				@Override
				public void set(int index, int value) {
				}

				@Override
				public int getCount() {
					return 1;
				}
			};
		} else {
			data = new SimpleContainerData(1);
		}
		this.mask = DataSlot.forContainer(data, 0);
		addDataSlot(mask);
	}

	/** Bitmask des cases cochées, pour l'écran. */
	public int getMask() {
		return mask.get();
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (player.level().isClientSide) {
			return false;
		}
		return player.level().getBlockEntity(pos) instanceof TileChunkLoaderDeluxe tile && tile.toggle(id);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return player.level().getBlockEntity(pos) instanceof TileChunkLoaderDeluxe
				&& player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
	}
}

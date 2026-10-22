package net.shadowmage.ancientwarfarerecompiled.automation.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationMenus;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.torque.TileStirlingGenerator;

/**
 * Menu du générateur Stirling : un emplacement de combustible et l'inventaire du joueur.
 */
public class StirlingGeneratorMenu extends AbstractContainerMenu {
	public static final int FUEL_SLOT = 0;
	public static final int PLAYER_START = 1;
	public static final int HOTBAR_END = 37;

	private final BlockPos pos;
	private final TileStirlingGenerator tile;

	public StirlingGeneratorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
		this(id, inventory, buf.readBlockPos());
	}

	public StirlingGeneratorMenu(int id, Inventory inventory, BlockPos pos) {
		super(AWAutomationMenus.STIRLING_GENERATOR.get(), id);
		this.pos = pos;
		this.tile = inventory.player.level().getBlockEntity(pos) instanceof TileStirlingGenerator t ? t : null;
		ItemStackHandler fuel = tile != null ? tile.fuelHandler : new ItemStackHandler(1);

		addSlot(new SlotItemHandler(fuel, 0, 80, 35));
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(inventory, col, 8 + col * 18, 142));
		}
	}

	public BlockPos getPos() {
		return pos;
	}

	public TileStirlingGenerator getTile() {
		return tile;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack copy = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return copy;
		}
		ItemStack stack = slot.getItem();
		copy = stack.copy();
		if (index == FUEL_SLOT) {
			if (!moveItemStackTo(stack, PLAYER_START, HOTBAR_END, true)) {
				return ItemStack.EMPTY;
			}
		} else if (!moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return copy;
	}

	@Override
	public boolean stillValid(Player player) {
		return tile != null && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
	}
}

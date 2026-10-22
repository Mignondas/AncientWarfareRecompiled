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
import net.shadowmage.ancientwarfarerecompiled.automation.tile.TileMailbox;

/**
 * Menu de la boîte aux lettres : 18 emplacements reçus, 18 à envoyer, et l'inventaire du joueur.
 */
public class MailboxMenu extends AbstractContainerMenu {
	public static final int RECEIVED_START = 0;
	public static final int SEND_START = 18;
	public static final int PLAYER_START = 36;
	public static final int HOTBAR_END = 63;
	public static final int RECEIVED_Y = 88;
	public static final int SEND_Y = 138;
	public static final int PLAYER_Y = 178;
	public static final int HOTBAR_Y = 236;

	private final BlockPos pos;
	private final TileMailbox tile;

	public MailboxMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
		this(id, inventory, buf.readBlockPos());
	}

	public MailboxMenu(int id, Inventory inventory, BlockPos pos) {
		super(AWAutomationMenus.MAILBOX.get(), id);
		this.pos = pos;
		this.tile = inventory.player.level().getBlockEntity(pos) instanceof TileMailbox t ? t : null;
		ItemStackHandler received = tile != null ? tile.receivedInventory : new ItemStackHandler(18);
		ItemStackHandler send = tile != null ? tile.sendInventory : new ItemStackHandler(18);

		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new SlotItemHandler(received, row * 9 + col, 8 + col * 18, RECEIVED_Y + row * 18));
			}
		}
		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new SlotItemHandler(send, row * 9 + col, 8 + col * 18, SEND_Y + row * 18));
			}
		}
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, PLAYER_Y + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(inventory, col, 8 + col * 18, HOTBAR_Y));
		}
	}

	public BlockPos getPos() {
		return pos;
	}

	public TileMailbox getTile() {
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
		if (index < PLAYER_START) {
			if (!moveItemStackTo(stack, PLAYER_START, HOTBAR_END + 1, true)) {
				return ItemStack.EMPTY;
			}
		} else if (!moveItemStackTo(stack, SEND_START, PLAYER_START, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		if (stack.getCount() == copy.getCount()) {
			return ItemStack.EMPTY;
		}
		slot.onTake(player, stack);
		return copy;
	}

	@Override
	public boolean stillValid(Player player) {
		return player.level().getBlockEntity(pos) instanceof TileMailbox
				&& player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
	}
}

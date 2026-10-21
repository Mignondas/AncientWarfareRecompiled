package net.shadowmage.ancientwarfarerecompiled.core.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreBlocks;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreMenus;
import net.shadowmage.ancientwarfarerecompiled.core.tile.TileResearchStation;

/**
 * Menu de la station de recherche : emplacement du livre et inventaire du joueur.
 * Le filtre sur le livre de recherche sera ajouté avec l'objet « research_book ».
 */
public class ResearchStationMenu extends AbstractContainerMenu {
	private static final int BOOK_SLOTS = 1;

	private final BlockPos pos;
	private final ContainerLevelAccess access;

	public ResearchStationMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
		this(id, inventory, buf.readBlockPos());
	}

	public ResearchStationMenu(int id, Inventory inventory, BlockPos pos) {
		super(AWCoreMenus.RESEARCH_STATION.get(), id);
		this.pos = pos;
		this.access = ContainerLevelAccess.create(inventory.player.level(), pos);

		IItemHandler book = inventory.player.level().getBlockEntity(pos) instanceof TileResearchStation tile
				? tile.bookSlot
				: new ItemStackHandler(BOOK_SLOTS);
		addSlot(new SlotItemHandler(book, 0, 80, 18));

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

	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, AWCoreBlocks.RESEARCH_STATION.get());
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			result = stack.copy();
			if (index < BOOK_SLOTS) {
				if (!moveItemStackTo(stack, BOOK_SLOTS, slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!moveItemStackTo(stack, 0, BOOK_SLOTS, false)) {
				return ItemStack.EMPTY;
			}
			if (stack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			if (stack.getCount() == result.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTake(player, stack);
		}
		return result;
	}

	public static void open(ServerPlayer player, BlockPos pos) {
		BlockEntity be = player.level().getBlockEntity(pos);
		if (be instanceof TileResearchStation) {
			player.openMenu(new net.minecraft.world.SimpleMenuProvider(
					(id, inv, p) -> new ResearchStationMenu(id, inv, pos),
					net.minecraft.network.chat.Component.translatable("block.ancientwarfarerecompiled.research_station")),
					buf -> buf.writeBlockPos(pos));
		}
	}
}

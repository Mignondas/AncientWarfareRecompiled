package net.shadowmage.ancientwarfarerecompiled.core.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
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
 * Menu de la station de recherche : livre (1), ressources de recherche (9) et inventaire du joueur.
 */
public class ResearchStationMenu extends AbstractContainerMenu {
	private static final int BOOK_SLOTS = 1;
	private static final int RESOURCE_SLOTS = 9;
	private static final int STATION_SLOTS = BOOK_SLOTS + RESOURCE_SLOTS;

	private final BlockPos pos;
	private final ContainerLevelAccess access;

	public ResearchStationMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
		this(id, inventory, buf.readBlockPos());
	}

	public ResearchStationMenu(int id, Inventory inventory, BlockPos pos) {
		super(AWCoreMenus.RESEARCH_STATION.get(), id);
		this.pos = pos;
		this.access = ContainerLevelAccess.create(inventory.player.level(), pos);

		TileResearchStation tile = inventory.player.level().getBlockEntity(pos) instanceof TileResearchStation t ? t : null;
		IItemHandler book = tile != null ? tile.bookSlot : new ItemStackHandler(BOOK_SLOTS);
		IItemHandler resources = tile != null ? tile.resourceInventory : new ItemStackHandler(RESOURCE_SLOTS);

		addSlot(new SlotItemHandler(book, 0, 8, 18));
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				addSlot(new SlotItemHandler(resources, col + row * 3, 80 + col * 18, 18 + row * 18));
			}
		}

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
			if (index < STATION_SLOTS) {
				if (!moveItemStackTo(stack, STATION_SLOTS, slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!moveItemStackTo(stack, 0, BOOK_SLOTS, false) && !moveItemStackTo(stack, BOOK_SLOTS, STATION_SLOTS, false)) {
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
			player.openMenu(new SimpleMenuProvider(
					(id, inv, p) -> new ResearchStationMenu(id, inv, pos),
					Component.translatable("block.ancientwarfarerecompiled.research_station")),
					buf -> buf.writeBlockPos(pos));
		}
	}
}

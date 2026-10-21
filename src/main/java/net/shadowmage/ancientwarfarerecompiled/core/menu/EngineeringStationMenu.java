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
import net.shadowmage.ancientwarfarerecompiled.core.tile.TileEngineeringStation;

/**
 * Menu de l'établi d'ingénierie : 18 emplacements supplémentaires et l'inventaire du joueur.
 */
public class EngineeringStationMenu extends AbstractContainerMenu {
	private static final int EXTRA_START = 0;
	private static final int EXTRA_END = TileEngineeringStation.EXTRA_SLOTS;

	private final BlockPos pos;
	private final ContainerLevelAccess access;

	public EngineeringStationMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
		this(id, inventory, buf.readBlockPos());
	}

	public EngineeringStationMenu(int id, Inventory inventory, BlockPos pos) {
		super(AWCoreMenus.ENGINEERING_STATION.get(), id);
		this.pos = pos;
		this.access = ContainerLevelAccess.create(inventory.player.level(), pos);

		IItemHandler extra = inventory.player.level().getBlockEntity(pos) instanceof TileEngineeringStation tile
				? tile.extraSlots
				: new ItemStackHandler(TileEngineeringStation.EXTRA_SLOTS);

		for (int i = 0; i < TileEngineeringStation.EXTRA_SLOTS; i++) {
			addSlot(new SlotItemHandler(extra, i, 8 + (i % 9) * 18, 18 + (i / 9) * 18));
		}
		addPlayerSlots(inventory);
	}

	private void addPlayerSlots(Inventory inventory) {
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 110 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(inventory, col, 8 + col * 18, 168));
		}
	}

	public BlockPos getPos() {
		return pos;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, AWCoreBlocks.ENGINEERING_STATION.get());
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			result = stack.copy();
			if (index < EXTRA_END) {
				if (!moveItemStackTo(stack, EXTRA_END, slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!moveItemStackTo(stack, EXTRA_START, EXTRA_END, false)) {
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

	/**
	 * Ouvre le menu côté serveur pour le joueur.
	 */
	public static void open(ServerPlayer player, BlockPos pos) {
		BlockEntity be = player.level().getBlockEntity(pos);
		if (be instanceof TileEngineeringStation) {
			player.openMenu(new net.minecraft.world.SimpleMenuProvider(
					(id, inv, p) -> new EngineeringStationMenu(id, inv, pos),
					net.minecraft.network.chat.Component.translatable("block.ancientwarfarerecompiled.engineering_station")),
					buf -> buf.writeBlockPos(pos));
		}
	}
}

package net.shadowmage.ancientwarfarerecompiled.core.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreAttachments;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreBlocks;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreMenus;
import net.shadowmage.ancientwarfarerecompiled.core.research.ResearchRecipes;
import net.shadowmage.ancientwarfarerecompiled.core.tile.TileEngineeringStation;

import java.util.Optional;

/**
 * Menu de l'établi d'ingénierie : grille de craft 3x3, résultat, 18 emplacements supplémentaires et inventaire.
 * Les recettes marquées d'une recherche ne sortent que si le joueur a terminé cette recherche.
 */
public class EngineeringStationMenu extends AbstractContainerMenu {
	private static final int GRID_END = 9;
	private static final int RESULT_SLOT = 9;
	private static final int EXTRA_START = 10;
	private static final int EXTRA_END = EXTRA_START + TileEngineeringStation.EXTRA_SLOTS;
	private static final int PLAYER_START = EXTRA_END;

	private final BlockPos pos;
	private final ContainerLevelAccess access;
	private final Player player;
	private final Level level;
	private final TransientCraftingContainer grid;
	private final ResultContainer result = new ResultContainer();

	public EngineeringStationMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
		this(id, inventory, buf.readBlockPos());
	}

	public EngineeringStationMenu(int id, Inventory inventory, BlockPos pos) {
		super(AWCoreMenus.ENGINEERING_STATION.get(), id);
		this.pos = pos;
		this.player = inventory.player;
		this.level = player.level();
		this.access = ContainerLevelAccess.create(level, pos);
		this.grid = new TransientCraftingContainer(this, 3, 3);

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				addSlot(new Slot(grid, col + row * 3, 30 + col * 18, 18 + row * 18));
			}
		}
		addSlot(new ResultSlot(result, 0, 124, 36));

		IItemHandler extra = level.getBlockEntity(pos) instanceof TileEngineeringStation tile
				? tile.extraSlots
				: new ItemStackHandler(TileEngineeringStation.EXTRA_SLOTS);
		for (int i = 0; i < TileEngineeringStation.EXTRA_SLOTS; i++) {
			addSlot(new SlotItemHandler(extra, i, 8 + (i % 9) * 18, 80 + (i / 9) * 18));
		}

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 134 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(inventory, col, 8 + col * 18, 192));
		}
	}

	public BlockPos getPos() {
		return pos;
	}

	@Override
	public void slotsChanged(Container container) {
		super.slotsChanged(container);
		if (container == grid) {
			updateResult();
		}
	}

	private void updateResult() {
		if (level.isClientSide) {
			return;
		}
		CraftingInput input = CraftingInput.of(3, 3, grid.getItems());
		ItemStack out = ItemStack.EMPTY;
		Optional<RecipeHolder<CraftingRecipe>> holder = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
		if (holder.isPresent()) {
			Optional<String> needed = ResearchRecipes.getRequiredResearch(holder.get().id());
			if (needed.isEmpty() || hasResearched(needed.get())) {
				out = holder.get().value().assemble(input, level.registryAccess());
			}
		}
		result.setItem(0, out);
	}

	private boolean hasResearched(String research) {
		return player.getData(AWCoreAttachments.PLAYER_RESEARCH.get()).isCompleted(research);
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, AWCoreBlocks.ENGINEERING_STATION.get());
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
		if (index <= RESULT_SLOT || index < EXTRA_END) {
			if (!moveItemStackTo(stack, PLAYER_START, slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (!moveItemStackTo(stack, EXTRA_START, EXTRA_END, false) && !moveItemStackTo(stack, 0, GRID_END, false)) {
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
	public void removed(Player player) {
		super.removed(player);
		clearContainer(player, grid);
	}

	/*
	 * Emplacement de résultat : on ne peut pas y poser d'objet, et le retrait consomme la grille.
	 */
	private class ResultSlot extends Slot {
		ResultSlot(Container container, int index, int x, int y) {
			super(container, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public void onTake(Player player, ItemStack stack) {
			for (int i = 0; i < grid.getContainerSize(); i++) {
				grid.removeItem(i, 1);
			}
			super.onTake(player, stack);
		}
	}

	public static void open(ServerPlayer player, BlockPos pos) {
		BlockEntity be = player.level().getBlockEntity(pos);
		if (be instanceof TileEngineeringStation) {
			player.openMenu(new SimpleMenuProvider(
					(id, inv, p) -> new EngineeringStationMenu(id, inv, pos),
					Component.translatable("block.ancientwarfarerecompiled.engineering_station")),
					buf -> buf.writeBlockPos(pos));
		}
	}
}

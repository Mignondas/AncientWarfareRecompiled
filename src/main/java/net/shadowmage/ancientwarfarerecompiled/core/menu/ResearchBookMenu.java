package net.shadowmage.ancientwarfarerecompiled.core.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreMenus;

/** Menu du livre de recherche : aucun emplacement, il sert seulement à ouvrir l'écran de recherche. */
public class ResearchBookMenu extends AbstractContainerMenu {
	public ResearchBookMenu(int id, Inventory inventory) {
		super(AWCoreMenus.RESEARCH_BOOK.get(), id);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}
}

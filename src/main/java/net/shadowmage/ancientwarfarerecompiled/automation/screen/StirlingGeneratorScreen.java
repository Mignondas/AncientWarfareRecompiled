package net.shadowmage.ancientwarfarerecompiled.automation.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.shadowmage.ancientwarfarerecompiled.automation.menu.StirlingGeneratorMenu;
import net.shadowmage.ancientwarfarerecompiled.core.screen.StationScreens;

/**
 * Écran du générateur Stirling : emplacement de combustible et inventaire du joueur.
 */
public class StirlingGeneratorScreen extends AbstractContainerScreen<StirlingGeneratorMenu> {

	public StirlingGeneratorScreen(StirlingGeneratorMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int l = leftPos;
		int t = topPos;
		graphics.fill(l - 1, t - 1, l + imageWidth + 1, t + imageHeight + 1, 0xFF000000);
		graphics.fill(l, t, l + imageWidth, t + imageHeight, 0xFF8B8B8B);
		StationScreens.drawSlotBackgrounds(graphics, menu, l, t);
	}
}

package net.shadowmage.ancientwarfarerecompiled.automation.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.shadowmage.ancientwarfarerecompiled.automation.menu.ChunkLoaderDeluxeMenu;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.TileChunkLoaderDeluxe;

/**
 * Grille 5x5 des chunks autour du chargeur deluxe. Un clic sur une case envoie son index au serveur.
 */
public class ChunkLoaderDeluxeScreen extends AbstractContainerScreen<ChunkLoaderDeluxeMenu> {
	private static final int CELL = 12;
	private static final int GRID_X = 8;
	private static final int GRID_Y = 8;

	public ChunkLoaderDeluxeScreen(ChunkLoaderDeluxeMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 76;
		this.imageHeight = 76;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int l = leftPos;
		int t = topPos;
		graphics.fill(l - 1, t - 1, l + imageWidth + 1, t + imageHeight + 1, 0xFF000000);
		graphics.fill(l, t, l + imageWidth, t + imageHeight, 0xFF2B2B2B);
		int mask = menu.getMask();
		for (int i = 0; i < TileChunkLoaderDeluxe.GRID * TileChunkLoaderDeluxe.GRID; i++) {
			int x = l + GRID_X + (i % TileChunkLoaderDeluxe.GRID) * CELL;
			int y = t + GRID_Y + (i / TileChunkLoaderDeluxe.GRID) * CELL;
			boolean forced = (mask & (1 << i)) != 0;
			int color;
			if (i == TileChunkLoaderDeluxe.CENTER) {
				color = 0xFFD4A017;
			} else if (forced) {
				color = 0xFF3CB043;
			} else {
				color = 0xFF555555;
			}
			graphics.fill(x, y, x + CELL - 1, y + CELL - 1, color);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		// Pas d'étiquettes d'inventaire : la grille occupe tout le panneau.
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			double dx = mouseX - leftPos - GRID_X;
			double dy = mouseY - topPos - GRID_Y;
			if (dx >= 0 && dy >= 0) {
				int col = (int) (dx / CELL);
				int row = (int) (dy / CELL);
				int index = row * TileChunkLoaderDeluxe.GRID + col;
				if (col < TileChunkLoaderDeluxe.GRID && row < TileChunkLoaderDeluxe.GRID && index != TileChunkLoaderDeluxe.CENTER) {
					minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
	}
}

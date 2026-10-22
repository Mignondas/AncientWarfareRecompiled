package net.shadowmage.ancientwarfarerecompiled.core.research.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.shadowmage.ancientwarfarerecompiled.core.menu.ResearchBookMenu;
import net.shadowmage.ancientwarfarerecompiled.core.network.PacketResearchAction;
import net.shadowmage.ancientwarfarerecompiled.core.research.ResearchRegistry;

import java.util.List;

/**
 * Livre de recherche, sur le même chemin de rendu que les stations (AbstractContainerScreen).
 * Liste des objectifs disponibles à gauche, file de recherche à droite.
 */
public class ResearchBookScreen extends AbstractContainerScreen<ResearchBookMenu> {
	private static final int PAGE = 8;
	private static final int ROW = 16;
	private static final int PANEL_W = 230;
	private static final int PANEL_H = 200;

	private int availPage;
	private int queuePage;
	private int builtVersion = -1;

	public ResearchBookScreen(ResearchBookMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = PANEL_W;
		this.imageHeight = PANEL_H;
	}

	@Override
	protected void init() {
		super.init();
		rebuild();
	}

	@Override
	protected void containerTick() {
		if (builtVersion != ClientResearchData.version()) {
			rebuild();
		}
	}

	private void rebuild() {
		clearWidgets();
		builtVersion = ClientResearchData.version();
		List<String> available = ClientResearchData.available();
		List<String> queue = ClientResearchData.queue();
		availPage = clampPage(availPage, available.size());
		queuePage = clampPage(queuePage, queue.size());

		for (int i = 0; i < PAGE; i++) {
			int index = availPage * PAGE + i;
			if (index >= available.size()) {
				break;
			}
			String name = available.get(index);
			addRenderableWidget(Button.builder(Component.literal("+"), b -> send(true, name))
					.bounds(leftPos + 90, rowY(i), 14, 14).build());
		}
		for (int i = 0; i < PAGE; i++) {
			int index = queuePage * PAGE + i;
			if (index >= queue.size()) {
				break;
			}
			String name = queue.get(index);
			addRenderableWidget(Button.builder(Component.literal("-"), b -> send(false, name))
					.bounds(leftPos + 212, rowY(i), 14, 14).build());
		}

		Button availPrev = addRenderableWidget(Button.builder(Component.literal("<"), b -> {
			availPage--;
			rebuild();
		}).bounds(leftPos + 8, pagerY(), 16, 14).build());
		availPrev.active = availPage > 0;
		Button availNext = addRenderableWidget(Button.builder(Component.literal(">"), b -> {
			availPage++;
			rebuild();
		}).bounds(leftPos + 70, pagerY(), 16, 14).build());
		availNext.active = availPage < pageCount(available.size()) - 1;

		Button queuePrev = addRenderableWidget(Button.builder(Component.literal("<"), b -> {
			queuePage--;
			rebuild();
		}).bounds(leftPos + 118, pagerY(), 16, 14).build());
		queuePrev.active = queuePage > 0;
		Button queueNext = addRenderableWidget(Button.builder(Component.literal(">"), b -> {
			queuePage++;
			rebuild();
		}).bounds(leftPos + 180, pagerY(), 16, 14).build());
		queueNext.active = queuePage < pageCount(queue.size()) - 1;
	}

	private static void send(boolean enqueue, String goal) {
		PacketDistributor.sendToServer(new PacketResearchAction(enqueue, goal));
	}

	private int rowY(int row) {
		return topPos + 34 + row * ROW;
	}

	private int pagerY() {
		return topPos + 34 + PAGE * ROW + 4;
	}

	private static int pageCount(int size) {
		return Math.max(1, (size + PAGE - 1) / PAGE);
	}

	private static int clampPage(int page, int size) {
		return Math.max(0, Math.min(page, pageCount(size) - 1));
	}

	private static Component goalText(String name) {
		String key = "research." + name;
		return I18n.exists(key) ? Component.translatable(key) : Component.literal(name);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int l = leftPos;
		int t = topPos;
		graphics.fill(l - 1, t - 1, l + PANEL_W + 1, t + PANEL_H + 1, 0xFF000000);
		graphics.fill(l, t, l + PANEL_W, t + PANEL_H, 0xFF2B2B2B);
		graphics.fill(l + 113, t + 20, l + 114, t + PANEL_H - 24, 0xFF555555);

		graphics.drawCenteredString(font, title, width / 2, t + 8, 0xFFFFFF);
		graphics.drawString(font, Component.translatable("guistrings.research.available").getString(), l + 8, t + 20, 0xAAAAAA, false);
		graphics.drawString(font, Component.translatable("guistrings.research.queue").getString(), l + 118, t + 20, 0xAAAAAA, false);

		List<String> available = ClientResearchData.available();
		List<String> queue = ClientResearchData.queue();
		for (int i = 0; i < PAGE; i++) {
			int index = availPage * PAGE + i;
			if (index < available.size()) {
				graphics.drawString(font, font.plainSubstrByWidth(goalText(available.get(index)).getString(), 76), l + 8, rowY(i) + 3, 0xFFFFFF, false);
			}
			index = queuePage * PAGE + i;
			if (index < queue.size()) {
				graphics.drawString(font, font.plainSubstrByWidth(goalText(queue.get(index)).getString(), 88), l + 118, rowY(i) + 3, 0xFFFFFF, false);
			}
		}

		graphics.drawString(font, (availPage + 1) + " / " + pageCount(available.size()), l + 30, pagerY() + 3, 0xAAAAAA, false);
		graphics.drawString(font, (queuePage + 1) + " / " + pageCount(queue.size()), l + 140, pagerY() + 3, 0xAAAAAA, false);

		String current = ClientResearchData.current();
		String currentLine;
		if (current == null) {
			currentLine = Component.translatable("guistrings.research.current").getString() + ": " + Component.translatable("guistrings.research.none").getString();
		} else {
			var goal = ResearchRegistry.getResearch(current);
			int total = goal == null ? 0 : goal.getTotalResearchTime();
			currentLine = Component.translatable("guistrings.research.current").getString() + ": "
					+ goalText(current).getString() + " " + Math.max(0, ClientResearchData.progress()) + " / " + total;
		}
		graphics.drawString(font, font.plainSubstrByWidth(currentLine, PANEL_W - 16), l + 8, t + PANEL_H - 16, 0xFFFFFF, false);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		// Les textes sont dessinés dans renderBg ; pas d'étiquettes d'inventaire vanilla.
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
	}
}

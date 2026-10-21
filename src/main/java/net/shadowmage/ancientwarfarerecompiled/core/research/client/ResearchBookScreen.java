package net.shadowmage.ancientwarfarerecompiled.core.research.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.shadowmage.ancientwarfarerecompiled.core.network.PacketResearchAction;
import net.shadowmage.ancientwarfarerecompiled.core.research.ResearchRegistry;

import java.util.List;

/**
 * Livre de recherche : liste des objectifs disponibles à gauche, file de recherche à droite.
 * Chaque clic envoie une action au serveur, qui valide et renvoie l'état mis à jour.
 */
public class ResearchBookScreen extends Screen {
	private static final int PAGE = 8;
	private static final int ROW = 16;
	private static final int PANEL_W = 230;
	private static final int PANEL_H = 200;

	private int left;
	private int top;
	private int availPage;
	private int queuePage;
	private int builtVersion = -1;

	public ResearchBookScreen() {
		super(Component.translatable("guistrings.research.book_title"));
	}

	public static void open() {
		Minecraft.getInstance().setScreen(new ResearchBookScreen());
	}

	@Override
	protected void init() {
		left = (width - PANEL_W) / 2;
		top = (height - PANEL_H) / 2;
		rebuild();
	}

	@Override
	public void tick() {
		if (builtVersion != ClientResearchData.version()) {
			rebuild();
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
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
					.bounds(left + 90, rowY(i), 14, 14).build());
		}
		for (int i = 0; i < PAGE; i++) {
			int index = queuePage * PAGE + i;
			if (index >= queue.size()) {
				break;
			}
			String name = queue.get(index);
			addRenderableWidget(Button.builder(Component.literal("-"), b -> send(false, name))
					.bounds(left + 212, rowY(i), 14, 14).build());
		}

		Button availPrev = addRenderableWidget(Button.builder(Component.literal("<"), b -> {
			availPage--;
			rebuild();
		}).bounds(left + 8, pagerY(), 16, 14).build());
		availPrev.active = availPage > 0;
		Button availNext = addRenderableWidget(Button.builder(Component.literal(">"), b -> {
			availPage++;
			rebuild();
		}).bounds(left + 70, pagerY(), 16, 14).build());
		availNext.active = availPage < pageCount(available.size()) - 1;

		Button queuePrev = addRenderableWidget(Button.builder(Component.literal("<"), b -> {
			queuePage--;
			rebuild();
		}).bounds(left + 118, pagerY(), 16, 14).build());
		queuePrev.active = queuePage > 0;
		Button queueNext = addRenderableWidget(Button.builder(Component.literal(">"), b -> {
			queuePage++;
			rebuild();
		}).bounds(left + 180, pagerY(), 16, 14).build());
		queueNext.active = queuePage < pageCount(queue.size()) - 1;
	}

	private static void send(boolean enqueue, String goal) {
		PacketDistributor.sendToServer(new PacketResearchAction(enqueue, goal));
	}

	private int rowY(int row) {
		return top + 34 + row * ROW;
	}

	private int pagerY() {
		return top + 34 + PAGE * ROW + 4;
	}

	private static int pageCount(int size) {
		return Math.max(1, (size + PAGE - 1) / PAGE);
	}

	private static int clampPage(int page, int size) {
		return Math.max(0, Math.min(page, pageCount(size) - 1));
	}

	private static Component goalText(String name) {
		String key = "research.ancientwarfarerecompiled." + name;
		return I18n.exists(key) ? Component.translatable(key) : Component.literal(name);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderTransparentBackground(graphics);
		graphics.fill(left - 1, top - 1, left + PANEL_W + 1, top + PANEL_H + 1, 0xFF000000);
		graphics.fill(left, top, left + PANEL_W, top + PANEL_H, 0xFF2B2B2B);
		graphics.fill(left + 113, top + 20, left + 114, top + PANEL_H - 24, 0xFF555555);

		graphics.drawCenteredString(font, title, width / 2, top + 8, 0xFFFFFF);
		graphics.drawString(font, Component.translatable("guistrings.research.available").getString(), left + 8, top + 20, 0xAAAAAA, false);
		graphics.drawString(font, Component.translatable("guistrings.research.queue").getString(), left + 118, top + 20, 0xAAAAAA, false);

		List<String> available = ClientResearchData.available();
		List<String> queue = ClientResearchData.queue();
		for (int i = 0; i < PAGE; i++) {
			int index = availPage * PAGE + i;
			if (index < available.size()) {
				graphics.drawString(font, font.plainSubstrByWidth(goalText(available.get(index)).getString(), 76), left + 8, rowY(i) + 3, 0xFFFFFF, false);
			}
			index = queuePage * PAGE + i;
			if (index < queue.size()) {
				graphics.drawString(font, font.plainSubstrByWidth(goalText(queue.get(index)).getString(), 88), left + 118, rowY(i) + 3, 0xFFFFFF, false);
			}
		}

		String pageAvail = (availPage + 1) + " / " + pageCount(available.size());
		String pageQueue = (queuePage + 1) + " / " + pageCount(queue.size());
		graphics.drawString(font, pageAvail, left + 30, pagerY() + 3, 0xAAAAAA, false);
		graphics.drawString(font, pageQueue, left + 140, pagerY() + 3, 0xAAAAAA, false);

		String current = ClientResearchData.current();
		String currentLine;
		if (current == null) {
			currentLine = Component.translatable("guistrings.research.current").getString() + ": " + Component.translatable("guistrings.research.none").getString();
		} else {
			int total = ResearchRegistry.getResearch(current) == null ? 0 : ResearchRegistry.getResearch(current).getTotalResearchTime();
			currentLine = Component.translatable("guistrings.research.current").getString() + ": "
					+ goalText(current).getString() + " " + Math.max(0, ClientResearchData.progress()) + " / " + total;
		}
		graphics.drawString(font, font.plainSubstrByWidth(currentLine, PANEL_W - 16), left + 8, top + PANEL_H - 16, 0xFFFFFF, false);

		super.render(graphics, mouseX, mouseY, partialTick);
	}
}

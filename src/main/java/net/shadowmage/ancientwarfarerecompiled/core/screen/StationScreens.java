package net.shadowmage.ancientwarfarerecompiled.core.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreMenus;
import net.shadowmage.ancientwarfarerecompiled.core.menu.EngineeringStationMenu;
import net.shadowmage.ancientwarfarerecompiled.core.menu.ResearchStationMenu;

/**
 * Écrans des stations (côté client). Le fond est le panneau générique du mod d'origine.
 */
@EventBusSubscriber(modid = AncientWarfareRecompiled.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class StationScreens {
	private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(AncientWarfareRecompiled.MODID, "textures/gui/background_large.png");

	private StationScreens() {}

	/*
	 * Fond de chaque emplacement (sprite vanilla « container/slot »), dessiné sous les objets.
	 */
	static void drawSlotBackgrounds(GuiGraphics graphics, AbstractContainerMenu menu, int leftPos, int topPos) {
		for (Slot slot : menu.slots) {
			graphics.blitSprite(ResourceLocation.withDefaultNamespace("container/slot"), leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18);
		}
	}

	@SubscribeEvent
	public static void register(RegisterMenuScreensEvent event) {
		event.register(AWCoreMenus.ENGINEERING_STATION.get(), EngineeringStationScreen::new);
		event.register(AWCoreMenus.RESEARCH_STATION.get(), ResearchStationScreen::new);
	}

	public static class EngineeringStationScreen extends AbstractContainerScreen<EngineeringStationMenu> {
		public EngineeringStationScreen(EngineeringStationMenu menu, Inventory inventory, Component title) {
			super(menu, inventory, title);
			this.imageWidth = 176;
			this.imageHeight = 218;
		}

		@Override
		protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
			graphics.blit(BACKGROUND, leftPos, topPos, imageWidth, imageHeight, 0, 0, 256, 240, 256, 256);
			drawSlotBackgrounds(graphics, menu, leftPos, topPos);
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			renderBackground(graphics, mouseX, mouseY, partialTick);
			super.render(graphics, mouseX, mouseY, partialTick);
			renderTooltip(graphics, mouseX, mouseY);
		}
	}

	public static class ResearchStationScreen extends AbstractContainerScreen<ResearchStationMenu> {
		public ResearchStationScreen(ResearchStationMenu menu, Inventory inventory, Component title) {
			super(menu, inventory, title);
			this.imageWidth = 176;
			this.imageHeight = 166;
		}

		@Override
		protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
			graphics.blit(BACKGROUND, leftPos, topPos, imageWidth, imageHeight, 0, 0, 256, 240, 256, 256);
			drawSlotBackgrounds(graphics, menu, leftPos, topPos);
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			renderBackground(graphics, mouseX, mouseY, partialTick);
			super.render(graphics, mouseX, mouseY, partialTick);
			renderTooltip(graphics, mouseX, mouseY);
		}
	}
}

package net.shadowmage.ancientwarfarerecompiled.automation.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.shadowmage.ancientwarfarerecompiled.automation.menu.MailboxMenu;
import net.shadowmage.ancientwarfarerecompiled.automation.network.PacketMailboxSettings;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.TileMailbox;
import net.shadowmage.ancientwarfarerecompiled.core.screen.StationScreens;

/**
 * Écran de la boîte aux lettres : nom de la boîte, destination, privée ou non, et les deux inventaires.
 */
public class MailboxScreen extends AbstractContainerScreen<MailboxMenu> {
	private EditBox nameBox;
	private EditBox targetBox;
	private boolean priv;

	public MailboxScreen(MailboxMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = 254;
	}

	@Override
	protected void init() {
		super.init();
		TileMailbox tile = menu.getTile();
		nameBox = addRenderableWidget(new EditBox(font, leftPos + 8, topPos + 16, 160, 12, Component.empty()));
		nameBox.setMaxLength(32);
		nameBox.setValue(tile != null && tile.getMailboxName() != null ? tile.getMailboxName() : "");
		targetBox = addRenderableWidget(new EditBox(font, leftPos + 8, topPos + 42, 160, 12, Component.empty()));
		targetBox.setMaxLength(32);
		targetBox.setValue(tile != null && tile.getTargetName() != null ? tile.getTargetName() : "");
		priv = tile != null && tile.isPrivateBox();
		addRenderableWidget(Button.builder(privateLabel(), b -> {
			priv = !priv;
			b.setMessage(privateLabel());
		}).bounds(leftPos + 8, topPos + 60, 100, 14).build());
		addRenderableWidget(Button.builder(Component.translatable("guistrings.automation.mailbox_save"), b -> save())
				.bounds(leftPos + 112, topPos + 60, 56, 14).build());
	}

	private Component privateLabel() {
		return Component.translatable(priv ? "guistrings.automation.mailbox_private_on" : "guistrings.automation.mailbox_private_off");
	}

	private void save() {
		PacketDistributor.sendToServer(new PacketMailboxSettings(menu.getPos(), nameBox.getValue(), targetBox.getValue(), priv));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int l = leftPos;
		int t = topPos;
		graphics.fill(l - 1, t - 1, l + imageWidth + 1, t + imageHeight + 1, 0xFF000000);
		graphics.fill(l, t, l + imageWidth, t + imageHeight, 0xFF2B2B2B);
		StationScreens.drawSlotBackgrounds(graphics, menu, l, t);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, Component.translatable("guistrings.automation.mailbox_box").getString(), 8, 6, 0xFFFFFF, false);
		graphics.drawString(font, Component.translatable("guistrings.automation.mailbox_destination").getString(), 8, 32, 0xFFFFFF, false);
		graphics.drawString(font, Component.translatable("guistrings.automation.mailbox_input").getString(), 8, MailboxMenu.RECEIVED_Y - 10, 0xAAAAAA, false);
		graphics.drawString(font, Component.translatable("guistrings.automation.mailbox_output").getString(), 8, MailboxMenu.SEND_Y - 10, 0xAAAAAA, false);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
	}
}

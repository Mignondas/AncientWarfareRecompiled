package net.shadowmage.ancientwarfarerecompiled.core.input.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfarerecompiled.core.input.IItemKeyInterface.ItemAltFunction;
import net.shadowmage.ancientwarfarerecompiled.core.input.IScrollableItem;
import net.shadowmage.ancientwarfarerecompiled.core.network.PacketItemInteraction;
import net.shadowmage.ancientwarfarerecompiled.core.network.PacketItemMouseScroll;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Détection côté client des touches alternatives et de la molette (joueur accroupi).
 */
@EventBusSubscriber(modid = AncientWarfareRecompiled.MODID, value = Dist.CLIENT)
public final class AWCoreInputHandler {
	private AWCoreInputHandler() {}

	@SubscribeEvent
	public static void onKeyInput(InputEvent.Key event) {
		if (event.getAction() != GLFW.GLFW_PRESS) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || mc.screen != null) {
			return;
		}
		InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());
		List<KeyMapping> mappings = AWCoreKeyMappings.all();
		ItemAltFunction[] functions = ItemAltFunction.values();
		for (int i = 0; i < mappings.size(); i++) {
			if (mappings.get(i).isActiveAndMatches(key)) {
				ItemAltFunction function = functions[i];
				if (accepts(player, InteractionHand.MAIN_HAND, function) || accepts(player, InteractionHand.OFF_HAND, function)) {
					PacketDistributor.sendToServer(new PacketItemInteraction(function.ordinal()));
				}
				return;
			}
		}
	}

	@SubscribeEvent
	public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		double delta = event.getScrollDeltaY();
		if (player == null || mc.screen != null || !player.isShiftKeyDown() || delta == 0) {
			return;
		}
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof IScrollableItem scrollable) {
			boolean up = delta > 0;
			boolean send = up ? scrollable.onScrollUp(player.level(), player, stack) : scrollable.onScrollDown(player.level(), player, stack);
			if (send) {
				PacketDistributor.sendToServer(new PacketItemMouseScroll(up));
			}
			event.setCanceled(true);
		}
	}

	private static boolean accepts(Player player, InteractionHand hand, ItemAltFunction function) {
		ItemStack stack = player.getItemInHand(hand);
		return stack.getItem() instanceof IItemKeyInterface keyInterface && keyInterface.onKeyActionClient(player, stack, function);
	}
}

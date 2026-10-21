package net.shadowmage.ancientwarfarerecompiled.core.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfarerecompiled.core.input.IItemKeyInterface.ItemAltFunction;

/**
 * Client -> serveur : le joueur a appuyé sur une touche alternative.
 */
public record PacketItemInteraction(int altFunction) implements CustomPacketPayload {
	public static final Type<PacketItemInteraction> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AncientWarfareRecompiled.MODID, "item_interaction"));
	public static final StreamCodec<io.netty.buffer.ByteBuf, PacketItemInteraction> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PacketItemInteraction::altFunction, PacketItemInteraction::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/**
	 * Exécuté côté serveur. Essaie la main principale puis la main secondaire.
	 */
	public static void execute(Player player, int altFunction) {
		ItemAltFunction[] functions = ItemAltFunction.values();
		if (altFunction < 0 || altFunction >= functions.length) {
			return;
		}
		ItemAltFunction function = functions[altFunction];
		if (!executeKeyPress(player, InteractionHand.MAIN_HAND, function)) {
			executeKeyPress(player, InteractionHand.OFF_HAND, function);
		}
	}

	private static boolean executeKeyPress(Player player, InteractionHand hand, ItemAltFunction function) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.isEmpty() || !(stack.getItem() instanceof IItemKeyInterface keyInterface)) {
			return false;
		}
		keyInterface.onKeyAction(player, stack, function);
		return true;
	}
}

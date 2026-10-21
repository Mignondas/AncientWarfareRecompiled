package net.shadowmage.ancientwarfarerecompiled.core.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.input.IScrollableItem;

/**
 * Client -> serveur : défilement de la molette alors que le joueur est accroupi.
 */
public record PacketItemMouseScroll(boolean scrollUp) implements CustomPacketPayload {
	public static final Type<PacketItemMouseScroll> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AncientWarfareRecompiled.MODID, "item_mouse_scroll"));
	public static final StreamCodec<io.netty.buffer.ByteBuf, PacketItemMouseScroll> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, PacketItemMouseScroll::scrollUp, PacketItemMouseScroll::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void execute(Player player, boolean scrollUp) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof IScrollableItem scrollable) {
			if (scrollUp) {
				scrollable.onScrollUp(player.level(), player, stack);
			} else {
				scrollable.onScrollDown(player.level(), player, stack);
			}
		}
	}
}

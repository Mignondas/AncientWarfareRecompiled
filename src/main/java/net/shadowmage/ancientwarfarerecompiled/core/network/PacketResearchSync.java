package net.shadowmage.ancientwarfarerecompiled.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;

import java.util.List;

/** Serveur -> client : état de recherche du joueur (terminé, file, en cours). */
public record PacketResearchSync(List<String> completed, List<String> queue, String current, int progress) implements CustomPacketPayload {
	public static final Type<PacketResearchSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AncientWarfareRecompiled.MODID, "research_sync"));
	public static final StreamCodec<ByteBuf, PacketResearchSync> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), PacketResearchSync::completed,
			ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), PacketResearchSync::queue,
			ByteBufCodecs.STRING_UTF8, PacketResearchSync::current,
			ByteBufCodecs.VAR_INT, PacketResearchSync::progress,
			PacketResearchSync::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

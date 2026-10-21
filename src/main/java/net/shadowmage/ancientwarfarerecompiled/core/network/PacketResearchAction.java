package net.shadowmage.ancientwarfarerecompiled.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreAttachments;
import net.shadowmage.ancientwarfarerecompiled.core.research.PlayerResearch;
import net.shadowmage.ancientwarfarerecompiled.core.research.ResearchSync;

/** Client -> serveur : ajouter (enqueue = true) ou retirer un objectif de la file de recherche. */
public record PacketResearchAction(boolean enqueue, String goal) implements CustomPacketPayload {
	public static final Type<PacketResearchAction> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AncientWarfareRecompiled.MODID, "research_action"));
	public static final StreamCodec<ByteBuf, PacketResearchAction> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, PacketResearchAction::enqueue,
			ByteBufCodecs.STRING_UTF8, PacketResearchAction::goal,
			PacketResearchAction::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/** Validation côté serveur : seuls les objectifs réellement disponibles peuvent entrer dans la file. */
	public static void execute(Player player, boolean enqueue, String goal) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		PlayerResearch research = serverPlayer.getData(AWCoreAttachments.PLAYER_RESEARCH.get());
		if (enqueue) {
			if (research.getResearchableGoals().contains(goal)) {
				research.enqueue(goal);
			}
		} else {
			research.dequeue(goal);
		}
		serverPlayer.setData(AWCoreAttachments.PLAYER_RESEARCH.get(), research);
		ResearchSync.send(serverPlayer);
	}
}

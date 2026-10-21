package net.shadowmage.ancientwarfarerecompiled.core.research;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.init.AWCoreAttachments;
import net.shadowmage.ancientwarfarerecompiled.core.network.PacketResearchSync;

/** Envoie l'état de recherche aux joueurs à la connexion, puis une fois par seconde. */
@EventBusSubscriber(modid = AncientWarfareRecompiled.MODID)
public final class ResearchSync {
	private ResearchSync() {}

	public static void send(ServerPlayer player) {
		PlayerResearch research = player.getData(AWCoreAttachments.PLAYER_RESEARCH.get());
		PacketDistributor.sendToPlayer(player, new PacketResearchSync(
				research.getCompleted().stream().sorted().toList(),
				research.getQueue(),
				research.getCurrent().orElse(""),
				research.getProgress()));
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			send(player);
		}
	}

	@SubscribeEvent
	public static void onTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) {
			send(player);
		}
	}
}

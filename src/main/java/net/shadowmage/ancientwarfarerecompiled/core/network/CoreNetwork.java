package net.shadowmage.ancientwarfarerecompiled.core.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.shadowmage.ancientwarfarerecompiled.core.research.client.ClientResearchData;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Enregistrement des paquets réseau du module Core (remplace NetworkHandler de 1.12).
 */
public final class CoreNetwork {
	private CoreNetwork() {}

	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToServer(PacketItemInteraction.TYPE, PacketItemInteraction.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() -> PacketItemInteraction.execute(context.player(), payload.altFunction())));
		registrar.playToServer(PacketItemMouseScroll.TYPE, PacketItemMouseScroll.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() -> PacketItemMouseScroll.execute(context.player(), payload.scrollUp())));
		registrar.playToServer(PacketResearchAction.TYPE, PacketResearchAction.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() -> PacketResearchAction.execute(context.player(), payload.enqueue(), payload.goal())));
		registrar.playToClient(PacketResearchSync.TYPE, PacketResearchSync.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() -> ClientResearchData.apply(payload)));
	}
}

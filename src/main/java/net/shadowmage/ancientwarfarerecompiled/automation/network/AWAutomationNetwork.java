package net.shadowmage.ancientwarfarerecompiled.automation.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Paquets réseau du module Automation.
 */
public final class AWAutomationNetwork {
	private AWAutomationNetwork() {}

	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToServer(PacketMailboxSettings.TYPE, PacketMailboxSettings.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() -> PacketMailboxSettings.execute(context.player(), payload)));
	}
}

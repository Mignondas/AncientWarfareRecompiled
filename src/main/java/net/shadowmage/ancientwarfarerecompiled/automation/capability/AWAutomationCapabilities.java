package net.shadowmage.ancientwarfarerecompiled.automation.capability;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;

/**
 * Capacités d'items des blocs d'automatisation : dessus = envoi, dessous = réception pour la boîte aux lettres.
 */
public final class AWAutomationCapabilities {
	private AWAutomationCapabilities() {}

	public static void register(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AWAutomationTileTypes.MAILBOX.get(),
				(be, side) -> side == Direction.UP ? be.sendInventory : side == Direction.DOWN ? be.receivedInventory : null);
	}
}

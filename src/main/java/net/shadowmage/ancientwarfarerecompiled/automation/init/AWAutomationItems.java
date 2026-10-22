package net.shadowmage.ancientwarfarerecompiled.automation.init;

import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;

/**
 * Objets du module Automation.
 */
public final class AWAutomationItems {
	private AWAutomationItems() {}

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AncientWarfareRecompiled.MODID);

	public static final DeferredItem<BlockItem> CHUNK_LOADER_SIMPLE_ITEM = ITEMS.registerSimpleBlockItem(AWAutomationBlocks.CHUNK_LOADER_SIMPLE);

	public static final DeferredItem<BlockItem> CHUNK_LOADER_DELUXE_ITEM = ITEMS.registerSimpleBlockItem(AWAutomationBlocks.CHUNK_LOADER_DELUXE);

	public static final DeferredItem<BlockItem> MAILBOX_ITEM = ITEMS.registerSimpleBlockItem(AWAutomationBlocks.MAILBOX);

	public static void register(IEventBus modEventBus) {
		ITEMS.register(modEventBus);
	}
}

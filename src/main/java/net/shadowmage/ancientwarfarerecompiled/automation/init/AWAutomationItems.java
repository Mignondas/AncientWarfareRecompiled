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

	public static final DeferredItem<BlockItem> TORQUE_SHAFT_LIGHT_ITEM = ITEMS.registerSimpleBlockItem(AWAutomationBlocks.TORQUE_SHAFT_LIGHT);
	public static final DeferredItem<BlockItem> TORQUE_SHAFT_MEDIUM_ITEM = ITEMS.registerSimpleBlockItem(AWAutomationBlocks.TORQUE_SHAFT_MEDIUM);
	public static final DeferredItem<BlockItem> TORQUE_SHAFT_HEAVY_ITEM = ITEMS.registerSimpleBlockItem(AWAutomationBlocks.TORQUE_SHAFT_HEAVY);
	public static final DeferredItem<BlockItem> HAND_CRANKED_GENERATOR_ITEM = ITEMS.registerSimpleBlockItem(AWAutomationBlocks.HAND_CRANKED_GENERATOR);

	public static final DeferredItem<BlockItem> STIRLING_GENERATOR_ITEM = ITEMS.registerSimpleBlockItem(AWAutomationBlocks.STIRLING_GENERATOR);

	public static final DeferredItem<BlockItem> TORQUE_DISTRIBUTOR_LIGHT_ITEM = ITEMS.registerSimpleBlockItem(AWAutomationBlocks.TORQUE_DISTRIBUTOR_LIGHT);
	public static final DeferredItem<BlockItem> TORQUE_DISTRIBUTOR_MEDIUM_ITEM = ITEMS.registerSimpleBlockItem(AWAutomationBlocks.TORQUE_DISTRIBUTOR_MEDIUM);
	public static final DeferredItem<BlockItem> TORQUE_DISTRIBUTOR_HEAVY_ITEM = ITEMS.registerSimpleBlockItem(AWAutomationBlocks.TORQUE_DISTRIBUTOR_HEAVY);

	public static void register(IEventBus modEventBus) {
		ITEMS.register(modEventBus);
	}
}

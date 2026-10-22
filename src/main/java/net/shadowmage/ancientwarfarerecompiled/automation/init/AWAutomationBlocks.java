package net.shadowmage.ancientwarfarerecompiled.automation.init;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.automation.block.BlockChunkLoaderDeluxe;
import net.shadowmage.ancientwarfarerecompiled.automation.block.BlockChunkLoaderSimple;
import net.shadowmage.ancientwarfarerecompiled.automation.block.BlockMailbox;

/**
 * Blocs du module Automation.
 */
public final class AWAutomationBlocks {
	private AWAutomationBlocks() {}

	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AncientWarfareRecompiled.MODID);

	public static final DeferredBlock<BlockChunkLoaderSimple> CHUNK_LOADER_SIMPLE = BLOCKS.registerBlock("chunk_loader_simple",
			BlockChunkLoaderSimple::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());

	public static final DeferredBlock<BlockChunkLoaderDeluxe> CHUNK_LOADER_DELUXE = BLOCKS.registerBlock("chunk_loader_deluxe",
			BlockChunkLoaderDeluxe::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());

	public static final DeferredBlock<BlockMailbox> MAILBOX = BLOCKS.registerBlock("mailbox",
			BlockMailbox::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());

	public static void register(IEventBus modEventBus) {
		BLOCKS.register(modEventBus);
	}
}

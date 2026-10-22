package net.shadowmage.ancientwarfarerecompiled.automation.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.TileChunkLoaderDeluxe;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.TileMailbox;

/**
 * Types de tuiles (BlockEntity) du module Automation.
 */
public final class AWAutomationTileTypes {
	private AWAutomationTileTypes() {}

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AncientWarfareRecompiled.MODID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileChunkLoaderDeluxe>> CHUNK_LOADER_DELUXE =
			BLOCK_ENTITY_TYPES.register("chunk_loader_deluxe_tile",
					() -> BlockEntityType.Builder.of(TileChunkLoaderDeluxe::new, AWAutomationBlocks.CHUNK_LOADER_DELUXE.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileMailbox>> MAILBOX =
			BLOCK_ENTITY_TYPES.register("mailbox_tile",
					() -> BlockEntityType.Builder.of(TileMailbox::new, AWAutomationBlocks.MAILBOX.get()).build(null));

	public static void register(IEventBus modEventBus) {
		BLOCK_ENTITY_TYPES.register(modEventBus);
	}
}

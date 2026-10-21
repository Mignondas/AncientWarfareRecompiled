package net.shadowmage.ancientwarfarerecompiled.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.tile.TileEngineeringStation;
import net.shadowmage.ancientwarfarerecompiled.core.tile.TileResearchStation;

/**
 * Types d'entités de bloc du module Core.
 */
public final class AWCoreTileTypes {
	private AWCoreTileTypes() {}

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AncientWarfareRecompiled.MODID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEngineeringStation>> ENGINEERING_STATION = BLOCK_ENTITY_TYPES.register("engineering_station_tile",
			() -> BlockEntityType.Builder.of(TileEngineeringStation::new, AWCoreBlocks.ENGINEERING_STATION.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileResearchStation>> RESEARCH_STATION = BLOCK_ENTITY_TYPES.register("research_station_tile",
			() -> BlockEntityType.Builder.of(TileResearchStation::new, AWCoreBlocks.RESEARCH_STATION.get()).build(null));

	public static void register(IEventBus modEventBus) {
		BLOCK_ENTITY_TYPES.register(modEventBus);
	}
}

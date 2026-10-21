package net.shadowmage.ancientwarfarerecompiled.core.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.block.BlockEngineeringStation;
import net.shadowmage.ancientwarfarerecompiled.core.block.BlockResearchStation;

/**
 * Blocs du module Core : établi d'ingénierie et station de recherche.
 */
public final class AWCoreBlocks {
	private AWCoreBlocks() {}

	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AncientWarfareRecompiled.MODID);

	public static final DeferredBlock<BlockEngineeringStation> ENGINEERING_STATION = BLOCKS.registerBlock("engineering_station",
			BlockEngineeringStation::new, stationProperties());

	public static final DeferredBlock<BlockResearchStation> RESEARCH_STATION = BLOCKS.registerBlock("research_station",
			BlockResearchStation::new, stationProperties());

	private static BlockBehaviour.Properties stationProperties() {
		return BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD)
				.strength(2.0F)
				.sound(SoundType.WOOD)
				.noOcclusion();
	}

	public static void register(IEventBus modEventBus) {
		BLOCKS.register(modEventBus);
	}
}

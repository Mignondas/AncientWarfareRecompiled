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
import net.shadowmage.ancientwarfarerecompiled.automation.block.BlockHandCrankedGenerator;
import net.shadowmage.ancientwarfarerecompiled.automation.block.BlockStirlingGenerator;
import net.shadowmage.ancientwarfarerecompiled.automation.block.BlockTorqueShaft;
import net.shadowmage.ancientwarfarerecompiled.automation.block.TorqueTier;

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

	public static final DeferredBlock<BlockTorqueShaft> TORQUE_SHAFT_LIGHT = BLOCKS.registerBlock("torque_shaft_light",
			p -> new BlockTorqueShaft(p, TorqueTier.LIGHT),
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).sound(SoundType.METAL).noOcclusion());
	public static final DeferredBlock<BlockTorqueShaft> TORQUE_SHAFT_MEDIUM = BLOCKS.registerBlock("torque_shaft_medium",
			p -> new BlockTorqueShaft(p, TorqueTier.MEDIUM),
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).sound(SoundType.METAL).noOcclusion());
	public static final DeferredBlock<BlockTorqueShaft> TORQUE_SHAFT_HEAVY = BLOCKS.registerBlock("torque_shaft_heavy",
			p -> new BlockTorqueShaft(p, TorqueTier.HEAVY),
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f).sound(SoundType.METAL).noOcclusion());
	public static final DeferredBlock<BlockHandCrankedGenerator> HAND_CRANKED_GENERATOR = BLOCKS.registerBlock("hand_cranked_generator",
			BlockHandCrankedGenerator::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD).noOcclusion());
	public static final DeferredBlock<BlockStirlingGenerator> STIRLING_GENERATOR = BLOCKS.registerBlock("stirling_generator",
			BlockStirlingGenerator::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());

	public static void register(IEventBus modEventBus) {
		BLOCKS.register(modEventBus);
	}
}

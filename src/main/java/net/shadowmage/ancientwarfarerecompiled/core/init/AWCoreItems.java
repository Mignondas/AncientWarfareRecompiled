package net.shadowmage.ancientwarfarerecompiled.core.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.item.AWToolMaterial;
import net.shadowmage.ancientwarfarerecompiled.core.item.ItemHammer;
import net.shadowmage.ancientwarfarerecompiled.core.item.ItemQuill;

/**
 * Objets du module Core.
 *
 * Portage : en 1.12, les composants étaient des sous-types (métadonnées) d'un même objet. En 1.21.1,
 * chaque composant est un objet distinct. Les modèles sont dans les fichiers JSON des ressources ;
 * les dictionnaires d'ores (oredict) de 1.12 deviendront des tags (à porter avec le module Core).
 */
public final class AWCoreItems {
	private AWCoreItems() {}

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AncientWarfareRecompiled.MODID);

	public static final DeferredItem<Item> STEEL_INGOT = ITEMS.registerSimpleItem("steel_ingot");

	/* blocs du module Core */
	public static final DeferredItem<BlockItem> ENGINEERING_STATION_ITEM = ITEMS.registerSimpleBlockItem("engineering_station", AWCoreBlocks.ENGINEERING_STATION);
	public static final DeferredItem<BlockItem> RESEARCH_STATION_ITEM = ITEMS.registerSimpleBlockItem("research_station", AWCoreBlocks.RESEARCH_STATION);

	/* marteaux : un par matériau */
	public static final DeferredItem<ItemHammer> WOODEN_HAMMER = ITEMS.register("wooden_hammer", () -> new ItemHammer(AWToolMaterial.WOOD));
	public static final DeferredItem<ItemHammer> STONE_HAMMER = ITEMS.register("stone_hammer", () -> new ItemHammer(AWToolMaterial.STONE));
	public static final DeferredItem<ItemHammer> IRON_HAMMER = ITEMS.register("iron_hammer", () -> new ItemHammer(AWToolMaterial.IRON));
	public static final DeferredItem<ItemHammer> GOLD_HAMMER = ITEMS.register("gold_hammer", () -> new ItemHammer(AWToolMaterial.GOLD));
	public static final DeferredItem<ItemHammer> DIAMOND_HAMMER = ITEMS.register("diamond_hammer", () -> new ItemHammer(AWToolMaterial.DIAMOND));

	/* plumes : une par matériau */
	public static final DeferredItem<ItemQuill> WOODEN_QUILL = ITEMS.register("wooden_quill", () -> new ItemQuill(AWToolMaterial.WOOD));
	public static final DeferredItem<ItemQuill> STONE_QUILL = ITEMS.register("stone_quill", () -> new ItemQuill(AWToolMaterial.STONE));
	public static final DeferredItem<ItemQuill> IRON_QUILL = ITEMS.register("iron_quill", () -> new ItemQuill(AWToolMaterial.IRON));
	public static final DeferredItem<ItemQuill> GOLD_QUILL = ITEMS.register("gold_quill", () -> new ItemQuill(AWToolMaterial.GOLD));
	public static final DeferredItem<ItemQuill> DIAMOND_QUILL = ITEMS.register("diamond_quill", () -> new ItemQuill(AWToolMaterial.DIAMOND));

	/* composants du module Automation (oredict de 1.12 : gearWood, gearIron, gearSteel, etc.) */
	public static final DeferredItem<Item> WOODEN_GEAR_SET = ITEMS.registerSimpleItem("wooden_gear_set");
	public static final DeferredItem<Item> IRON_GEAR_SET = ITEMS.registerSimpleItem("iron_gear_set");
	public static final DeferredItem<Item> STEEL_GEAR_SET = ITEMS.registerSimpleItem("steel_gear_set");
	public static final DeferredItem<Item> WOODEN_BEARINGS = ITEMS.registerSimpleItem("wooden_bearings");
	public static final DeferredItem<Item> IRON_BEARINGS = ITEMS.registerSimpleItem("iron_bearings");
	public static final DeferredItem<Item> STEEL_BEARINGS = ITEMS.registerSimpleItem("steel_bearings");
	public static final DeferredItem<Item> WOODEN_TORQUE_SHAFT = ITEMS.registerSimpleItem("wooden_torque_shaft");
	public static final DeferredItem<Item> IRON_TORQUE_SHAFT = ITEMS.registerSimpleItem("iron_torque_shaft");
	public static final DeferredItem<Item> STEEL_TORQUE_SHAFT = ITEMS.registerSimpleItem("steel_torque_shaft");

	/**
	 * Enregistre les objets sur le bus de mod. À appeler depuis le constructeur du mod.
	 */
	public static void register(IEventBus modEventBus) {
		ITEMS.register(modEventBus);
	}
}

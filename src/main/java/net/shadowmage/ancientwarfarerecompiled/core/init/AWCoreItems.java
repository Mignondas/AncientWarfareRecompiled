package net.shadowmage.ancientwarfarerecompiled.core.init;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;

/**
 * Objets du module Core.
 *
 * Portage : en 1.12, les composants étaient des sous-types (métadonnées) d'un même objet. En 1.21.1,
 * chaque composant est un objet distinct. Les modèles sont dans les fichiers JSON des ressources ;
 * les dictionnaires d'ores (oredict) de 1.12 deviendront des tags (à porter avec le module Core).
 *
 * L'onglet créatif n'est pas encore porté : ces objets ne sont donc pas encore visibles dans l'inventaire créatif.
 */
public final class AWCoreItems {
	private AWCoreItems() {}

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AncientWarfareRecompiled.MODID);

	public static final DeferredItem<Item> STEEL_INGOT = ITEMS.registerSimpleItem("steel_ingot");

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

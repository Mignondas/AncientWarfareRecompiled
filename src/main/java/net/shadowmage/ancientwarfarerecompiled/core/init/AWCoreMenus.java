package net.shadowmage.ancientwarfarerecompiled.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.core.menu.EngineeringStationMenu;
import net.shadowmage.ancientwarfarerecompiled.core.menu.ResearchBookMenu;
import net.shadowmage.ancientwarfarerecompiled.core.menu.ResearchStationMenu;

/**
 * Types de menus (interfaces de conteneur) du module Core.
 */
public final class AWCoreMenus {
	private AWCoreMenus() {}

	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, AncientWarfareRecompiled.MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<EngineeringStationMenu>> ENGINEERING_STATION =
			MENUS.register("engineering_station", () -> IMenuTypeExtension.create(EngineeringStationMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<ResearchStationMenu>> RESEARCH_STATION =
			MENUS.register("research_station", () -> IMenuTypeExtension.create(ResearchStationMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<ResearchBookMenu>> RESEARCH_BOOK =
			MENUS.register("research_book", () -> IMenuTypeExtension.create((id, inv, buf) -> new ResearchBookMenu(id, inv)));

	public static void register(IEventBus modEventBus) {
		MENUS.register(modEventBus);
	}
}

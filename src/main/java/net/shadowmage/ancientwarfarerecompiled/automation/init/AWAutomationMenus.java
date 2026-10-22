package net.shadowmage.ancientwarfarerecompiled.automation.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import net.shadowmage.ancientwarfarerecompiled.automation.menu.ChunkLoaderDeluxeMenu;
import net.shadowmage.ancientwarfarerecompiled.automation.menu.MailboxMenu;
import net.shadowmage.ancientwarfarerecompiled.automation.menu.StirlingGeneratorMenu;

/**
 * Types de menus du module Automation.
 */
public final class AWAutomationMenus {
	private AWAutomationMenus() {}

	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, AncientWarfareRecompiled.MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<ChunkLoaderDeluxeMenu>> CHUNK_LOADER_DELUXE =
			MENUS.register("chunk_loader_deluxe", () -> IMenuTypeExtension.create(ChunkLoaderDeluxeMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<MailboxMenu>> MAILBOX =
			MENUS.register("mailbox", () -> IMenuTypeExtension.create(MailboxMenu::new));

	public static final DeferredHolder<MenuType<?>, MenuType<StirlingGeneratorMenu>> STIRLING_GENERATOR =
			MENUS.register("stirling_generator", () -> IMenuTypeExtension.create(StirlingGeneratorMenu::new));

	public static void register(IEventBus modEventBus) {
		MENUS.register(modEventBus);
	}
}

package net.shadowmage.ancientwarfarerecompiled.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;

/**
 * Onglets créatifs du module Core (remplace AWCoreTab de 1.12).
 */
public final class AWCoreTabs {
	private AWCoreTabs() {}

	public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AncientWarfareRecompiled.MODID);

	/**
	 * Onglet du module Core. L'icône est provisoire (lingot d'acier) : l'icône d'origine est le livre de recherche,
	 * à rétablir quand il sera porté.
	 */
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CORE_TAB = TABS.register("core", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.ancientwarfarerecompiled"))
			.icon(() -> AWCoreItems.STEEL_INGOT.get().getDefaultInstance())
			.displayItems((parameters, output) -> {
				output.accept(AWCoreItems.STEEL_INGOT.get());
				output.accept(AWCoreItems.WOODEN_GEAR_SET.get());
				output.accept(AWCoreItems.IRON_GEAR_SET.get());
				output.accept(AWCoreItems.STEEL_GEAR_SET.get());
				output.accept(AWCoreItems.WOODEN_BEARINGS.get());
				output.accept(AWCoreItems.IRON_BEARINGS.get());
				output.accept(AWCoreItems.STEEL_BEARINGS.get());
				output.accept(AWCoreItems.WOODEN_TORQUE_SHAFT.get());
				output.accept(AWCoreItems.IRON_TORQUE_SHAFT.get());
				output.accept(AWCoreItems.STEEL_TORQUE_SHAFT.get());
			})
			.build());

	/**
	 * Enregistre les onglets sur le bus de mod. À appeler depuis le constructeur du mod.
	 */
	public static void register(IEventBus modEventBus) {
		TABS.register(modEventBus);
	}
}

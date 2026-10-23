package net.shadowmage.ancientwarfarerecompiled;

import net.neoforged.bus.api.IEventBus;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Partie cliente du mod : ne se charge pas sur un serveur dédié.
 * Active le bouton « Config » dans la liste des mods.
 */
@Mod(value = AncientWarfareRecompiled.MODID, dist = Dist.CLIENT)
public class AncientWarfareRecompiledClient {
	public AncientWarfareRecompiledClient(ModContainer container, IEventBus modBus) {
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		modBus.addListener(this::registerRenderers);
		modBus.addListener(this::registerModels);
	}

	private void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes.WINDMILL_BLADE.get(),
				net.shadowmage.ancientwarfarerecompiled.automation.render.WindmillBladeRenderer::new);
	}

	private void registerModels(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event) {
		event.register(net.shadowmage.ancientwarfarerecompiled.automation.render.WindmillBladeRenderer.ROTOR);
	}
}

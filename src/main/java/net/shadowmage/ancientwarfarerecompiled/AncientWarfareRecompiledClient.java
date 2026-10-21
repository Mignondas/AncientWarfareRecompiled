package net.shadowmage.ancientwarfarerecompiled;

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
	public AncientWarfareRecompiledClient(ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}
}

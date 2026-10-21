package net.shadowmage.ancientwarfarerecompiled.core.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;

import java.util.ArrayList;

/**
 * Référence à un objet, lue depuis une liste de configuration de la forme "namespace:nom".
 *
 * Note de portage : la métadonnée (damage) de 1.12 n'existe plus en 1.21.1. Une entrée
 * "namespace:nom:valeur" est acceptée mais la valeur est ignorée, avec un avertissement.
 */
public class ItemWrapper {
	public final Item item;

	public ItemWrapper(Item item) {
		this.item = item;
	}

	public static ArrayList<ItemWrapper> buildList(String listName, String[] input) {
		ArrayList<ItemWrapper> outputList = new ArrayList<>();

		AncientWarfareRecompiled.LOGGER.info("Construction de {}...", listName);

		for (String entry : input) {
			String itemName = entry.trim();
			if (itemName.isEmpty()) {
				continue;
			}

			String[] parts = itemName.split(":");
			if (parts.length != 2 && parts.length != 3) {
				AncientWarfareRecompiled.LOGGER.warn(" - Objet invalide (nombre de parties : {}) : {}", parts.length, itemName);
				continue;
			}

			ResourceLocation id = ResourceLocation.tryParse(parts[0] + ":" + parts[1]);
			if (id == null) {
				AncientWarfareRecompiled.LOGGER.warn(" - Identifiant invalide : {}", itemName);
				continue;
			}

			if (!BuiltInRegistries.ITEM.containsKey(id)) {
				AncientWarfareRecompiled.LOGGER.warn(" - Objet absent ignoré : {}", itemName);
				continue;
			}

			if (parts.length == 3) {
				AncientWarfareRecompiled.LOGGER.warn(" - La métadonnée '{}' est ignorée en 1.21.1 (entrée : {})", parts[2], itemName);
			}

			outputList.add(new ItemWrapper(BuiltInRegistries.ITEM.get(id)));
		}

		AncientWarfareRecompiled.LOGGER.info("...{} objets ajoutés à {}", outputList.size(), listName);

		return outputList;
	}
}

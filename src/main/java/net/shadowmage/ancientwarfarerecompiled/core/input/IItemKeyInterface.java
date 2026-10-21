package net.shadowmage.ancientwarfarerecompiled.core.input;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Objets pouvant réagir aux touches alternatives (Z, X, C, V, B par défaut).
 *
 * La touche est détectée côté client, qui envoie un paquet au serveur ; le serveur appelle onKeyAction.
 * Remplace le clic gauche sur les objets de 1.12, peu fiable.
 */
public interface IItemKeyInterface {

	enum ItemAltFunction {
		ALT_FUNCTION_1,
		ALT_FUNCTION_2,
		ALT_FUNCTION_3,
		ALT_FUNCTION_4,
		ALT_FUNCTION_5
	}

	/**
	 * Appelée côté client avant l'envoi du paquet.
	 *
	 * @return true pour envoyer le paquet au serveur
	 */
	boolean onKeyActionClient(Player player, ItemStack stack, ItemAltFunction altFunction);

	/**
	 * Appelée côté serveur lorsque le client appuie sur la touche liée à la fonction alternative.
	 */
	void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction);
}

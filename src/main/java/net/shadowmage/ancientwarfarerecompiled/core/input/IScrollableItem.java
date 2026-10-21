package net.shadowmage.ancientwarfarerecompiled.core.input;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Objets réagissant à la molette de la souris lorsque le joueur est accroupi.
 */
public interface IScrollableItem {
	/**
	 * @return true pour envoyer le défilement au serveur
	 */
	boolean onScrollUp(Level level, Player player, ItemStack stack);

	/**
	 * @return true pour envoyer le défilement au serveur
	 */
	boolean onScrollDown(Level level, Player player, ItemStack stack);
}

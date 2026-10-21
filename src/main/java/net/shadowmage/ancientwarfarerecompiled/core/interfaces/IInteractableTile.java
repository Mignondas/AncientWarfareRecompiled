package net.shadowmage.ancientwarfarerecompiled.core.interfaces;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Tuiles qui réagissent à un clic du joueur.
 */
public interface IInteractableTile {
	boolean onBlockClicked(Player player, @Nullable InteractionHand hand);
}

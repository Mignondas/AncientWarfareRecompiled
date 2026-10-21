package net.shadowmage.ancientwarfarerecompiled.core.owner;

import net.minecraft.world.entity.player.Player;

/**
 * Tuiles ou entités appartenant à un joueur. Appelé lors de la pose ou du placement d'un objet
 * pour définir le propriétaire.
 */
public interface IOwnable {
	void setOwner(Player player);

	void setOwner(Owner owner);

	Owner getOwner();

	boolean isOwner(Player player);
}

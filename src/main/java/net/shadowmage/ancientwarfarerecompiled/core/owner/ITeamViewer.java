package net.shadowmage.ancientwarfarerecompiled.core.owner;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**
 * Source de la notion d'équipe (scoreboard, mods d'équipes, etc.).
 */
public interface ITeamViewer {
	boolean areTeamMates(Level level, UUID player1, UUID player2, String playerName1, String playerName2);

	boolean areFriendly(Level level, UUID player1, @Nullable UUID player2, String playerName1, String playerName2);

	Set<ResourceLocation> getPlayerTeamNames(Level level, UUID playerId, String playerName);

	default boolean needsRegularMembershipRecheck() {
		return true;
	}

	String getName();
}

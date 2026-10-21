package net.shadowmage.ancientwarfarerecompiled.core.owner;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Équipes définies par le scoreboard de Minecraft.
 */
public class ScoreboardTeamViewer implements ITeamViewer {
	@Override
	public boolean areTeamMates(Level level, UUID player1, UUID player2, String playerName1, String playerName2) {
		return player1.equals(player2) || isSameTeam(level, playerName1, playerName2);
	}

	@Override
	public boolean areFriendly(Level level, UUID player1, @Nullable UUID player2, String playerName1, String playerName2) {
		return playerName1.equals(playerName2) || player1.equals(player2) || isSameTeam(level, playerName1, playerName2);
	}

	@Override
	public Set<ResourceLocation> getPlayerTeamNames(Level level, UUID playerId, String playerName) {
		PlayerTeam team = level.getScoreboard().getPlayersTeam(playerName);
		return team == null ? Collections.emptySet() : Collections.singleton(ResourceLocation.withDefaultNamespace(team.getName()));
	}

	@Override
	public String getName() {
		return "minecraft";
	}

	private boolean isSameTeam(Level level, String playerName1, String playerName2) {
		PlayerTeam team = level.getScoreboard().getPlayersTeam(playerName1);
		return team != null && team.isAlliedTo(level.getScoreboard().getPlayersTeam(playerName2));
	}
}

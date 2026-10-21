package net.shadowmage.ancientwarfarerecompiled.core.owner;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Registre des sources d'équipes. Les mods peuvent en ajouter via {@link #registerTeamViewer(ITeamViewer)}.
 */
public class TeamViewerRegistry {
	private TeamViewerRegistry() {}

	private static final Set<ITeamViewer> teamViewers = new HashSet<>();

	static {
		teamViewers.add(new ScoreboardTeamViewer());
	}

	public static void registerTeamViewer(ITeamViewer teamViewer) {
		teamViewers.add(teamViewer);
	}

	public static boolean areTeamMates(Level level, UUID player1, UUID player2, String playerName1, String playerName2) {
		for (ITeamViewer teamViewer : teamViewers) {
			if (teamViewer.areTeamMates(level, player1, player2, playerName1, playerName2)) {
				return true;
			}
		}
		return false;
	}

	public static boolean areFriendly(Level level, UUID player1, @Nullable UUID player2, String playerName1, String playerName2) {
		for (ITeamViewer teamViewer : teamViewers) {
			if (teamViewer.areFriendly(level, player1, player2, playerName1, playerName2)) {
				return true;
			}
		}
		return false;
	}

	public static Set<ResourceLocation> getPlayerTeamNames(Level level, UUID playerUUID, String playerName) {
		Set<ResourceLocation> ret = new HashSet<>();
		teamViewers.forEach(v -> ret.addAll(v.getPlayerTeamNames(level, playerUUID, playerName)));
		return ret;
	}

	public static Set<String> getRegularlyCheckedViewerNames() {
		Set<String> ret = new HashSet<>();
		for (ITeamViewer teamViewer : teamViewers) {
			if (teamViewer.needsRegularMembershipRecheck()) {
				ret.add(teamViewer.getName());
			}
		}
		return ret;
	}

	public static Set<ResourceLocation> getRegularlyCheckedPlayerTeamNames(Level level, UUID playerUUID, String playerName) {
		Set<ResourceLocation> ret = new HashSet<>();
		for (ITeamViewer teamViewer : teamViewers) {
			if (teamViewer.needsRegularMembershipRecheck()) {
				ret.addAll(teamViewer.getPlayerTeamNames(level, playerUUID, playerName));
			}
		}
		return ret;
	}
}

package net.shadowmage.ancientwarfarerecompiled.core.research.client;

import net.shadowmage.ancientwarfarerecompiled.core.network.PacketResearchSync;
import net.shadowmage.ancientwarfarerecompiled.core.research.ResearchGoal;
import net.shadowmage.ancientwarfarerecompiled.core.research.ResearchRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Copie côté client de la recherche du joueur local, mise à jour par PacketResearchSync. */
public final class ClientResearchData {
	private static volatile Set<String> completed = Set.of();
	private static volatile List<String> queue = List.of();
	private static volatile String current = "";
	private static volatile int progress = -1;
	private static volatile int version = 0;

	private ClientResearchData() {}

	public static void apply(PacketResearchSync packet) {
		completed = Set.copyOf(packet.completed());
		queue = List.copyOf(packet.queue());
		current = packet.current();
		progress = packet.progress();
		version++;
	}

	public static int version() {
		return version;
	}

	public static List<String> queue() {
		return queue;
	}

	/** Recherche en cours, ou null. */
	public static String current() {
		return current.isEmpty() ? null : current;
	}

	public static int progress() {
		return progress;
	}

	/** Objectifs disponibles : dépendances satisfaites, ni terminés, ni en file, ni en cours. */
	public static List<String> available() {
		Set<String> known = new HashSet<>(completed);
		known.addAll(queue);
		if (current() != null) {
			known.add(current());
		}
		return ResearchRegistry.getAllResearchGoals().stream()
				.filter(goal -> !known.contains(goal.getName()) && goal.canResearch(known))
				.map(ResearchGoal::getName)
				.sorted()
				.toList();
	}
}

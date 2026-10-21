package net.shadowmage.ancientwarfarerecompiled.core.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Progression de recherche d'un joueur. Stockée comme attachment NeoForge sur le joueur.
 */
public final class PlayerResearch {
	public static final Codec<PlayerResearch> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.listOf().optionalFieldOf("completed", List.of()).forGetter(p -> List.copyOf(p.completed)),
			Codec.STRING.listOf().optionalFieldOf("queued", List.of()).forGetter(p -> List.copyOf(p.queue)),
			Codec.STRING.optionalFieldOf("current").forGetter(p -> Optional.ofNullable(p.current)),
			Codec.INT.optionalFieldOf("progress", -1).forGetter(p -> p.progress)
	).apply(i, PlayerResearch::new));

	private final Set<String> completed = new HashSet<>();
	private final List<String> queue = new ArrayList<>();
	private String current;
	private int progress = -1;

	public PlayerResearch() {}

	private PlayerResearch(List<String> completed, List<String> queue, Optional<String> current, int progress) {
		this.completed.addAll(completed);
		this.queue.addAll(queue);
		this.current = current.orElse(null);
		this.progress = progress;
	}

	public boolean isCompleted(String research) {
		return completed.contains(research);
	}

	public Set<String> getCompleted() {
		return Set.copyOf(completed);
	}

	public List<String> getQueue() {
		return List.copyOf(queue);
	}

	public Optional<String> getCurrent() {
		return Optional.ofNullable(current);
	}

	public int getProgress() {
		return progress;
	}

	/**
	 * Ensemble de tout ce que le joueur connaît ou prépare : terminé, en file et en cours.
	 */
	public Set<String> getKnowledge() {
		Set<String> knowledge = new HashSet<>(completed);
		knowledge.addAll(queue);
		if (current != null) {
			knowledge.add(current);
		}
		return knowledge;
	}

	public void addCompleted(String research) {
		completed.add(research);
		queue.remove(research);
		if (research.equals(current)) {
			current = null;
			progress = -1;
		}
	}

	public void removeCompleted(String research) {
		completed.remove(research);
	}

	public void clear() {
		completed.clear();
		queue.clear();
		current = null;
		progress = -1;
	}

	public void fill() {
		clear();
		for (ResearchGoal goal : ResearchRegistry.getAllResearchGoals()) {
			completed.add(goal.getName());
		}
	}

	public void enqueue(String research) {
		if (!queue.contains(research)) {
			queue.add(research);
		}
	}

	/**
	 * Retire un objectif de la file. Les objectifs suivants sont conservés seulement si leurs dépendances restent satisfaites.
	 */
	public void dequeue(String research) {
		int index = queue.indexOf(research);
		if (index < 0) {
			return;
		}
		List<String> after = new ArrayList<>(queue.subList(index + 1, queue.size()));
		queue.subList(index, queue.size()).clear();
		Set<String> total = getKnowledge();
		for (String next : after) {
			ResearchGoal goal = ResearchRegistry.getResearch(next);
			if (goal != null && goal.canResearch(total)) {
				total.add(next);
				queue.add(next);
			}
		}
	}

	/**
	 * Démarre le premier objectif de la file, s'il n'y a pas déjà de recherche en cours.
	 */
	public boolean startResearch(String research) {
		if (current != null || !queue.contains(research)) {
			return false;
		}
		queue.remove(research);
		current = research;
		progress = 0;
		return true;
	}

	/**
	 * Ajoute de la progression à la recherche en cours. Termine la recherche si le temps requis est atteint.
	 */
	public boolean addProgress(int amount) {
		if (current == null) {
			return false;
		}
		progress += amount;
		ResearchGoal goal = ResearchRegistry.getResearch(current);
		if (goal != null && progress >= goal.getTotalResearchTime()) {
			addCompleted(current);
		}
		return true;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	/**
	 * Objectifs qui peuvent être commencés maintenant (dépendances satisfaites, pas encore connus).
	 */
	public Set<String> getResearchableGoals() {
		Set<String> knowledge = getKnowledge();
		Set<String> result = new HashSet<>();
		for (ResearchGoal goal : ResearchRegistry.getAllResearchGoals()) {
			if (!knowledge.contains(goal.getName()) && goal.canResearch(knowledge)) {
				result.add(goal.getName());
			}
		}
		return result;
	}
}

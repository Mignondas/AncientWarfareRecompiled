package net.shadowmage.ancientwarfarerecompiled.core.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.shadowmage.ancientwarfarerecompiled.core.config.AWCoreConfig;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Un objectif de recherche, chargé depuis data/ancientwarfarerecompiled/research/*.json.
 */
public final class ResearchGoal {
	public static final Codec<ResearchGoal> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.fieldOf("name").forGetter(ResearchGoal::getName),
			Codec.STRING.listOf().optionalFieldOf("dependencies", List.of()).forGetter(ResearchGoal::getDependencyNames),
			Ingredient.CODEC.listOf().optionalFieldOf("resources", List.of()).forGetter(ResearchGoal::getResources),
			Codec.INT.fieldOf("time").forGetter(ResearchGoal::getTotalResearchTime)
	).apply(i, ResearchGoal::new));

	private final String name;
	private final List<String> dependencies;
	private final List<Ingredient> resources;
	private final int researchTime;

	public ResearchGoal(String name, List<String> dependencies, List<Ingredient> resources, int researchTime) {
		this.name = name;
		this.dependencies = List.copyOf(dependencies);
		this.resources = List.copyOf(resources);
		this.researchTime = researchTime;
	}

	public String getName() {
		return name;
	}

	public String getUnlocalizedName() {
		return "research." + name;
	}

	public int getTotalResearchTime() {
		return researchTime;
	}

	public List<String> getDependencyNames() {
		return dependencies;
	}

	public List<Ingredient> getResources() {
		return resources;
	}

	/**
	 * Vrai si toutes les dépendances (directes et indirectes) sont dans l'ensemble de connaissances donné.
	 */
	public boolean canResearch(Set<String> knownResearch) {
		return knownResearch.containsAll(resolveFullDependencies());
	}

	/**
	 * Ensemble de toutes les dépendances, récursivement. Les noms inconnus sont ignorés.
	 */
	public Set<String> resolveFullDependencies() {
		Set<String> found = new HashSet<>();
		Deque<String> open = new ArrayDeque<>(dependencies);
		while (!open.isEmpty()) {
			String dep = open.poll();
			if (!found.add(dep)) {
				continue;
			}
			ResearchGoal goal = ResearchRegistry.getResearch(dep);
			if (goal != null) {
				open.addAll(goal.dependencies);
			}
		}
		return found;
	}

	/**
	 * Consomme les ressources de recherche dans l'inventaire donné, si elles sont présentes.
	 * Chaque ingrédient consomme un objet. Ne fait rien si la règle de config est désactivée.
	 *
	 * @return true si la recherche peut démarrer
	 */
	public boolean tryStart(IItemHandler handler) {
		if (!AWCoreConfig.ENABLE_RESEARCH_RESOURCE_USE.get()) {
			return true;
		}
		int[] reserved = new int[handler.getSlots()];
		List<Integer> slotsToUse = new ArrayList<>();
		for (Ingredient ingredient : resources) {
			int found = -1;
			for (int slot = 0; slot < handler.getSlots() && found < 0; slot++) {
				ItemStack stack = handler.getStackInSlot(slot);
				if (!stack.isEmpty() && stack.getCount() - reserved[slot] > 0 && ingredient.test(stack)) {
					found = slot;
				}
			}
			if (found < 0) {
				return false;
			}
			reserved[found]++;
			slotsToUse.add(found);
		}
		for (int slot : slotsToUse) {
			handler.extractItem(slot, 1, false);
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof ResearchGoal other && name.equals(other.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}

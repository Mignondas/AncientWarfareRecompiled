package net.shadowmage.ancientwarfarerecompiled.core.research;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Recettes verrouillées par la recherche : une recette de craft est marquée par le champ "research".
 * Le registre associe l'identifiant de la recette à la recherche requise.
 */
public final class ResearchRecipes {
	private static volatile Map<ResourceLocation, String> required = Map.of();

	private ResearchRecipes() {}

	public static Optional<String> getRequiredResearch(ResourceLocation recipeId) {
		return Optional.ofNullable(required.get(recipeId));
	}

	public static final class Loader extends SimpleJsonResourceReloadListener {
		public Loader() {
			super(new Gson(), "recipe");
		}

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
			Map<ResourceLocation, String> map = new HashMap<>();
			for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
				if (!entry.getKey().getNamespace().equals(AncientWarfareRecompiled.MODID) || !entry.getValue().isJsonObject()) {
					continue;
				}
				JsonObject json = entry.getValue().getAsJsonObject();
				if (json.has("research") && json.get("research").isJsonPrimitive()) {
					map.put(entry.getKey(), json.get("research").getAsString());
				}
			}
			required = Map.copyOf(map);
			AncientWarfareRecompiled.LOGGER.info("{} recettes verrouillées par la recherche", map.size());
		}
	}
}

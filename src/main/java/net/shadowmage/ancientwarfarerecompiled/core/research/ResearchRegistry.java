package net.shadowmage.ancientwarfarerecompiled.core.research;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Registre des objectifs de recherche, rechargé avec les datapacks (dossier research/).
 */
public final class ResearchRegistry {
	private static volatile Map<String, ResearchGoal> goals = Map.of();

	private ResearchRegistry() {}

	@Nullable
	public static ResearchGoal getResearch(String name) {
		return goals.get(name);
	}

	public static boolean researchExists(String name) {
		return goals.containsKey(name);
	}

	public static Collection<ResearchGoal> getAllResearchGoals() {
		return goals.values();
	}

	@EventBusSubscriber(modid = AncientWarfareRecompiled.MODID)
	public static final class Events {
		private Events() {}

		@SubscribeEvent
		public static void addReloadListeners(AddReloadListenerEvent event) {
			event.addListener(new Loader());
		}
	}

	private static final class Loader extends SimpleJsonResourceReloadListener {
		private Loader() {
			super(new Gson(), "research");
		}

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
			Map<String, ResearchGoal> loaded = new HashMap<>();
			for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
				ResearchGoal.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
						.resultOrPartial(err -> AncientWarfareRecompiled.LOGGER.error("Recherche invalide {} : {}", entry.getKey(), err))
						.ifPresent(goal -> loaded.put(goal.getName(), goal));
			}
			goals = Map.copyOf(loaded);
			AncientWarfareRecompiled.LOGGER.info("{} objectifs de recherche chargés", loaded.size());
		}
	}
}

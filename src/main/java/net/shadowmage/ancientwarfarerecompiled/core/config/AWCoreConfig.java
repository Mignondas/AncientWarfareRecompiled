package net.shadowmage.ancientwarfarerecompiled.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Options de configuration du module Core (remplace AWCoreStatics de 1.12).
 *
 * Les réglages par objet (craftable / recherchable) ne sont pas portés ici : ils seront
 * gérés par les données du jeu, car une liste par objet ne convient pas à un fichier de configuration.
 */
public final class AWCoreConfig {
	private AWCoreConfig() {}

	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	/* options générales : identiques côté client et serveur */
	public static final ModConfigSpec.BooleanValue DEBUG = BUILDER
			.comment("Active les messages de débogage supplémentaires dans la console et les vérifications à l'exécution.",
					"Peut dégrader les performances et générer des journaux volumineux.")
			.define("debug_output", false);

	/* options serveur */
	public static final ModConfigSpec.BooleanValue FIRE_BLOCK_BREAK_EVENTS = BUILDER
			.comment("Déclenche les événements de cassure de bloc pour toutes les opérations de l'automatisation et des véhicules.",
					"Si désactivé, aucun événement de cassure n'est envoyé : cela neutralise les mods de protection des blocs.",
					"Les événements utilisent le nom du propriétaire pour les systèmes de permissions.")
			.define("fire_block_break_events", true);

	public static final ModConfigSpec.BooleanValue INCLUDE_RESEARCH_IN_CHESTS = BUILDER
			.comment("Ajoute les notes de recherche aux tables de butin des coffres de donjon.",
					"Peut être désactivé recherche par recherche dans la section Recherche.")
			.define("include_research_in_chests", true);

	public static final ModConfigSpec.DoubleValue ENERGY_PER_WORK_UNIT = BUILDER
			.comment("Énergie générée par tick de travail d'un ouvrier (valeur de base, ajustée par l'efficacité de chaque ouvrier).",
					"Une valeur égale ou inférieure à 0 désactive les ouvriers.")
			.defineInRange("energy_per_work_unit", 50.0D, 0.0D, Double.MAX_VALUE);

	/* options de recherche : identiques côté client et serveur */
	static {
		BUILDER.push("research");
	}

	public static final ModConfigSpec.BooleanValue USE_RESEARCH_SYSTEM = BUILDER
			.comment("Si désactivé, le système de recherche est désactivé et toutes les recettes sont disponibles dans l'établi normal.")
			.define("use_research_system", true);

	public static final ModConfigSpec.BooleanValue ENABLE_RESEARCH_RESOURCE_USE = BUILDER
			.comment("Si désactivé, la recherche ne consomme pas de ressources.")
			.define("use_research_resources", true);

	public static final ModConfigSpec.DoubleValue ENERGY_PER_RESEARCH_UNIT = BUILDER
			.comment("Énergie consommée par tick de recherche.",
					"0 supprime les besoins en énergie et en ouvriers pour la recherche.",
					"Une valeur supérieure à 1 augmente la durée et les ressources nécessaires à toute la recherche.")
			.defineInRange("energy_per_research_tick", 1.0D, 0.0D, Double.MAX_VALUE);

	static {
		BUILDER.pop();
	}

	public static final ModConfigSpec SPEC = BUILDER.build();
}

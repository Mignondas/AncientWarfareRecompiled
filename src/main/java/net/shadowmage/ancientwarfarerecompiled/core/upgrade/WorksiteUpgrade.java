package net.shadowmage.ancientwarfarerecompiled.core.upgrade;

import java.util.HashSet;
import java.util.Set;

/**
 * Améliorations d'une station de travail.
 *
 * ATTENTION : ne jamais changer l'ordre des constantes. Les améliorations sont restaurées par leur ordinal
 * (même règle qu'en 1.12).
 */
public enum WorksiteUpgrade {
	SIZE_MEDIUM(new int[] {}, new int[] {}), SIZE_LARGE(new int[] {0}, new int[] {0}), QUARRY_MEDIUM(new int[] {}, new int[] {}),
	QUARRY_LARGE(new int[] {2}, new int[] {2}), ENCHANTED_TOOLS_1(new int[] {}, new int[] {}), ENCHANTED_TOOLS_2(new int[] {4}, new int[] {4}),
	TOOL_QUALITY_1(new int[] {}, new int[] {}), TOOL_QUALITY_2(new int[] {6}, new int[] {6}), TOOL_QUALITY_3(new int[] {6, 7}, new int[] {6, 7}),
	BASIC_CHUNK_LOADER(new int[] {}, new int[] {}), QUARRY_CHUNK_LOADER(new int[] {}, new int[] {});

	private final Set<Integer> exclusive;
	private final Set<Integer> overrides;

	WorksiteUpgrade(int[] exc, int[] ovr) {
		this.exclusive = new HashSet<>(exc.length);
		this.overrides = new HashSet<>(ovr.length);
		for (int anExc : exc) {
			exclusive.add(anExc);
		}
		for (int anOvr : ovr) {
			overrides.add(anOvr);
		}
	}

	/*
	 * Clé de traduction de l'amélioration, pour les listes de l'interface.
	 * Le nom de l'objet « amélioration de station » sera défini avec le module Automation.
	 */
	public String unlocalizedName() {
		return "item.ancientwarfarerecompiled.worksite_upgrade." + ordinal();
	}

	/*
	 * Vrai si CETTE amélioration remplace l'entrée (l'entrée est retirée, celle-ci est appliquée).
	 */
	public boolean overrides(WorksiteUpgrade ug) {
		return overrides.contains(ug.ordinal());
	}

	/*
	 * Vrai si CETTE amélioration empêche l'entrée d'être appliquée (rien ne change).
	 */
	public boolean exclusive(WorksiteUpgrade ug) {
		return exclusive.contains(ug.ordinal());
	}
}

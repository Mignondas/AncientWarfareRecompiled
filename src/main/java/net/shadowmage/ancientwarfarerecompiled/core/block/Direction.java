package net.shadowmage.ancientwarfarerecompiled.core.block;

import javax.annotation.Nullable;

/**
 * Direction affichée dans l'interface (clés de traduction).
 *
 * Note : ce nom est le même que net.minecraft.core.Direction. Les fichiers qui utilisent les deux
 * désignent celle de Minecraft par son nom complet.
 */
public enum Direction {
	DOWN("guistrings.inventory.direction.down"),
	UP("guistrings.inventory.direction.up"),
	NORTH("guistrings.inventory.direction.north"),
	SOUTH("guistrings.inventory.direction.south"),
	WEST("guistrings.inventory.direction.west"),
	EAST("guistrings.inventory.direction.east"),
	UNKNOWN("guistrings.inventory.direction.unknown");

	private final String translationKey;

	Direction(String key) {
		translationKey = key;
	}

	public String getTranslationKey() {
		return translationKey;
	}

	/**
	 * Convertit une direction Minecraft. Les ordinaux sont identiques entre 1.12 et 1.21.1
	 * (DOWN, UP, NORTH, SOUTH, WEST, EAST), la conversion se fait donc par index.
	 */
	public static Direction getDirectionFor(@Nullable net.minecraft.core.Direction direction) {
		return direction == null ? UNKNOWN : values()[direction.ordinal()];
	}
}

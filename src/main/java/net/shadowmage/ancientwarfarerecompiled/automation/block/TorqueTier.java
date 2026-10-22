package net.shadowmage.ancientwarfarerecompiled.automation.block;

import java.util.Locale;

/**
 * Qualité des composants de couple : rendement et débit maximal par tick.
 * Valeurs par défaut du mod d'origine.
 */
public enum TorqueTier {
	LIGHT(0.99990d, 32.d),
	MEDIUM(0.99995d, 192.d),
	HEAVY(0.99999d, 1024.d);

	public final double efficiency;
	public final double transfer;

	TorqueTier(double efficiency, double transfer) {
		this.efficiency = efficiency;
		this.transfer = transfer;
	}

	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}
}

package net.shadowmage.ancientwarfarerecompiled.core.interfaces;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import net.shadowmage.ancientwarfarerecompiled.core.config.AWCoreConfig;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.ITorque.ITorqueTile;
import net.shadowmage.ancientwarfarerecompiled.core.owner.IOwnable;
import net.shadowmage.ancientwarfarerecompiled.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfarerecompiled.core.upgrade.WorksiteUpgrade;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

/**
 * Station de travail : lieu où les ouvriers accomplissent un travail (minage, culture, foresterie, artisanat, recherche).
 */
public interface IWorkSite extends ITorqueTile, IBlockBreakHandler, IOwnable {

	/*
	 * Les ouvriers doivent appeler cette méthode avant doWork() pour vérifier qu'il y a du travail à faire.
	 */
	boolean hasWork();

	/*
	 * Appelée par un ouvrier si hasWork() renvoie vrai.
	 */
	void addEnergyFromWorker(IWorker worker);

	void addEnergyFromPlayer(Player player);

	/*
	 * Appelée par les ouvriers pour valider le type de travail lors de IWorker.canWorkAt(IWorkSite).
	 * Les ouvriers doivent gérer eux-mêmes la liste des types de travail qu'ils acceptent.
	 */
	WorkType getWorkType();

	@Nullable
	Team getTeam();

	Set<WorksiteUpgrade> getUpgrades();

	Set<WorksiteUpgrade> getValidUpgrades();

	/*
	 * Ajoute l'amélioration à l'ensemble courant et applique ses bonus.
	 * Résultat indéfini si l'amélioration est déjà présente.
	 */
	void addUpgrade(WorksiteUpgrade upgrade);

	/*
	 * Retire l'amélioration de l'ensemble courant et supprime ses bonus.
	 * Résultat indéfini si l'amélioration n'est pas présente.
	 */
	void removeUpgrade(WorksiteUpgrade upgrade);

	enum WorkType {
		/*
		 * Ne jamais changer les noms : ils servent à sauvegarder l'expérience accumulée des PNJ.
		 */
		MINING("work_type.mining"), FARMING("work_type.farming"), FORESTRY("work_type.forestry"), CRAFTING("work_type.crafting"), RESEARCH("work_type.research"), NONE("work_type.none");

		public final String regName;

		WorkType(String regName) {
			this.regName = regName;
		}
	}

	/*
	 * Méthodes statiques pour la logique par défaut des stations de travail.
	 */
	final class WorksiteImplementation {

		private WorksiteImplementation() {}

		public static double getEnergyPerActivation(double efficiencyBonusFactor) {
			return AWCoreConfig.ENERGY_PER_WORK_UNIT.get() * 1.f - efficiencyBonusFactor;
		}

		public static double getEfficiencyFactor(EnumSet<WorksiteUpgrade> upgrades) {
			double efficiencyBonusFactor = 0.d;
			if (upgrades.contains(WorksiteUpgrade.ENCHANTED_TOOLS_1)) {
				efficiencyBonusFactor += 0.05;
			}
			if (upgrades.contains(WorksiteUpgrade.ENCHANTED_TOOLS_2)) {
				efficiencyBonusFactor += 0.1;
			}
			if (upgrades.contains(WorksiteUpgrade.TOOL_QUALITY_1)) {
				efficiencyBonusFactor += 0.05;
			}
			if (upgrades.contains(WorksiteUpgrade.TOOL_QUALITY_2)) {
				efficiencyBonusFactor += 0.15;
			}
			if (upgrades.contains(WorksiteUpgrade.TOOL_QUALITY_3)) {
				efficiencyBonusFactor += 0.25;
			}
			return efficiencyBonusFactor;
		}
	}
}

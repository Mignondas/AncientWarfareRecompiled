package net.shadowmage.ancientwarfarerecompiled.core.interfaces;

import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IWorkSite.WorkType;

/**
 * Entités capables de réaliser un travail sur une station de travail.
 */
public interface IWorker {

	/*
	 * Efficacité de l'ouvrier. Base = 1 ; plus la valeur est élevée, plus il est efficace pour la plupart des types de travail.
	 *
	 * @param type le type de travail à réaliser
	 */
	float getWorkEffectiveness(WorkType type);

	boolean canWorkAt(WorkType type);

	double getWorkRangeSq();
}

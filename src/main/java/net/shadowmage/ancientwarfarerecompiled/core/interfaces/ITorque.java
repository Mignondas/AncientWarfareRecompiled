package net.shadowmage.ancientwarfarerecompiled.core.interfaces;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

/**
 * Énergie mécanique (« torque ») : interfaces et réalisations de référence.
 *
 * Les directions sont celles de net.minecraft.core.Direction (EnumFacing en 1.12).
 */
public final class ITorque {
	private ITorque() {} // conteneur d'interfaces et de méthodes statiques, pas d'instance

	/*
	 * Interface des tuiles qui produisent, consomment ou transmettent de l'énergie mécanique.
	 * Les tuiles gèrent leur énergie comme elles le souhaitent, mais ne demandent pas d'énergie
	 * aux autres tuiles : celles-ci en fournissent d'elles-mêmes.
	 */
	public interface ITorqueTile {

		/*
		 * Quantité maximale d'énergie stockable côté donné.
		 */
		double getMaxTorque(@Nullable Direction from);

		/*
		 * Énergie accessible depuis le côté donné.
		 */
		double getTorqueStored(@Nullable Direction from);

		/*
		 * Ajoute de l'énergie côté donné, jusqu'à la quantité indiquée.
		 * Renvoie la quantité réellement ajoutée (0 si aucune).
		 */
		double addTorque(@Nullable Direction from, double energy);

		/*
		 * Retire de l'énergie côté donné, jusqu'à la quantité indiquée.
		 * Renvoie la quantité réellement retirée (0 si aucune).
		 */
		double drainTorque(Direction from, double energy);

		/*
		 * Quantité maximale que le côté donné peut fournir MAINTENANT.
		 * Équivalent des actions « simulées » des autres systèmes d'énergie.
		 */
		double getMaxTorqueOutput(Direction from);

		/*
		 * Quantité maximale que le côté donné peut accepter MAINTENANT.
		 * Équivalent des actions « simulées » des autres systèmes d'énergie.
		 */
		double getMaxTorqueInput(@Nullable Direction from);

		/*
		 * Vrai si cette tuile peut fournir de l'énergie depuis le côté donné.
		 * Sert à l'état de connexion. La valeur doit rester la même entre deux appels,
		 * sinon il faut déclencher une mise à jour des blocs voisins.
		 * Peut renvoyer vrai avec une sortie de 0 pour les côtés désactivables.
		 */
		boolean canOutputTorque(Direction from);

		/*
		 * Vrai si cette tuile peut recevoir de l'énergie par le côté donné.
		 * Même règles que canOutputTorque.
		 */
		boolean canInputTorque(Direction from);

		/*
		 * Utilisé par le client pour le rendu. Si vrai, le voisin utilise la rotation de sortie
		 * de cette tuile pour afficher le côté d'entrée correspondant.
		 */
		boolean useOutputRotation(@Nullable Direction from);

		/*
		 * Rotation de l'arbre de sortie pour le côté donné. Appelé seulement si useOutputRotation(from) est vrai.
		 */
		float getClientOutputRotation(Direction from, float delta);
	}

	/*
	 * Réalisation de référence d'une cellule d'énergie, à utiliser par délégation dans une tuile.
	 * Une tuile peut en avoir une ou plusieurs, ou gérer son énergie autrement.
	 */
	public static class TorqueCell {
		double maxInput;
		double maxOutput;
		double maxEnergy;
		double efficiency;
		protected double energy;

		public TorqueCell(double in, double out, double max, double eff) {
			maxInput = in;
			maxOutput = out;
			maxEnergy = max;
			efficiency = eff;
		}

		public double getEfficiency() {
			return efficiency;
		}

		public double getEnergy() {
			return energy;
		}

		public double getMaxEnergy() {
			return maxEnergy;
		}

		public void setEnergy(double in) {
			energy = Math.max(0, Math.min(in, maxEnergy));
		}

		public double addEnergy(double in) {
			if (Double.isNaN(in)) {
				throw new IllegalArgumentException("L'énergie demandée ne peut pas être NaN");
			}
			in = Math.min(getMaxTickInput(), in);
			energy += in;
			return in;
		}

		public double drainEnergy(double request) {
			if (Double.isNaN(request)) {
				throw new IllegalArgumentException("La quantité d'énergie retirée ne peut pas être NaN");
			}
			request = Math.min(getMaxTickOutput(), request);
			energy -= request;
			return request;
		}

		public double getMaxInput() {
			return maxInput;
		}

		public double getMaxOutput() {
			return maxOutput;
		}

		public double getMaxTickInput() {
			return Math.min(maxInput, getMaxEnergy() - getEnergy());
		}

		public double getMaxTickOutput() {
			return Math.min(maxOutput, getEnergy());
		}

		public CompoundTag writeToNBT(CompoundTag tag) {
			tag.putDouble("energy", energy);
			return tag;
		}

		public void readFromNBT(CompoundTag tag) {
			energy = tag.getDouble("energy");
		}

		public double getPercentFull() {
			return maxEnergy > 0.d ? energy / maxEnergy : 0.d;
		}
	}

	/*
	 * Cellule d'énergie liée à un côté de la tuile propriétaire. Utilisée par les tuiles multi-entrées/sorties
	 * (conduit, distributeur) pour garder une cellule par côté, sans en recréer à chaque changement d'orientation.
	 * Attention : la cellule conserve son énergie quelle que soit l'orientation ; un nouveau côté d'entrée peut donc
	 * hériter de l'énergie d'un ancien côté de sortie.
	 */
	public static class SidedTorqueCell extends TorqueCell {

		Direction dir;
		ITorqueTile owner;

		public SidedTorqueCell(double in, double out, double max, double eff, Direction dir, ITorqueTile owner) {
			super(in, out, max, eff);
			this.dir = dir;
			this.owner = owner;
		}

		@Override
		public double getMaxTickInput() {
			return owner.canInputTorque(dir) ? super.getMaxTickInput() : 0;
		}

		@Override
		public double getMaxTickOutput() {
			return owner.canOutputTorque(dir) ? super.getMaxTickOutput() : 0;
		}

		@Override
		public double addEnergy(double in) {
			return owner.canInputTorque(dir) ? super.addEnergy(in) : 0;
		}

		@Override
		public double drainEnergy(double request) {
			return owner.canOutputTorque(dir) ? super.drainEnergy(request) : 0;
		}
	}
}

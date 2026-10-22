package net.shadowmage.ancientwarfarerecompiled.automation.tile.torque;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.ITorque.ITorqueTile;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.ITorque.SidedTorqueCell;

import javax.annotation.Nullable;

/**
 * Tuile à six cellules de couple, une par face. Chaque cellule gère l'entrée ou la sortie de sa face.
 * Base du distributeur (et des jonctions à venir).
 */
public abstract class TileTorqueSidedCell extends TileTorqueBase {
	protected static final Direction[] DIRS = Direction.values();

	protected final SidedTorqueCell[] storage = new SidedTorqueCell[DIRS.length];

	protected TileTorqueSidedCell(BlockEntityType<?> type, BlockPos pos, BlockState state, double maxTransfer, double efficiency) {
		super(type, pos, state);
		for (int i = 0; i < storage.length; i++) {
			storage[i] = new SidedTorqueCell(maxTransfer, maxTransfer, maxTransfer, efficiency, DIRS[i], this);
		}
	}

	public void serverTick() {
		torqueIn = getTotalTorque() - prevEnergy;
		balanceStorage();
		torqueLoss = applyPowerLoss();
		torqueOut = transferPower();
		prevEnergy = getTotalTorque();
	}

	protected double applyPowerLoss() {
		double loss = 0;
		for (SidedTorqueCell cell : storage) {
			loss += applyPowerDrain(cell);
		}
		return loss;
	}

	/** Par défaut (jonction) : la sortie est la face FACING, les autres faces lui envoient leur couple. */
	protected double transferPower() {
		return transferPowerTo(getPrimaryFacing());
	}

	protected void balanceStorage() {
		int face = getPrimaryFacing().ordinal();
		SidedTorqueCell out = storage[face];
		double total = 0;
		for (int i = 0; i < storage.length; i++) {
			if (i != face) {
				total += storage[i].getEnergy();
			}
		}
		if (total > 0) {
			double transfer = Math.min(total, out.getMaxEnergy() - out.getEnergy());
			double percent = transfer / total;
			double moved = 0;
			for (int i = 0; i < storage.length; i++) {
				if (i == face) {
					continue;
				}
				double fromEach = storage[i].getEnergy() * percent;
				moved += fromEach;
				storage[i].setEnergy(storage[i].getEnergy() - fromEach);
			}
			out.setEnergy(out.getEnergy() + moved);
		}
	}

	/** Faces connectées à un voisin qui peut recevoir ou fournir du couple dans le bon sens. */
	protected boolean[] getConnections() {
		boolean[] connected = new boolean[DIRS.length];
		for (int i = 0; i < DIRS.length; i++) {
			Direction dir = DIRS[i];
			ITorqueTile neighbor = neighbor(dir);
			if (neighbor != null) {
				connected[i] = (neighbor.canInputTorque(dir.getOpposite()) && canOutputTorque(dir))
						|| (neighbor.canOutputTorque(dir.getOpposite()) && canInputTorque(dir));
			}
		}
		return connected;
	}

	@Override
	public boolean canOutputTorque(Direction towards) {
		return towards == getPrimaryFacing();
	}

	@Override
	public boolean canInputTorque(@Nullable Direction from) {
		return from != null && from != getPrimaryFacing();
	}

	@Override
	public double getMaxTorque(@Nullable Direction from) {
		return from == null ? 0 : storage[from.ordinal()].getMaxEnergy();
	}

	@Override
	public double getTorqueStored(@Nullable Direction from) {
		return from == null ? 0 : storage[from.ordinal()].getEnergy();
	}

	@Override
	public double addTorque(@Nullable Direction from, double energy) {
		return from == null ? 0 : storage[from.ordinal()].addEnergy(energy);
	}

	@Override
	public double drainTorque(Direction from, double energy) {
		return storage[from.ordinal()].drainEnergy(energy);
	}

	@Override
	public double getMaxTorqueOutput(Direction from) {
		return storage[from.ordinal()].getMaxTickOutput();
	}

	@Override
	public double getMaxTorqueInput(@Nullable Direction from) {
		return from == null ? 0 : storage[from.ordinal()].getMaxTickInput();
	}

	@Override
	protected double getTotalTorque() {
		double total = 0;
		for (Direction dir : DIRS) {
			if (canInputTorque(dir) || canOutputTorque(dir)) {
				total += storage[dir.ordinal()].getEnergy();
			}
		}
		return total;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		for (int i = 0; i < storage.length; i++) {
			tag.put("side" + i, storage[i].writeToNBT(new CompoundTag()));
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		for (int i = 0; i < storage.length; i++) {
			if (tag.contains("side" + i)) {
				storage[i].readFromNBT(tag.getCompound("side" + i));
			}
		}
	}
}

package net.shadowmage.ancientwarfarerecompiled.automation.tile.torque;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfarerecompiled.automation.block.TorqueTier;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;
import net.shadowmage.ancientwarfarerecompiled.core.config.AWCoreConfig;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.ITorque.TorqueCell;

import javax.annotation.Nullable;

/**
 * Générateur à manivelle : un joueur tourne la manivelle (clic), l'énergie est stockée puis transmise sur la face FACING.
 */
public class TileHandCrankedGenerator extends TileTorqueBase {
	private static final String OUTPUT_TAG = "torqueEnergy";
	private static final String INPUT_TAG = "inputEnergy";
	private static final double MAX_BALANCE_PER_TICK = 2.d;

	private final TorqueCell torqueCell;
	private final TorqueCell inputCell;

	public TileHandCrankedGenerator(BlockPos pos, BlockState state) {
		super(AWAutomationTileTypes.HAND_CRANKED_GENERATOR.get(), pos, state);
		double eff = TorqueTier.LIGHT.efficiency;
		torqueCell = new TorqueCell(0, 32, 32, eff);
		inputCell = new TorqueCell(32, 0, 150, eff);
	}

	public void serverTick() {
		torqueIn = torqueCell.getEnergy() - prevEnergy;
		balancePower();
		torqueOut = transferPowerTo(getPrimaryFacing());
		torqueLoss = applyPowerDrain(torqueCell) + applyPowerDrain(inputCell);
		prevEnergy = torqueCell.getEnergy();
	}

	private void balancePower() {
		double trans = Math.min(MAX_BALANCE_PER_TICK, torqueCell.getMaxEnergy() - torqueCell.getEnergy());
		trans = Math.min(trans, inputCell.getEnergy());
		inputCell.setEnergy(inputCell.getEnergy() - trans);
		torqueCell.setEnergy(torqueCell.getEnergy() + trans);
	}

	/** Un tour de manivelle par clic. */
	public void crank() {
		inputCell.setEnergy(inputCell.getEnergy() + AWCoreConfig.ENERGY_PER_WORK_UNIT.get());
		setChanged();
	}

	@Override
	public boolean canOutputTorque(Direction towards) {
		return towards == getPrimaryFacing();
	}

	@Override
	public boolean canInputTorque(@Nullable Direction from) {
		return false;
	}

	@Override
	public double getMaxTorque(@Nullable Direction from) {
		return inputCell.getMaxEnergy() + torqueCell.getMaxEnergy();
	}

	@Override
	public double getTorqueStored(@Nullable Direction from) {
		return inputCell.getEnergy() + torqueCell.getEnergy();
	}

	@Override
	public double addTorque(@Nullable Direction from, double energy) {
		if (from == getPrimaryFacing()) {
			return 0;
		}
		if (from == null || from == Direction.UP) {
			return inputCell.addEnergy(energy);
		}
		return 0;
	}

	@Override
	public double drainTorque(Direction from, double energy) {
		return from == getPrimaryFacing() ? torqueCell.drainEnergy(energy) : 0;
	}

	@Override
	public double getMaxTorqueOutput(Direction from) {
		return from == getPrimaryFacing() ? torqueCell.getMaxTickOutput() : 0;
	}

	@Override
	public double getMaxTorqueInput(@Nullable Direction from) {
		return 0;
	}

	@Override
	protected double getTotalTorque() {
		return inputCell.getEnergy() + torqueCell.getEnergy();
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putDouble(OUTPUT_TAG, torqueCell.getEnergy());
		tag.putDouble(INPUT_TAG, inputCell.getEnergy());
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		torqueCell.setEnergy(tag.getDouble(OUTPUT_TAG));
		inputCell.setEnergy(tag.getDouble(INPUT_TAG));
	}
}

package net.shadowmage.ancientwarfarerecompiled.automation.tile.torque;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfarerecompiled.automation.block.TorqueTier;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;
import net.shadowmage.ancientwarfarerecompiled.automation.tile.TileWindmillBlade;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.ITorque.TorqueCell;

import javax.annotation.Nullable;

/**
 * Générateur éolien : reçoit l'énergie du contrôleur de pales placé derrière lui, et la sort sur la face FACING.
 */
public class TileWindmillController extends TileTorqueBase {
	private static final String OUTPUT_TAG = "torqueEnergy";

	private final TorqueCell torqueCell;

	public TileWindmillController(BlockPos pos, BlockState state) {
		super(AWAutomationTileTypes.WINDMILL_CONTROLLER.get(), pos, state);
		double max = TorqueTier.LIGHT.transfer;
		torqueCell = new TorqueCell(max, max, max, TorqueTier.LIGHT.efficiency);
	}

	public void serverTick() {
		torqueIn = torqueCell.getEnergy() - prevEnergy;
		TileWindmillBlade blade = controlledBlade();
		if (blade != null && blade.getEnergy() > 0) {
			torqueCell.addEnergy(blade.drainEnergy());
		}
		torqueLoss = applyPowerDrain(torqueCell);
		torqueOut = transferPowerTo(getPrimaryFacing());
		prevEnergy = torqueCell.getEnergy();
	}

	@Nullable
	private TileWindmillBlade controlledBlade() {
		if (level == null) {
			return null;
		}
		BlockPos behind = worldPosition.relative(getPrimaryFacing().getOpposite());
		if (!level.hasChunkAt(behind)) {
			return null;
		}
		BlockEntity be = level.getBlockEntity(behind);
		return be instanceof TileWindmillBlade blade && blade.isControl() ? blade : null;
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
		return torqueCell.getMaxEnergy();
	}

	@Override
	public double getTorqueStored(@Nullable Direction from) {
		return torqueCell.getEnergy();
	}

	@Override
	public double addTorque(@Nullable Direction from, double energy) {
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
		return torqueCell.getEnergy();
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putDouble(OUTPUT_TAG, torqueCell.getEnergy());
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		torqueCell.setEnergy(tag.getDouble(OUTPUT_TAG));
	}
}

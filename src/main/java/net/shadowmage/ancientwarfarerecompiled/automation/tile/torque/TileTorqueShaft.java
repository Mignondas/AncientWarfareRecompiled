package net.shadowmage.ancientwarfarerecompiled.automation.tile.torque;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfarerecompiled.automation.block.BlockTorqueShaft;
import net.shadowmage.ancientwarfarerecompiled.automation.block.TorqueTier;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.ITorque.TorqueCell;

import javax.annotation.Nullable;

/**
 * Arbre de couple : une cellule, qui laisse passer le couple de l'arrière vers l'avant (face FACING).
 */
public class TileTorqueShaft extends TileTorqueBase {
	private static final String ENERGY_TAG = "torqueEnergy";

	private final TorqueCell cell;

	public TileTorqueShaft(BlockPos pos, BlockState state) {
		super(AWAutomationTileTypes.TORQUE_SHAFT.get(), pos, state);
		TorqueTier tier = state.getBlock() instanceof BlockTorqueShaft shaft ? shaft.getTier() : TorqueTier.LIGHT;
		cell = new TorqueCell(tier.transfer, tier.transfer, tier.transfer, tier.efficiency);
	}

	public void serverTick() {
		torqueIn = cell.getEnergy() - prevEnergy;
		torqueLoss = applyPowerDrain(cell);
		torqueOut = transferPowerTo(getPrimaryFacing());
		prevEnergy = cell.getEnergy();
	}

	@Override
	public boolean canOutputTorque(Direction towards) {
		return towards == getPrimaryFacing();
	}

	@Override
	public boolean canInputTorque(@Nullable Direction from) {
		return from != null && from == getPrimaryFacing().getOpposite();
	}

	@Override
	public double getMaxTorque(@Nullable Direction from) {
		return cell.getMaxEnergy();
	}

	@Override
	public double getTorqueStored(@Nullable Direction from) {
		return cell.getEnergy();
	}

	@Override
	public double addTorque(@Nullable Direction from, double energy) {
		return canInputTorque(from) ? cell.addEnergy(energy) : 0;
	}

	@Override
	public double drainTorque(Direction from, double energy) {
		return canOutputTorque(from) ? cell.drainEnergy(energy) : 0;
	}

	@Override
	public double getMaxTorqueOutput(Direction from) {
		return canOutputTorque(from) ? cell.getMaxTickOutput() : 0;
	}

	@Override
	public double getMaxTorqueInput(@Nullable Direction from) {
		return canInputTorque(from) ? cell.getMaxTickInput() : 0;
	}

	@Override
	protected double getTotalTorque() {
		return cell.getEnergy();
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putDouble(ENERGY_TAG, cell.getEnergy());
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		cell.setEnergy(tag.getDouble(ENERGY_TAG));
	}
}

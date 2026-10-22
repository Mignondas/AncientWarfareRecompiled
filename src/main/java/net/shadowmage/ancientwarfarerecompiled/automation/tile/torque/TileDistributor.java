package net.shadowmage.ancientwarfarerecompiled.automation.tile.torque;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfarerecompiled.automation.block.BlockTorqueDistributor;
import net.shadowmage.ancientwarfarerecompiled.automation.block.TorqueTier;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.ITorque.SidedTorqueCell;

import javax.annotation.Nullable;

/**
 * Distributeur de couple : reçoit par la face arrière (opposée à FACING) et répartit sur les autres faces connectées.
 */
public class TileDistributor extends TileTorqueSidedCell {

	public TileDistributor(BlockPos pos, BlockState state) {
		super(AWAutomationTileTypes.TORQUE_DISTRIBUTOR.get(), pos, state, tierOf(state).transfer, tierOf(state).efficiency);
	}

	private static TorqueTier tierOf(BlockState state) {
		return state.getBlock() instanceof BlockTorqueDistributor distributor ? distributor.getTier() : TorqueTier.LIGHT;
	}

	@Override
	protected double transferPower() {
		Direction in = getPrimaryFacing().getOpposite();
		double out = 0;
		for (Direction dir : DIRS) {
			if (dir == in) {
				continue;
			}
			out += transferPowerTo(dir);
		}
		return out;
	}

	@Override
	protected void balanceStorage() {
		Direction in = getPrimaryFacing().getOpposite();
		SidedTorqueCell input = storage[in.ordinal()];
		boolean[] connected = getConnections();

		double totalRequested = 0;
		for (Direction dir : DIRS) {
			int i = dir.ordinal();
			if (dir == in || !connected[i]) {
				continue;
			}
			totalRequested += storage[i].getMaxEnergy() - storage[i].getEnergy();
		}
		if (totalRequested > 0 && input.getEnergy() > 0) {
			double transfer = Math.min(totalRequested, input.getEnergy());
			double percent = transfer / totalRequested;
			double transferred = 0;
			for (Direction dir : DIRS) {
				int i = dir.ordinal();
				if (dir == in || !connected[i]) {
					continue;
				}
				SidedTorqueCell out = storage[i];
				double request = out.getMaxEnergy() - out.getEnergy();
				double trans = request * percent;
				transferred += trans;
				out.setEnergy(out.getEnergy() + trans);
			}
			input.setEnergy(input.getEnergy() - transferred);
		}
	}

	@Override
	public boolean canInputTorque(@Nullable Direction from) {
		return from != null && from == getPrimaryFacing().getOpposite();
	}

	@Override
	public boolean canOutputTorque(Direction towards) {
		return towards != getPrimaryFacing().getOpposite();
	}
}

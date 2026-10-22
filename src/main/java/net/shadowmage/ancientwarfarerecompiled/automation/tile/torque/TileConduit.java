package net.shadowmage.ancientwarfarerecompiled.automation.tile.torque;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfarerecompiled.automation.block.BlockTorqueJunction;
import net.shadowmage.ancientwarfarerecompiled.automation.block.TorqueTier;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;

/**
 * Jonction de couple : une seule sortie (FACING), alimentée par les autres faces. Comportement de base de la cellule à six faces.
 */
public class TileConduit extends TileTorqueSidedCell {

	public TileConduit(BlockPos pos, BlockState state) {
		super(AWAutomationTileTypes.TORQUE_JUNCTION.get(), pos, state, tierOf(state).transfer, tierOf(state).efficiency);
	}

	private static TorqueTier tierOf(BlockState state) {
		return state.getBlock() instanceof BlockTorqueJunction junction ? junction.getTier() : TorqueTier.LIGHT;
	}
}

package net.shadowmage.ancientwarfarerecompiled.automation.tile.torque;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.ITorque.ITorqueTile;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.ITorque.TorqueCell;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Base commune des tuiles de couple : voisinage, transfert vers la face de sortie, pertes.
 * La sortie est la face « FACING » du bloc.
 */
public abstract class TileTorqueBase extends BlockEntity implements ITorqueTile {
	protected double torqueIn;
	protected double torqueOut;
	protected double torqueLoss;
	protected double prevEnergy;

	protected TileTorqueBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public Direction getPrimaryFacing() {
		return getBlockState().getValue(BlockStateProperties.FACING);
	}

	@Nullable
	protected ITorqueTile neighbor(Direction dir) {
		if (level == null) {
			return null;
		}
		BlockPos offset = worldPosition.relative(dir);
		if (!level.isLoaded(offset)) {
			return null;
		}
		return level.getBlockEntity(offset) instanceof ITorqueTile tile ? tile : null;
	}

	/** Envoie au voisin de la face donnée ce que cette tuile peut sortir, et retire ce qui a été accepté. */
	protected final double transferPowerTo(Direction from) {
		ITorqueTile target = neighbor(from);
		if (target == null || !target.canInputTorque(from.getOpposite())) {
			return 0;
		}
		return drainTorque(from, target.addTorque(from.getOpposite(), getMaxTorqueOutput(from)));
	}

	protected static double applyPowerDrain(TorqueCell cell) {
		double e = cell.getEnergy();
		cell.setEnergy(e * cell.getEfficiency());
		return cell.getEnergy() - e;
	}

	/** Énergie totale stockée, pour l'affichage. */
	protected abstract double getTotalTorque();

	/** Affiche les valeurs du tick dans la barre d'action (clic sur le bloc). */
	public void showValues(Player player) {
		player.displayClientMessage(Component.translatable("guistrings.automation.torque.values",
				fmt(getTotalTorque()), fmt(torqueIn), fmt(torqueOut), fmt(torqueLoss)), true);
	}

	private static String fmt(double value) {
		return String.format(Locale.ROOT, "%.2f", value);
	}
}

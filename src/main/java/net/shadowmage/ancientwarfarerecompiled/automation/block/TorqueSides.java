package net.shadowmage.ancientwarfarerecompiled.automation.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Propriétés « connecté » des six faces, utilisées par les distributeurs et jonctions pour afficher les bras.
 */
public final class TorqueSides {
	private TorqueSides() {}

	public static BooleanProperty of(Direction dir) {
		return switch (dir) {
			case NORTH -> BlockStateProperties.NORTH;
			case SOUTH -> BlockStateProperties.SOUTH;
			case EAST -> BlockStateProperties.EAST;
			case WEST -> BlockStateProperties.WEST;
			case UP -> BlockStateProperties.UP;
			case DOWN -> BlockStateProperties.DOWN;
		};
	}

	public static void addProperties(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.NORTH, BlockStateProperties.SOUTH, BlockStateProperties.EAST,
				BlockStateProperties.WEST, BlockStateProperties.UP, BlockStateProperties.DOWN);
	}

	public static BlockState withDefaults(BlockState state) {
		BlockState result = state;
		for (Direction dir : Direction.values()) {
			result = result.setValue(of(dir), false);
		}
		return result;
	}

	/** Met à jour les six propriétés ; tableau indexé comme Direction.values(). */
	public static BlockState withConnections(BlockState state, boolean[] connected) {
		BlockState result = state;
		Direction[] dirs = Direction.values();
		for (int i = 0; i < dirs.length; i++) {
			result = result.setValue(of(dirs[i]), connected[i]);
		}
		return result;
	}
}

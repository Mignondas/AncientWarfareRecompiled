package net.shadowmage.ancientwarfarerecompiled.automation.block;

import net.minecraft.core.Direction;

/**
 * Rotation de 90° d'une direction autour d'un axe (utilisée par le marteau pour orienter les blocs de couple).
 */
public final class TorqueRotation {
	private TorqueRotation() {}

	public static Direction rotateAround(Direction dir, Direction.Axis axis) {
		if (dir.getAxis() == axis) {
			return dir;
		}
		int x = dir.getStepX();
		int y = dir.getStepY();
		int z = dir.getStepZ();
		int nx;
		int ny;
		int nz;
		switch (axis) {
			case X -> {
				nx = x;
				ny = -z;
				nz = y;
			}
			case Y -> {
				nx = z;
				ny = y;
				nz = -x;
			}
			default -> {
				nx = -y;
				ny = x;
				nz = z;
			}
		}
		for (Direction candidate : Direction.values()) {
			if (candidate.getStepX() == nx && candidate.getStepY() == ny && candidate.getStepZ() == nz) {
				return candidate;
			}
		}
		return dir;
	}
}

package net.shadowmage.ancientwarfarerecompiled.core.block;

import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Gestion de l'orientation des blocs et de leurs faces relatives.
 *
 * Les directions sont celles de net.minecraft.core.Direction, désignées par leur nom complet
 * pour éviter la collision avec {@link Direction}.
 */
public class BlockRotationHandler {
	private BlockRotationHandler() {}

	public static net.minecraft.core.Direction getFaceForPlacement(LivingEntity entity, IRotatableBlock block, net.minecraft.core.Direction sideHit) {
		if (block.getRotationType() == RotationType.NONE) {
			return net.minecraft.core.Direction.NORTH;
		}
		net.minecraft.core.Direction facing = entity.getDirection();
		if (block.getRotationType() == RotationType.SIX_WAY && sideHit.getAxis() == net.minecraft.core.Direction.Axis.Y) {
			facing = sideHit.getOpposite();
		}
		if (block.invertFacing()) {
			facing = facing.getOpposite();
		}
		return facing;
	}

	public interface IRotatableBlock {
		RotationType getRotationType();

		boolean invertFacing();
	}

	public interface IRotatableTile {
		net.minecraft.core.Direction getPrimaryFacing();

		void setPrimaryFacing(net.minecraft.core.Direction face);
	}

	public enum RotationType {
		/*
		 * Peut avoir 6 textures / inventaires.
		 * Haut, bas, avant, arrière, gauche, droite.
		 * Ne peut regarder que dans quatre directions : N/S/E/O.
		 */
		FOUR_WAY(EnumSet.of(RelativeSide.TOP, RelativeSide.BOTTOM, RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.FRONT, RelativeSide.REAR)),
		/*
		 * Peut avoir 3 textures / inventaires : haut, bas, côtés.
		 * Peut regarder dans n'importe quelle direction : haut/bas/N/S/E/O.
		 */
		SIX_WAY(EnumSet.of(RelativeSide.TOP, RelativeSide.BOTTOM, RelativeSide.ANY_SIDE)),
		/*
		 * Pas de rotation. Des faces relatives sont possibles, mais FRONT vaut toujours NORD.
		 */
		NONE(EnumSet.of(RelativeSide.TOP, RelativeSide.BOTTOM, RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.FRONT, RelativeSide.REAR));

		private final EnumSet<RelativeSide> validSides;

		RotationType(EnumSet<RelativeSide> sides) {
			validSides = sides;
		}

		public Set<RelativeSide> getValidSides() {
			return validSides;
		}
	}

	public enum RelativeSide {
		TOP("guistrings.inventory.side.top"),
		BOTTOM("guistrings.inventory.side.bottom"),
		FRONT("guistrings.inventory.side.front"),
		REAR("guistrings.inventory.side.rear"),
		LEFT("guistrings.inventory.side.left"),
		RIGHT("guistrings.inventory.side.right"),
		ANY_SIDE("guistrings.inventory.side.all_sides"),
		NONE("guistrings.inventory.side.none");

		private static final int DOWN = net.minecraft.core.Direction.DOWN.ordinal();
		private static final int UP = net.minecraft.core.Direction.UP.ordinal();
		private static final int NORTH = net.minecraft.core.Direction.NORTH.ordinal();
		private static final int SOUTH = net.minecraft.core.Direction.SOUTH.ordinal();
		private static final int WEST = net.minecraft.core.Direction.WEST.ordinal();
		private static final int EAST = net.minecraft.core.Direction.EAST.ordinal();

		//[côté vu][orientation du bloc] = côté relatif vu
		protected static final RelativeSide[][] sixWayMap = new RelativeSide[6][6];
		//[côté vu][orientation du bloc] = côté relatif vu
		protected static final RelativeSide[][] fourWayMap = new RelativeSide[6][6];

		static {
			//D,U,N,S,W,E
			//fourWayMap[X][0-1] NE DOIT JAMAIS ÊTRE LU, LE BLOC NE PEUT PAS POINTER VERS LE HAUT/BAS
			sixWayMap[DOWN][DOWN] = TOP;
			sixWayMap[DOWN][UP] = BOTTOM;
			sixWayMap[DOWN][NORTH] = ANY_SIDE;
			sixWayMap[DOWN][SOUTH] = ANY_SIDE;
			sixWayMap[DOWN][WEST] = ANY_SIDE;
			sixWayMap[DOWN][EAST] = ANY_SIDE;

			sixWayMap[UP][DOWN] = BOTTOM;
			sixWayMap[UP][UP] = TOP;
			sixWayMap[UP][NORTH] = ANY_SIDE;
			sixWayMap[UP][SOUTH] = ANY_SIDE;
			sixWayMap[UP][WEST] = ANY_SIDE;
			sixWayMap[UP][EAST] = ANY_SIDE;

			sixWayMap[NORTH][DOWN] = ANY_SIDE;
			sixWayMap[NORTH][UP] = ANY_SIDE;
			sixWayMap[NORTH][NORTH] = TOP;
			sixWayMap[NORTH][SOUTH] = BOTTOM;
			sixWayMap[NORTH][WEST] = ANY_SIDE;
			sixWayMap[NORTH][EAST] = ANY_SIDE;

			sixWayMap[SOUTH][DOWN] = ANY_SIDE;
			sixWayMap[SOUTH][UP] = ANY_SIDE;
			sixWayMap[SOUTH][NORTH] = BOTTOM;
			sixWayMap[SOUTH][SOUTH] = TOP;
			sixWayMap[SOUTH][WEST] = ANY_SIDE;
			sixWayMap[SOUTH][EAST] = ANY_SIDE;

			sixWayMap[WEST][DOWN] = ANY_SIDE;
			sixWayMap[WEST][UP] = ANY_SIDE;
			sixWayMap[WEST][NORTH] = ANY_SIDE;
			sixWayMap[WEST][SOUTH] = ANY_SIDE;
			sixWayMap[WEST][WEST] = TOP;
			sixWayMap[WEST][EAST] = BOTTOM;

			sixWayMap[EAST][DOWN] = ANY_SIDE;
			sixWayMap[EAST][UP] = ANY_SIDE;
			sixWayMap[EAST][NORTH] = ANY_SIDE;
			sixWayMap[EAST][SOUTH] = ANY_SIDE;
			sixWayMap[EAST][WEST] = BOTTOM;
			sixWayMap[EAST][EAST] = TOP;

			fourWayMap[DOWN][DOWN] = ANY_SIDE;
			fourWayMap[DOWN][UP] = ANY_SIDE;
			fourWayMap[DOWN][NORTH] = BOTTOM;
			fourWayMap[DOWN][SOUTH] = BOTTOM;
			fourWayMap[DOWN][WEST] = BOTTOM;
			fourWayMap[DOWN][EAST] = BOTTOM;

			fourWayMap[UP][DOWN] = ANY_SIDE;
			fourWayMap[UP][UP] = ANY_SIDE;
			fourWayMap[UP][NORTH] = TOP;
			fourWayMap[UP][SOUTH] = TOP;
			fourWayMap[UP][WEST] = TOP;
			fourWayMap[UP][EAST] = TOP;

			fourWayMap[NORTH][DOWN] = ANY_SIDE;
			fourWayMap[NORTH][UP] = ANY_SIDE;
			fourWayMap[NORTH][NORTH] = FRONT;
			fourWayMap[NORTH][SOUTH] = REAR;
			fourWayMap[NORTH][WEST] = RIGHT;
			fourWayMap[NORTH][EAST] = LEFT;

			fourWayMap[SOUTH][DOWN] = ANY_SIDE;
			fourWayMap[SOUTH][UP] = ANY_SIDE;
			fourWayMap[SOUTH][NORTH] = REAR;
			fourWayMap[SOUTH][SOUTH] = FRONT;
			fourWayMap[SOUTH][WEST] = LEFT;
			fourWayMap[SOUTH][EAST] = RIGHT;

			fourWayMap[WEST][DOWN] = ANY_SIDE;
			fourWayMap[WEST][UP] = ANY_SIDE;
			fourWayMap[WEST][NORTH] = LEFT;
			fourWayMap[WEST][SOUTH] = RIGHT;
			fourWayMap[WEST][WEST] = FRONT;
			fourWayMap[WEST][EAST] = REAR;

			fourWayMap[EAST][DOWN] = ANY_SIDE;
			fourWayMap[EAST][UP] = ANY_SIDE;
			fourWayMap[EAST][NORTH] = RIGHT;
			fourWayMap[EAST][SOUTH] = LEFT;
			fourWayMap[EAST][WEST] = REAR;
			fourWayMap[EAST][EAST] = FRONT;
		}

		private final String key;

		RelativeSide(String key) {
			this.key = key;
		}

		public String getTranslationKey() {
			return key;
		}

		public static RelativeSide getSideViewed(RotationType t, net.minecraft.core.Direction facing, @Nullable net.minecraft.core.Direction side) {
			if (side != null) {
				if (t == RotationType.FOUR_WAY) {
					return fourWayMap[side.ordinal()][facing.ordinal()];
				} else if (t == RotationType.SIX_WAY) {
					return sixWayMap[side.ordinal()][facing.ordinal()];
				}
			}
			return ANY_SIDE;
		}

		@Nullable
		public static net.minecraft.core.Direction getMCSideToAccess(RotationType t, net.minecraft.core.Direction facing, RelativeSide access) {
			RelativeSide[][] map = t == RotationType.FOUR_WAY ? fourWayMap : sixWayMap;
			for (int x = 0; x < map.length; x++) {
				if (map[x][facing.ordinal()] == access) {
					return net.minecraft.core.Direction.values()[x];
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}

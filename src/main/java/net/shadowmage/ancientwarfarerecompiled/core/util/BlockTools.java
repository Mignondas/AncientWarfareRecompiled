package net.shadowmage.ancientwarfarerecompiled.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

/**
 * Utilitaires géométriques sur les positions de blocs.
 *
 * Portage partiel : les méthodes qui interagissent avec le monde (casser ou placer des blocs,
 * raycast, notifications) dépendent d'AWFakePlayer et de la configuration, elles seront
 * portées dans une étape ultérieure.
 */
public class BlockTools {
	private BlockTools() {}

	/**
	 * Fait pivoter un décalage horizontal (x, z) autour de l'origine, par pas de 90°.
	 */
	public static BlockPos rotateHorizontal(BlockPos offset, int turns) {
		BlockPos ret = offset;
		for (int i = 0; i < turns; i++) {
			ret = new BlockPos(-ret.getZ(), ret.getY(), ret.getX());
		}
		return ret;
	}

	/*
	 * fait pivoter un décalage flottant (-1 <= x <= 1) à l'intérieur d'un bloc, sur l'axe X
	 */
	public static float rotateFloatX(float x, float z, int turns) {
		float x1 = x;
		float z1 = z;
		for (int i = 0; i < turns; i++) {
			z = x1;
			x = 1.f - z1;
			x1 = x;
			z1 = z;
		}
		return x;
	}

	/*
	 * fait pivoter un décalage flottant (-1 <= z <= 1) à l'intérieur d'un bloc, sur l'axe Z
	 */
	public static float rotateFloatZ(float x, float z, int turns) {
		float x1 = x;
		float z1 = z;
		for (int i = 0; i < turns; i++) {
			z = x1;
			x = 1.f - z1;
			x1 = x;
			z1 = z;
		}
		return z;
	}

	public static BlockPos getAverageOf(BlockPos... positions) {
		float x = 0;
		float y = 0;
		float z = 0;
		int count = 0;
		for (BlockPos pos : positions) {
			x += pos.getX();
			y += pos.getY();
			z += pos.getZ();
			count++;
		}
		if (count > 0) {
			x /= count;
			y /= count;
			z /= count;
		}
		return new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
	}

	public static BlockPos rotateAroundOrigin(BlockPos pos, int turns) {
		for (int i = 0; i < turns; i++) {
			pos = rotateAroundOriginOnce(pos);
		}
		return pos;
	}

	/*
	 * fait pivoter une position autour de son origine (0, 0, 0), par pas de 90° dans le sens horaire
	 */
	private static BlockPos rotateAroundOriginOnce(BlockPos pos) {
		return new BlockPos(-pos.getZ(), pos.getY(), pos.getX());
	}

	/*
	 * vérifie si test se trouve dans le pavé délimité par pos1 et pos2
	 *
	 * @return true si c'est le cas
	 */
	public static boolean isPositionWithinBounds(BlockPos test, BlockPos pos1, BlockPos pos2) {
		return isPositionWithinHorizontalBounds(test, pos1, pos2)
				&& test.getY() >= pos1.getY() && test.getY() <= pos2.getY();
	}

	public static boolean isPositionWithinHorizontalBounds(BlockPos test, BlockPos minPos, BlockPos maxPos) {
		return test.getX() >= minPos.getX() && test.getX() <= maxPos.getX()
				&& test.getZ() >= minPos.getZ() && test.getZ() <= maxPos.getZ();
	}

	/*
	 * renvoie une nouvelle position contenant les coordonnées minimales des deux positions passées
	 */
	public static BlockPos getMin(BlockPos pos1, BlockPos pos2) {
		return new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
	}

	/*
	 * renvoie une nouvelle position contenant les coordonnées maximales des deux positions passées
	 */
	public static BlockPos getMax(BlockPos pos1, BlockPos pos2) {
		return new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
	}

	/*
	 * fait pivoter une position relative dans une zone de xSize x zSize, utilisé par les templates
	 * pour obtenir une position relative après rotation
	 */
	public static BlockPos rotateInArea(BlockPos pos, int xSize, int zSize, int turns) {
		int xSize1 = xSize;
		int zSize1 = zSize;
		int x = pos.getX();
		int z = pos.getZ();
		int x1 = x;
		int z1 = z;
		int positiveTurns = turns > 0 ? turns : 4 + turns;
		for (int i = 0; i < positiveTurns; i++) {
			x = zSize - 1 - z1;
			z = x1;
			x1 = x;
			z1 = z;
			xSize = zSize1;
			zSize = xSize1;
			xSize1 = xSize;
			zSize1 = zSize;
		}
		return new BlockPos(x, pos.getY(), z);
	}
}

package net.shadowmage.ancientwarfarerecompiled.core.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Bloc pouvant être tourné avec un marteau (mode rotation).
 *
 * Remplace la méthode rotateBlock de Block (supprimée en 1.21.1). Les blocs du mod l'implémentent
 * lorsqu'ils seront portés.
 */
public interface IHammerRotatable {
	/**
	 * @param side face visée par le marteau
	 * @return true si le bloc a été tourné
	 */
	boolean rotateBlock(Level level, BlockPos pos, net.minecraft.core.Direction side);
}

package net.shadowmage.ancientwarfarerecompiled.core.tile;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Tuiles qui doivent réagir juste avant que leur bloc ne soit cassé.
 */
public interface IBlockBreakHandler {
	void onBlockBroken(BlockState state);
}

package net.shadowmage.ancientwarfarerecompiled.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Pavé de positions délimité par deux coins (min et max, inclus).
 */
public class Zone implements INBTSerializable<CompoundTag> {
	public BlockPos min;
	public BlockPos max;

	public Zone(BlockPos p1, BlockPos p2) {
		min = BlockTools.getMin(p1, p2);
		max = BlockTools.getMax(p1, p2);
	}

	public Zone() {}

	private boolean intersects(BlockPos min, BlockPos max) {
		return intersects(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
	}

	public boolean intersects(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		return max.getX() >= minX && max.getY() >= minY && max.getZ() >= minZ && min.getX() <= maxX && min.getY() <= maxY && min.getZ() <= maxZ;
	}

	/*
	 * indique si la zone passée partage au moins une position avec cette zone
	 */
	public boolean intersects(Zone z) {
		return intersects(z.min, z.max);
	}

	public boolean equals(BlockPos min, BlockPos max) {
		return min.equals(this.min) && max.equals(this.max);
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof Zone other && this.equals(other.min, other.max);
	}

	@Override
	public int hashCode() {
		return 31 * min.hashCode() + max.hashCode();
	}

	@Override
	public String toString() {
		return String.format("De %s à %s", min, max);
	}

	public boolean contains(BlockPos pos) {
		return pos.getX() >= min.getX() && pos.getX() <= max.getX()
				&& pos.getY() >= min.getY() && pos.getY() <= max.getY()
				&& pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putLong("min", min.asLong());
		tag.putLong("max", max.asLong());
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		min = BlockPos.of(tag.getLong("min"));
		max = BlockPos.of(tag.getLong("max"));
	}
}

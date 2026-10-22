package net.shadowmage.ancientwarfarerecompiled.automation.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;

import java.util.HashSet;
import java.util.Set;

/**
 * Tuile du chargeur deluxe : chunks supplémentaires choisis dans la grille 5x5 autour du chargeur.
 * L'index d'une case est col + row * 5, avec le centre (index 12) toujours forcé et non modifiable.
 */
public class TileChunkLoaderDeluxe extends BlockEntity {
	public static final int GRID = 5;
	public static final int CENTER = 12;

	private final Set<ChunkPos> extra = new HashSet<>();

	public TileChunkLoaderDeluxe(BlockPos pos, BlockState state) {
		super(AWAutomationTileTypes.CHUNK_LOADER_DELUXE.get(), pos, state);
	}

	private ChunkPos center() {
		return new ChunkPos(worldPosition);
	}

	private static ChunkPos chunkAt(ChunkPos center, int index) {
		return new ChunkPos(center.x + index % GRID - 2, center.z + index / GRID - 2);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.setChunkForced(center().x, center().z, true);
			for (ChunkPos chunk : extra) {
				serverLevel.setChunkForced(chunk.x, chunk.z, true);
			}
		}
	}

	/** Libère tous les chunks forcés par ce chargeur (appelé à la destruction du bloc). */
	public void releaseAll(ServerLevel serverLevel) {
		serverLevel.setChunkForced(center().x, center().z, false);
		for (ChunkPos chunk : extra) {
			serverLevel.setChunkForced(chunk.x, chunk.z, false);
		}
		extra.clear();
	}

	/** Bitmask des 25 cases : bit i = chunk i forcé (le centre compte toujours). */
	public int getMask() {
		int mask = 1 << CENTER;
		ChunkPos center = center();
		for (int i = 0; i < GRID * GRID; i++) {
			if (i != CENTER && extra.contains(chunkAt(center, i))) {
				mask |= 1 << i;
			}
		}
		return mask;
	}

	/** Coche ou décoche une case. Retourne false si l'index est invalide ou si le chargeur n'est pas côté serveur. */
	public boolean toggle(int index) {
		if (index < 0 || index >= GRID * GRID || index == CENTER || !(level instanceof ServerLevel serverLevel)) {
			return false;
		}
		ChunkPos chunk = chunkAt(center(), index);
		if (extra.remove(chunk)) {
			serverLevel.setChunkForced(chunk.x, chunk.z, false);
		} else {
			extra.add(chunk);
			serverLevel.setChunkForced(chunk.x, chunk.z, true);
		}
		setChanged();
		return true;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putLongArray("chunks", extra.stream().mapToLong(ChunkPos::toLong).toArray());
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		extra.clear();
		for (long value : tag.getLongArray("chunks")) {
			extra.add(new ChunkPos(value));
		}
	}
}

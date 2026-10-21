package net.shadowmage.ancientwarfarerecompiled.core.util;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.shadowmage.ancientwarfarerecompiled.core.interfaces.IInteractableTile;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Accès aux tuiles et au monde.
 *
 * Portage partiel : les méthodes liées à l'inventaire (InventoryTools), aux données sauvegardées
 * (SavedData) et aux biomes (définis par données en 1.21.1) seront portées avec leurs modules.
 */
public class WorldTools {
	private WorldTools() {}

	/*
	 * CÔTÉ SERVEUR UNIQUEMENT
	 */
	public static List<BlockEntity> getTileEntitiesInArea(Level level, int x1, int y1, int z1, int x2, int y2, int z2) {
		if (level instanceof ServerLevel serverLevel) {
			List<BlockEntity> tileEntities = Lists.newArrayList();
			for (int x = (x1 >> 4); x <= (x2 >> 4); x++) {
				for (int z = (z1 >> 4); z <= (z2 >> 4); z++) {
					addValidTilesInChunkArea(serverLevel, new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), tileEntities, x, z);
				}
			}
			return tileEntities;
		}
		return Collections.emptyList();
	}

	private static void addValidTilesInChunkArea(ServerLevel level, BlockPos min, BlockPos max, List<BlockEntity> tileEntities, int x, int z) {
		LevelChunk chunk = level.getChunk(x, z);
		for (BlockEntity tile : chunk.getBlockEntities().values()) {
			if (!tile.isRemoved() && isTileInArea(min, max, tile)) {
				tileEntities.add(tile);
			}
		}
	}

	private static boolean isTileInArea(BlockPos min, BlockPos max, BlockEntity tile) {
		BlockPos tilePos = tile.getBlockPos();
		return tilePos.getX() >= min.getX() && tilePos.getY() >= min.getY() && tilePos.getZ() >= min.getZ() && tilePos.getX() <= max.getX() && tilePos.getY() <= max.getY() && tilePos.getZ() <= max.getZ();
	}

	public static boolean sendClientEventToTile(BlockGetter level, BlockPos pos, int id, int param) {
		return getTile(level, pos, BlockEntity.class).map(t -> t.triggerEvent(id, param)).orElse(false);
	}

	public static Optional<BlockEntity> getTile(BlockGetter level, BlockPos pos) {
		return getTile(level, pos, BlockEntity.class);
	}

	public static <T> Optional<T> getTile(@Nullable BlockGetter level, @Nullable BlockPos pos, Class<T> teClass) {
		if (level == null || pos == null) {
			return Optional.empty();
		}

		BlockEntity te = level.getBlockEntity(pos);

		if (teClass.isInstance(te)) {
			return Optional.of(teClass.cast(te));
		}

		return Optional.empty();
	}

	public static boolean clickInteractableTileWithHand(Level level, BlockPos pos, Player player, InteractionHand hand) {
		return getTile(level, pos, IInteractableTile.class).map(t -> t.onBlockClicked(player, hand)).orElse(false);
	}

	private static final Map<ResourceKey<Level>, Predicate<Level>> DIMENSION_DAY_TIMES = new HashMap<>();

	public static void registerDimensionDaytimeLogic(ResourceKey<Level> dimension, Predicate<Level> isDayTime) {
		DIMENSION_DAY_TIMES.put(dimension, isDayTime);
	}

	public static boolean isDaytimeInDimension(Level level) {
		return DIMENSION_DAY_TIMES.getOrDefault(level.dimension(), Level::isDay).test(level);
	}

	/**
	 * Indique si le bloc expose un inventaire accessible par le côté donné (ou par défaut si null).
	 */
	public static boolean hasItemHandler(LevelReader level, BlockPos pos, @Nullable Direction side) {
		return level instanceof Level l && l.getCapability(Capabilities.ItemHandler.BLOCK, pos, side) != null;
	}
}

package net.shadowmage.ancientwarfarerecompiled.automation.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationBlocks;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;
import net.shadowmage.ancientwarfarerecompiled.core.config.AWCoreConfig;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Pale d'éolienne. Les pales connectées forment un multibloc : une colonne impaire (5 minimum) de pales,
 * dont le bloc central est le contrôleur. Le contrôleur fournit l'énergie du vent, proportionnelle à la taille.
 */
public class TileWindmillBlade extends BlockEntity {
	private static final String SPEED_TAG = "speed";
	private static final String CONTROL_TAG = "isControl";
	private static final String CONTROL_POS_TAG = "controlPos";
	private static final String SIZE_TAG = "size";
	private static final String ENERGY_TAG = "energy";
	private static final int MAX_RADIUS = 20;
	private static final int CHECK_INTERVAL = 20;
	private static final float ROTOR_SPEED = 4.0f;

	private float speed = 0f;
	private boolean control = false;
	@Nullable
	private BlockPos controlPos;
	private int size = 0;
	private double energy = 0;
	private int checkTimer = 0;

	public TileWindmillBlade(BlockPos pos, BlockState state) {
		super(AWAutomationTileTypes.WINDMILL_BLADE.get(), pos, state);
	}

	/** Vitesse de rotation en degrés par tick (0 si le multibloc n'est pas formé). */
	public float getSpeed() {
		return speed;
	}

	public boolean isControl() {
		return control;
	}

	public double getEnergy() {
		return energy;
	}

	/** Retire et renvoie l'énergie accumulée. */
	public double drainEnergy() {
		double e = energy;
		energy = 0;
		return e;
	}

	public void serverTick() {
		if (checkTimer-- <= 0) {
			checkTimer = CHECK_INTERVAL;
			validate();
		}
		if (control) {
			energy = size * AWCoreConfig.WINDMILL_PER_SIZE_OUTPUT.get();
		}
	}

	/** Recalcule le multibloc à partir de cette pale et l'applique à toutes les pales connectées. */
	private void validate() {
		Level lvl = getLevel();
		if (lvl == null) {
			return;
		}
		Set<BlockPos> set = connectedBlades(lvl);
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPos p : set) {
			minX = Math.min(minX, p.getX());
			minY = Math.min(minY, p.getY());
			minZ = Math.min(minZ, p.getZ());
			maxX = Math.max(maxX, p.getX());
			maxY = Math.max(maxY, p.getY());
			maxZ = Math.max(maxZ, p.getZ());
		}
		int xs = maxX - minX + 1;
		int ys = maxY - minY + 1;
		int zs = maxZ - minZ + 1;
		boolean valid = ys >= 5 && ys % 2 == 1 && (zs == 1 || xs == 1) && (zs == ys || xs == ys) && set.size() == xs * ys * zs;
		if (valid) {
			int half = (ys - 1) / 2;
			BlockPos center = xs > 1
					? new BlockPos(minX + half, minY + half, minZ)
					: new BlockPos(minX, minY + half, minZ + half);
			for (BlockPos p : set) {
				if (lvl.getBlockEntity(p) instanceof TileWindmillBlade blade) {
					blade.assign(center, p.equals(center), ys);
				}
			}
		} else {
			for (BlockPos p : set) {
				if (lvl.getBlockEntity(p) instanceof TileWindmillBlade blade) {
					blade.assign(null, false, 0);
				}
			}
		}
	}

	private Set<BlockPos> connectedBlades(Level lvl) {
		Set<BlockPos> found = new HashSet<>();
		Deque<BlockPos> queue = new ArrayDeque<>();
		found.add(worldPosition);
		queue.add(worldPosition);
		while (!queue.isEmpty()) {
			BlockPos cur = queue.poll();
			for (Direction d : Direction.values()) {
				BlockPos next = cur.relative(d);
				if (!found.contains(next) && next.distSqr(worldPosition) < MAX_RADIUS * MAX_RADIUS
						&& lvl.hasChunkAt(next) && lvl.getBlockState(next).is(AWAutomationBlocks.WINDMILL_BLADE.get())) {
					found.add(next);
					queue.add(next);
				}
			}
		}
		return found;
	}

	/** Appliqué par le validateur : contrôleur (ou null si non formé), taille de la colonne. */
	public void assign(@Nullable BlockPos center, boolean isControl, int columnSize) {
		boolean changed = !java.util.Objects.equals(controlPos, center) || control != isControl || size != columnSize;
		controlPos = center;
		control = isControl;
		size = columnSize;
		float newSpeed = center != null ? ROTOR_SPEED : 0f;
		if (newSpeed != speed) {
			speed = newSpeed;
			changed = true;
		}
		if (changed) {
			setChanged();
			if (level != null) {
				level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
			}
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putFloat(SPEED_TAG, speed);
		tag.putBoolean(CONTROL_TAG, control);
		tag.putInt(SIZE_TAG, size);
		tag.putDouble(ENERGY_TAG, energy);
		if (controlPos != null) {
			tag.putLong(CONTROL_POS_TAG, controlPos.asLong());
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		speed = tag.getFloat(SPEED_TAG);
		control = tag.getBoolean(CONTROL_TAG);
		size = tag.getInt(SIZE_TAG);
		energy = tag.getDouble(ENERGY_TAG);
		controlPos = tag.contains(CONTROL_POS_TAG) ? BlockPos.of(tag.getLong(CONTROL_POS_TAG)) : null;
	}
}

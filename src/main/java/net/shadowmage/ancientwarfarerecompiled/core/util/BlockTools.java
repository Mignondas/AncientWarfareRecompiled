package net.shadowmage.ancientwarfarerecompiled.core.util;

import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.shadowmage.ancientwarfarerecompiled.core.config.AWCoreConfig;
import net.shadowmage.ancientwarfarerecompiled.core.entity.AWFakePlayer;

import javax.annotation.Nullable;

/**
 * Utilitaires sur les positions et les blocs.
 *
 * Portage partiel : les rotations des états de blocs (leviers, rails, champignons)
 * et la lecture des états depuis JSON seront portées avec le module Blocks.
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

	/**
	 * Renvoie la position visée par le joueur à moins de 5 blocs, ou null si rien n'est touché.
	 * Les entités visées ont priorité : leur position est renvoyée à la place du bloc.
	 *
	 * @param offset si vrai, renvoie la case adjacente à la face touchée (utile pour placer un bloc)
	 */
	@Nullable
	public static BlockPos getBlockClickedOn(Player player, Level level, boolean offset) {
		double reachLength = 5.0D;
		Vec3 start = player.getEyePosition(1.0F);
		Vec3 look = player.getViewVector(1.0F);
		Vec3 end = start.add(look.x * reachLength, look.y * reachLength, look.z * reachLength);

		BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		if (hit.getType() == HitResult.Type.MISS) {
			return null;
		}

		AABB search = player.getBoundingBox().expandTowards(look.scale(reachLength)).inflate(1.0D);
		for (Entity entity : level.getEntities(player, search)) {
			if (entity.isPickable()) {
				AABB entityBox = entity.getBoundingBox().inflate(entity.getPickRadius());
				if (entityBox.contains(start)) {
					return BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());
				}
			}
		}

		return offset ? hit.getBlockPos().relative(hit.getDirection()) : hit.getBlockPos();
	}

	/**
	 * Casse le bloc et laisse tomber ses objets.
	 */
	public static boolean breakBlockAndDrop(Level level, BlockPos pos) {
		return breakBlock(level, pos, true);
	}

	/**
	 * Casse le bloc à la position donnée.
	 *
	 * Note de portage : le paramètre « fortune » de 1.12 n'existe plus sous cette forme en 1.21.1 ;
	 * les enchantements de l'outil seront gérés avec le module Automation.
	 */
	public static boolean breakBlock(Level level, BlockPos pos, boolean doDrop) {
		if (level.isClientSide) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		if (level.isEmptyBlock(pos) || state.getDestroySpeed(level, pos) < 0) {
			return false;
		}
		if (doDrop) {
			if (!canBreakBlock(level, pos, state)) {
				return false;
			}
			Block.dropResources(state, level, pos);
		}
		return level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
	}

	private static boolean canBreakBlock(Level level, BlockPos pos, BlockState state) {
		return !AWCoreConfig.FIRE_BLOCK_BREAK_EVENTS.get()
				|| !NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, AWFakePlayer.get(level))).isCanceled();
	}

	/**
	 * Casse le bloc sans faire tomber ses objets (le son et les particules sont joués).
	 */
	public static boolean breakBlockNoDrops(Level level, BlockPos pos, BlockState state) {
		if (!canBreakBlock(level, pos, state) || !level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)) {
			return false;
		}
		level.levelEvent(2001, pos, Block.getId(state));
		return true;
	}

	/**
	 * Fait utiliser l'objet à la position de l'auto-joueur, comme un clic droit dans le vide.
	 */
	public static boolean placeItemBlockRightClick(ItemStack stack, Level level, BlockPos pos) {
		AWFakePlayer owner = AWFakePlayer.get(level);
		owner.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
		owner.setItemInHand(InteractionHand.MAIN_HAND, stack);
		owner.setXRot(90F % 360F);

		return stack.getItem().use(level, owner, InteractionHand.MAIN_HAND).getResult() == InteractionResult.SUCCESS;
	}

	/**
	 * Place l'objet sur la face donnée du bloc à la position donnée.
	 */
	public static boolean placeItemBlock(ItemStack stack, Level level, BlockPos pos, Direction face) {
		Direction direction = face.getOpposite();
		BlockPos target = pos.relative(direction);

		AWFakePlayer owner = AWFakePlayer.get(level);
		owner.setItemInHand(InteractionHand.MAIN_HAND, stack);

		Vec3 hitVec = Vec3.atLowerCornerOf(target).add(0.25D, 0.25D, 0.25D);
		BlockHitResult hit = new BlockHitResult(hitVec, face, target, false);
		return stack.useOn(new UseOnContext(level, owner, InteractionHand.MAIN_HAND, stack, hit)) == InteractionResult.SUCCESS;
	}

	public static void notifyBlockUpdate(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		level.sendBlockUpdated(pos, state, state, 3);
	}

	public static void notifyBlockUpdate(BlockEntity tile) {
		notifyBlockUpdate(tile.getLevel(), tile.getBlockPos());
	}

	public static void notifyNeighbors(BlockEntity tile) {
		Level level = tile.getLevel();
		BlockPos pos = tile.getBlockPos();
		level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
	}

	public static Iterable<BlockPos> getAllInBoxTopDown(BlockPos from, BlockPos to) {
		return getAllInBoxTopDown(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()), Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
	}

	/*
	 * parcourt la boîte de haut en bas : chaque couche Y est parcourue en X puis Z
	 */
	public static Iterable<BlockPos> getAllInBoxTopDown(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
		return () -> new AbstractIterator<BlockPos>() {
			private boolean first = true;
			private int lastPosX;
			private int lastPosY;
			private int lastPosZ;

			@Override
			protected BlockPos computeNext() {
				if (first) {
					first = false;
					lastPosX = x1;
					lastPosY = y2;
					lastPosZ = z1;
					return new BlockPos(x1, y2, z1);
				} else if (lastPosX == x2 && lastPosY == y1 && lastPosZ == z2) {
					return endOfData();
				} else {
					if (lastPosX < x2) {
						++lastPosX;
					} else if (lastPosZ < z2) {
						lastPosX = x1;
						++lastPosZ;
					} else if (lastPosY > y1) {
						lastPosX = x1;
						lastPosZ = z1;
						--lastPosY;
					}

					return new BlockPos(lastPosX, lastPosY, lastPosZ);
				}
			}
		};
	}
}

package net.shadowmage.ancientwarfarerecompiled.automation.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;

/**
 * Pale d'éolienne : porte la vitesse de rotation affichée. La vitesse est envoyée au client avec le chunk.
 */
public class TileWindmillBlade extends BlockEntity {
	private static final String SPEED_TAG = "speed";
	private float speed = 4.0f;

	public TileWindmillBlade(BlockPos pos, BlockState state) {
		super(AWAutomationTileTypes.WINDMILL_BLADE.get(), pos, state);
	}

	/** Vitesse de rotation en degrés par tick. */
	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		if (tag.contains(SPEED_TAG)) {
			speed = tag.getFloat(SPEED_TAG);
		}
	}
}

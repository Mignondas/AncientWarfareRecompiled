package net.shadowmage.ancientwarfarerecompiled.core.owner;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfarerecompiled.core.util.StringTools;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.UUID;

/**
 * Propriétaire d'une tuile ou d'une entité : nom et identifiant du joueur.
 */
@Immutable
public class Owner {
	public static final Owner EMPTY = new Owner();

	private static final String OWNER_NAME_TAG = "ownerName";
	private static final String OWNER_ID_TAG = "ownerId";
	private final UUID uuid;
	private final String name;

	private Owner() {
		this(new UUID(0, 0), "");
	}

	private Owner(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}

	public Owner(Player player) {
		this(player.getUUID(), player.getScoreboardName());
	}

	public Owner(ByteBuf buffer) {
		this(new UUID(buffer.readLong(), buffer.readLong()), StringTools.readString(buffer));
	}

	public Owner(Level level, String name) {
		Player player = level instanceof ServerLevel serverLevel ? serverLevel.getServer().getPlayerList().getPlayerByName(name) : null;
		uuid = player != null ? player.getUUID() : new UUID(0, 0);
		this.name = name;
	}

	public boolean isOwnerOrSameTeamOrFriend(@Nullable Entity entity) {
		// entités propres au mod
		if (entity instanceof IOwnable ownable) {
			Owner owner = ownable.getOwner();
			return isOwnerOrSameTeamOrFriend(entity.level(), owner.getUUID(), owner.getName());
		}
		// entités vanilla appartenant à un propriétaire
		if (entity instanceof OwnableEntity ownable && ownable.getOwner() != null) {
			Entity owner = ownable.getOwner();
			return isOwnerOrSameTeamOrFriend(entity.level(), owner.getUUID(), owner.getName().getString());
		}
		return entity != null && isOwnerOrSameTeamOrFriend(entity.level(), entity.getUUID(), entity.getName().getString());
	}

	public boolean isOwnerOrSameTeamOrFriend(Level level, @Nullable UUID playerId, String playerName) {
		return TeamViewerRegistry.areFriendly(level, uuid, playerId, name, playerName);
	}

	public String getName() {
		return name;
	}

	public UUID getUUID() {
		return uuid;
	}

	public void serializeToBuffer(ByteBuf buffer) {
		buffer.writeLong(uuid.getMostSignificantBits());
		buffer.writeLong(uuid.getLeastSignificantBits());
		StringTools.writeString(buffer, name);
	}

	public CompoundTag serializeToNBT(CompoundTag tag) {
		if (this == EMPTY) {
			return tag;
		}
		tag.putString(OWNER_NAME_TAG, name);
		tag.putUUID(OWNER_ID_TAG, uuid);
		return tag;
	}

	public static Owner deserializeFromNBT(CompoundTag tag) {
		if (tag.contains(OWNER_NAME_TAG)) {
			return new Owner(tag.getUUID(OWNER_ID_TAG), tag.getString(OWNER_NAME_TAG));
		}
		return Owner.EMPTY;
	}

	public boolean playerHasCommandPermissions(Level level, UUID playerId, String playerName) {
		return this != Owner.EMPTY && TeamViewerRegistry.areTeamMates(level, uuid, playerId, name, playerName);
	}
}

package net.shadowmage.ancientwarfarerecompiled.automation.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfarerecompiled.automation.gamedata.MailboxData;
import net.shadowmage.ancientwarfarerecompiled.automation.init.AWAutomationTileTypes;

import javax.annotation.Nullable;

/**
 * Boîte aux lettres : envoie le contenu de sendInventory vers la boîte de destination,
 * et reçoit dans receivedInventory les objets adressés à sa propre boîte.
 */
public class TileMailbox extends BlockEntity {
	/** Ticks de transit par bloc de distance, et ticks pour un changement de dimension. */
	public static final int TIME_PER_BLOCK = 10;
	public static final int TIME_FOR_DIMENSION = 100;
	private static final int MAX_NAME = 32;

	public final ItemStackHandler sendInventory = new ItemStackHandler(18);
	public final ItemStackHandler receivedInventory = new ItemStackHandler(18);

	private String owner = "";
	private String mailboxName;
	private String destinationName;
	private boolean privateBox;

	public TileMailbox(BlockPos pos, BlockState state) {
		super(AWAutomationTileTypes.MAILBOX.get(), pos, state);
	}

	public void setOwner(String name) {
		owner = name;
		setChanged();
	}

	public String getMailboxName() {
		return mailboxName;
	}

	public String getTargetName() {
		return destinationName;
	}

	public boolean isPrivateBox() {
		return privateBox;
	}

	/** Clé de la boîte dans MailboxData : le propriétaire pour une boîte privée, "" pour une publique. */
	private String dataOwner() {
		return privateBox ? owner : "";
	}

	/** Applique les réglages reçus du client (validés côté serveur). */
	public void applySettings(ServerLevel level, String name, String target, boolean priv) {
		privateBox = priv;
		mailboxName = clean(name);
		destinationName = clean(target);
		if (mailboxName != null) {
			MailboxData.get(level).addBox(dataOwner(), mailboxName);
		}
		setChanged();
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	@Nullable
	private static String clean(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		return trimmed.length() > MAX_NAME ? trimmed.substring(0, MAX_NAME) : trimmed;
	}

	public void serverTick(ServerLevel level) {
		if (mailboxName == null) {
			return;
		}
		MailboxData data = MailboxData.get(level);
		String key = dataOwner();
		long now = level.getGameTime();

		// Réception : un objet arrivé à temps est versé dans receivedInventory (un par tick).
		for (MailboxData.Delivery delivery : data.getDeliveries(key, mailboxName)) {
			int needed = delivery.dim().equals(level.dimension())
					? (int) (Math.sqrt(delivery.pos().distSqr(worldPosition)) * TIME_PER_BLOCK)
					: TIME_FOR_DIMENSION;
			if (now - delivery.sentTick() < needed) {
				continue;
			}
			ItemStack rest = ItemHandlerHelper.insertItemStacked(receivedInventory, delivery.stack().copy(), false);
			data.replaceDelivery(key, mailboxName, delivery, rest);
			setChanged();
			break;
		}

		// Envoi : le premier emplacement non vide part vers la destination.
		if (destinationName != null) {
			for (int slot = 0; slot < sendInventory.getSlots(); slot++) {
				ItemStack stack = sendInventory.getStackInSlot(slot);
				if (!stack.isEmpty()) {
					ItemStack out = sendInventory.extractItem(slot, stack.getCount(), false);
					data.addDelivery(key, destinationName, out, level.dimension(), worldPosition, now);
					setChanged();
					break;
				}
			}
		}
	}

	/** Vide les deux inventaires dans le monde (appelé à la destruction du bloc). */
	public void dropContents(Level level, BlockPos pos) {
		for (ItemStackHandler handler : new ItemStackHandler[] {sendInventory, receivedInventory}) {
			for (int slot = 0; slot < handler.getSlots(); slot++) {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(slot));
			}
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putString("owner", owner);
		tag.putBoolean("privateBox", privateBox);
		if (mailboxName != null) {
			tag.putString("mailboxName", mailboxName);
		}
		if (destinationName != null) {
			tag.putString("targetName", destinationName);
		}
		tag.put("sendInventory", sendInventory.serializeNBT(registries));
		tag.put("receivedInventory", receivedInventory.serializeNBT(registries));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		owner = tag.getString("owner");
		privateBox = tag.getBoolean("privateBox");
		mailboxName = tag.contains("mailboxName") ? tag.getString("mailboxName") : null;
		destinationName = tag.contains("targetName") ? tag.getString("targetName") : null;
		sendInventory.deserializeNBT(registries, tag.getCompound("sendInventory"));
		receivedInventory.deserializeNBT(registries, tag.getCompound("receivedInventory"));
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}

package net.shadowmage.ancientwarfarerecompiled.automation.gamedata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Données globales des boîtes aux lettres : boîtes par propriétaire ("" = publiques) et envois en attente.
 * Stockées dans le stockage de l'overworld, donc partagées entre toutes les dimensions.
 */
public class MailboxData extends SavedData {
	public static final String ID = "ancientwarfarerecompiled_mailboxes";

	/** Un objet en transit : parti de dim/pos au tick sentTick. */
	public record Delivery(ItemStack stack, ResourceKey<Level> dim, BlockPos pos, long sentTick) {}

	private static final Codec<Delivery> DELIVERY_CODEC = RecordCodecBuilder.create(i -> i.group(
			ItemStack.CODEC.fieldOf("item").forGetter(Delivery::stack),
			Level.RESOURCE_KEY_CODEC.fieldOf("dim").forGetter(Delivery::dim),
			BlockPos.CODEC.fieldOf("pos").forGetter(Delivery::pos),
			Codec.LONG.fieldOf("sent").forGetter(Delivery::sentTick)
	).apply(i, Delivery::new));

	private static final Codec<Map<String, Map<String, List<Delivery>>>> BOXES_CODEC =
			Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, DELIVERY_CODEC.listOf()));

	private static final SavedData.Factory<MailboxData> FACTORY = new SavedData.Factory<>(MailboxData::new, MailboxData::load, null);

	private final Map<String, Map<String, List<Delivery>>> boxes = new HashMap<>();

	public MailboxData() {}

	public static MailboxData get(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, ID);
	}

	private static MailboxData load(CompoundTag tag, HolderLookup.Provider registries) {
		MailboxData data = new MailboxData();
		if (tag.contains("boxes")) {
			BOXES_CODEC.parse(NbtOps.INSTANCE, tag.get("boxes")).result().ifPresent(map ->
					map.forEach((owner, set) -> {
						Map<String, List<Delivery>> copy = data.boxes.computeIfAbsent(owner, k -> new HashMap<>());
						set.forEach((name, list) -> copy.put(name, new ArrayList<>(list)));
					}));
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		tag.put("boxes", BOXES_CODEC.encodeStart(NbtOps.INSTANCE, boxes).result().orElseThrow());
		return tag;
	}

	/** Crée une boîte si elle n'existe pas. */
	public boolean addBox(String owner, String name) {
		Map<String, List<Delivery>> set = boxes.computeIfAbsent(owner, k -> new HashMap<>());
		if (set.containsKey(name)) {
			return false;
		}
		set.put(name, new ArrayList<>());
		setDirty();
		return true;
	}

	public List<Delivery> getDeliveries(String owner, String name) {
		Map<String, List<Delivery>> set = boxes.get(owner);
		if (set == null || !set.containsKey(name)) {
			return List.of();
		}
		return List.copyOf(set.get(name));
	}

	public void addDelivery(String owner, String name, ItemStack stack, ResourceKey<Level> dim, BlockPos pos, long tick) {
		boxes.computeIfAbsent(owner, k -> new HashMap<>())
				.computeIfAbsent(name, k -> new ArrayList<>())
				.add(new Delivery(stack, dim, pos.immutable(), tick));
		setDirty();
	}

	/** Remplace un objet livré partiellement par son reste, ou le retire si rien ne reste. */
	public void replaceDelivery(String owner, String name, Delivery old, ItemStack rest) {
		Map<String, List<Delivery>> set = boxes.get(owner);
		if (set == null || !set.containsKey(name)) {
			return;
		}
		List<Delivery> list = set.get(name);
		int index = list.indexOf(old);
		if (index < 0) {
			return;
		}
		if (rest.isEmpty()) {
			list.remove(index);
		} else {
			list.set(index, new Delivery(rest, old.dim(), old.pos(), old.sentTick()));
		}
		setDirty();
	}
}

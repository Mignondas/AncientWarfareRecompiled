package net.shadowmage.ancientwarfarerecompiled.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Utilitaires NBT. Les méthodes qui manipulent des états de blocs seront ajoutées
 * avec la portée Blocks (BlockTools), car elles dépendent du registre des blocs.
 */
public class NBTHelper {
	private NBTHelper() {}

	/** Identifiant de NBT "compound" dans une liste (valeur de Tag.TAG_COMPOUND en 1.21.1). */
	private static final int TAG_COMPOUND = 10;
	/** Identifiant de NBT "string" (valeur de Tag.TAG_STRING en 1.21.1). */
	private static final int TAG_STRING = 8;

	public static <T> Set<T> getSet(ListTag tagList, Function<Tag, T> getElement) {
		Set<T> ret = new HashSet<>();
		for (Tag tag : tagList) {
			ret.add(getElement.apply(tag));
		}
		return ret;
	}

	public static <T> List<T> getList(ListTag tagList, Function<Tag, T> getElement) {
		ArrayList<T> ret = new ArrayList<>();
		for (Tag tag : tagList) {
			ret.add(getElement.apply(tag));
		}
		return ret;
	}

	public static Set<String> getStringSet(ListTag tagList) {
		return getSet(getTagList(tagList, TAG_STRING), tag -> tag.getAsString());
	}

	public static <T> ListTag getTagList(Collection<T> collection, Function<T, Tag> serializeElement) {
		ListTag ret = new ListTag();
		collection.forEach(element -> ret.add(serializeElement.apply(element)));
		return ret;
	}

	public static ListTag getNBTStringList(Collection<String> strings) {
		ListTag ret = new ListTag();
		strings.forEach(str -> ret.add(StringTag.valueOf(str)));
		return ret;
	}

	public static ListTag getNBTUniqueIdList(Collection<UUID> uuids) {
		ListTag ret = new ListTag();
		uuids.forEach(uuid -> ret.add(new NBTBuilder().setUniqueId("uuid", uuid).build()));
		return ret;
	}

	public static Set<UUID> getUniqueIdSet(Tag tag) {
		return getSet(getTagList(tag, TAG_COMPOUND), element -> ((CompoundTag) element).getUUID("uuid"));
	}

	/**
	 * Renvoie la liste si son type d'élément correspond (ou si elle est vide), sinon une liste vide.
	 * Équivalent de l'ancien getTagList(NBTBase, int) de 1.12.
	 */
	private static ListTag getTagList(Tag tag, int elementType) {
		if (tag instanceof ListTag list) {
			if (!list.isEmpty() && list.getElementType() != elementType) {
				return new ListTag();
			}
			return list;
		}
		AncientWarfareRecompiled.LOGGER.error("Erreur de conversion en liste NBT : {}", tag);
		return new ListTag();
	}

	public static final Collector<Tag, ListTag, ListTag> NBTLIST_COLLECTOR =
			Collector.of(ListTag::new, ListTag::add, (l1, l2) -> {
				l2.forEach(l1::add);
				return l1;
			});

	public static CompoundTag writeBlockPosToNBT(CompoundTag tag, BlockPos pos) {
		tag.putInt("x", pos.getX());
		tag.putInt("y", pos.getY());
		tag.putInt("z", pos.getZ());
		return tag;
	}

	public static BlockPos readBlockPosFromNBT(CompoundTag tag) {
		return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
	}

	public static <K, V> Map<K, V> getMap(ListTag list, Function<CompoundTag, K> getKey, Function<CompoundTag, V> getValue) {
		Map<K, V> ret = new HashMap<>();
		for (int i = 0; i < list.size(); i++) {
			CompoundTag tag = list.getCompound(i);
			ret.put(getKey.apply(tag), getValue.apply(tag));
		}
		return ret;
	}

	public static <K, V> ListTag mapToCompoundList(Map<K, V> map, BiConsumer<CompoundTag, K> setKeyTag, BiConsumer<CompoundTag, V> setValueTag) {
		ListTag list = new ListTag();
		for (Map.Entry<K, V> entry : map.entrySet()) {
			CompoundTag nbtEntry = new CompoundTag();
			setKeyTag.accept(nbtEntry, entry.getKey());
			setValueTag.accept(nbtEntry, entry.getValue());
			list.add(nbtEntry);
		}
		return list;
	}

	public static <T extends INBTSerializable<CompoundTag>> void writeSerializablesTo(HolderLookup.Provider provider, CompoundTag tag, String key, List<T> elements) {
		ListTag list = new ListTag();
		for (T serializable : elements) {
			list.add(serializable.serializeNBT(provider));
		}
		tag.put(key, list);
	}

	public static <T extends INBTSerializable<CompoundTag>> List<T> deserializeListFrom(HolderLookup.Provider provider, CompoundTag tag, String key, Supplier<T> supplier) {
		ListTag tags = tag.getList(key, TAG_COMPOUND);
		ArrayList<T> list = new ArrayList<>();
		for (int i = 0; i < tags.size(); i++) {
			T element = supplier.get();
			element.deserializeNBT(provider, tags.getCompound(i));
			list.add(element);
		}
		return list;
	}
}

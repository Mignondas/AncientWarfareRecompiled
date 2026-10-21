package net.shadowmage.ancientwarfarerecompiled.core.util;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class SortItemsFirstComparator implements Comparator<ItemStack> {
	private final Map<Predicate<ItemStack>, Integer> firstElements = new HashMap<>();

	/**
	 * @param firstElements Item, Block, ou classe (Item.class / Block.class sous-classes) à mettre en tête,
	 *                      dans l'ordre fourni
	 */
	public SortItemsFirstComparator(Object... firstElements) {
		for (int i = 0; i < firstElements.length; i++) {
			Object element = firstElements[i];

			Predicate<ItemStack> matches;
			if (element instanceof Item item) {
				matches = s -> s.getItem() == item;
			} else if (element instanceof Block block) {
				matches = s -> s.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == block;
			} else if (element instanceof Class<?> clazz && Block.class.isAssignableFrom(clazz)) {
				matches = s -> s.getItem() instanceof BlockItem blockItem && clazz.isAssignableFrom(blockItem.getBlock().getClass());
			} else if (element instanceof Class<?> clazz && Item.class.isAssignableFrom(clazz)) {
				matches = s -> clazz.isAssignableFrom(s.getItem().getClass());
			} else {
				continue;
			}

			this.firstElements.put(matches, firstElements.length - i);
		}
	}

	@Override
	public int compare(ItemStack o1, ItemStack o2) {
		if (o1 == o2 || o1.getItem() == o2.getItem()) {
			return 0;
		}

		int sortWeight1 = 0;
		int sortWeight2 = 0;
		for (Map.Entry<Predicate<ItemStack>, Integer> entry : firstElements.entrySet()) {
			if (entry.getKey().test(o1)) {
				sortWeight1 = entry.getValue();
			}
			if (entry.getKey().test(o2)) {
				sortWeight2 = entry.getValue();
			}

			if (sortWeight1 > 0 && sortWeight2 > 0) {
				break;
			}
		}

		return sortWeight2 - sortWeight1;
	}
}

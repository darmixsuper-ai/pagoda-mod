package dev.gatherlist;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/** Zaehlt Items, auch in Shulker-Kisten. */
public final class Counter {
	private static final int SHULKER_SLOTS = 27;

	private Counter() {
	}

	public static boolean isShulker(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock;
	}

	public static NonNullList<ItemStack> contents(ItemStack box) {
		NonNullList<ItemStack> items = NonNullList.withSize(SHULKER_SLOTS, ItemStack.EMPTY);
		box.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
		return items;
	}

	public static int countContainer(Container c, Item item) {
		int n = 0;
		for (int i = 0; i < c.getContainerSize(); i++) {
			ItemStack s = c.getItem(i);
			if (s.is(item)) {
				n += s.getCount();
			}
			if (isShulker(s)) {
				for (ItemStack inner : contents(s)) {
					if (inner.is(item)) {
						n += inner.getCount();
					}
				}
			}
		}
		return n;
	}
}

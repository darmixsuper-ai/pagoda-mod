package dev.pagoda;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public final class PagodaLogic {
	private static final int SHULKER_SLOTS = 27;

	private PagodaLogic() {
	}

	public static boolean isShulker(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock;
	}

	/** Eine Shulker-Kiste, die im Amboss "Pagoda" heisst (Gross/Klein egal). */
	public static boolean isPagodaShulker(ItemStack stack) {
		if (!isShulker(stack)) {
			return false;
		}
		Component name = stack.get(DataComponents.CUSTOM_NAME);
		return name != null && name.getString().toLowerCase(Locale.ROOT).contains("pagoda");
	}

	public static NonNullList<ItemStack> contents(ItemStack box) {
		NonNullList<ItemStack> items = NonNullList.withSize(SHULKER_SLOTS, ItemStack.EMPTY);
		box.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
		return items;
	}

	public static int countInBox(ItemStack box, Item item) {
		int n = 0;
		for (ItemStack s : contents(box)) {
			if (s.is(item)) {
				n += s.getCount();
			}
		}
		return n;
	}

	/**
	 * Legt so viel wie noch gebraucht wird von {@code stack} in die Pagoda-Kisten im Inventar.
	 * Der Stack wird entsprechend kleiner. Gibt zurueck, wie viele Items verschoben wurden.
	 */
	public static int absorb(ServerPlayer player, ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}
		Integer target = PagodaList.TARGETS.get(stack.getItem());
		if (target == null) {
			return 0;
		}
		Container inv = player.getInventory();
		List<ItemStack> boxes = new ArrayList<>();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (isPagodaShulker(s)) {
				boxes.add(s);
			}
		}
		if (boxes.isEmpty()) {
			return 0;
		}
		int have = 0;
		for (ItemStack box : boxes) {
			have += countInBox(box, stack.getItem());
		}
		int allowed = Math.min(stack.getCount(), target - have);
		if (allowed <= 0) {
			return 0;
		}

		int moved = 0;
		// Durchgang 0: angefangene Stapel auffuellen, Durchgang 1: leere Plaetze belegen
		for (int pass = 0; pass < 2 && moved < allowed; pass++) {
			for (ItemStack box : boxes) {
				if (moved >= allowed) {
					break;
				}
				NonNullList<ItemStack> items = contents(box);
				boolean changed = false;
				for (int i = 0; i < items.size() && moved < allowed; i++) {
					ItemStack slot = items.get(i);
					if (pass == 0 && !slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack)) {
						int n = Math.min(slot.getMaxStackSize() - slot.getCount(), allowed - moved);
						if (n > 0) {
							slot.grow(n);
							moved += n;
							changed = true;
						}
					} else if (pass == 1 && slot.isEmpty()) {
						int n = Math.min(stack.getMaxStackSize(), allowed - moved);
						items.set(i, stack.copyWithCount(n));
						moved += n;
						changed = true;
					}
				}
				if (changed) {
					box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
				}
			}
		}
		if (moved > 0) {
			Component name = stack.getHoverName();
			stack.shrink(moved);
			player.sendOverlayMessage(Component.literal("+" + moved + " ").withStyle(ChatFormatting.GREEN)
					.append(name.copy().withStyle(ChatFormatting.WHITE))
					.append(Component.literal(" → Pagoda-Kiste  (" + (have + moved) + "/" + target + ")").withStyle(ChatFormatting.GOLD)));
		}
		return moved;
	}

	/** Zaehlt ein Item im Inventar, in der Endertruhe und in allen Shulker-Kisten darin. */
	public static int countEverywhere(ServerPlayer player, Item item) {
		return countContainer(player.getInventory(), item) + countContainer(player.getEnderChestInventory(), item);
	}

	public static int countContainer(Container c, Item item) {
		int n = 0;
		for (int i = 0; i < c.getContainerSize(); i++) {
			ItemStack s = c.getItem(i);
			if (s.is(item)) {
				n += s.getCount();
			}
			if (isShulker(s)) {
				n += countInBox(s, item);
			}
		}
		return n;
	}
}

package dev.pagoda.client;

import dev.pagoda.PagodaList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Der aktuelle Stand aller Pagoda-Materialien, fuer Fenster und HUD. */
public final class Stand {
	public record Entry(ItemStack icon, String name, String id, int have, int need) {
		public boolean done() {
			return this.have >= this.need;
		}

		public int missing() {
			return Math.max(0, this.need - this.have);
		}
	}

	private static List<Entry> entries = List.of();
	private static int done;

	private Stand() {
	}

	public static void update(Minecraft mc) {
		if (mc.player == null) {
			return;
		}
		List<Entry> list = new ArrayList<>();
		int d = 0;
		for (Map.Entry<Item, Integer> e : PagodaList.TARGETS.entrySet()) {
			ItemStack icon = new ItemStack(e.getKey());
			Entry en = new Entry(icon, icon.getHoverName().getString(), Lager.id(e.getKey()), Lager.have(mc, e.getKey()), e.getValue());
			if (en.done()) {
				d++;
			}
			list.add(en);
		}
		entries = list;
		done = d;
	}

	public static List<Entry> entries() {
		return entries;
	}

	public static int done() {
		return done;
	}

	public static int total() {
		return PagodaList.TARGETS.size();
	}
}

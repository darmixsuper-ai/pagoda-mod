package dev.gatherlist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Liest eine Materialliste aus der Zwischenablage, z. B. aus einem YouTube-Kommentar:
 * "1910 spruce logs", "64x Oak Planks", "Fichtenstamm: 12".
 */
public final class ListImport {
	public record Result(int added, int unknown) {
	}

	private record Candidate(Item item, Set<String> words, String full) {
	}

	private static final Pattern NUM_FIRST = Pattern.compile("^\\s*(\\d+)\\s*[x×*]?\\s+(.+)$");
	private static final Pattern NUM_LAST = Pattern.compile("^\\s*(.+?)\\s*[:=x×*-]?\\s*(\\d+)\\s*$");

	private ListImport() {
	}

	private static String norm(String s) {
		return s.toLowerCase(Locale.ROOT).replaceAll("\\(.*?\\)", " ").replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
	}

	private static String singular(String w) {
		if (w.endsWith("ies") && w.length() > 4) {
			return w.substring(0, w.length() - 3) + "y";
		}
		if (w.endsWith("es") && (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("xes"))) {
			return w.substring(0, w.length() - 2);
		}
		if (w.endsWith("s") && !w.endsWith("ss") && w.length() > 3) {
			return w.substring(0, w.length() - 1);
		}
		return w;
	}

	private static Set<String> words(String s) {
		Set<String> out = new HashSet<>();
		for (String w : norm(s).split(" ")) {
			if (!w.isEmpty()) {
				out.add(singular(w));
			}
		}
		return out;
	}

	private static String joined(Set<String> words, String s) {
		StringBuilder b = new StringBuilder();
		for (String w : norm(s).split(" ")) {
			if (!w.isEmpty()) {
				b.append(singular(w)).append(' ');
			}
		}
		return b.toString().trim();
	}

	private static List<Candidate> candidates() {
		List<Candidate> out = new ArrayList<>();
		for (Item item : BuiltInRegistries.ITEM) {
			if (item == Items.AIR) {
				continue;
			}
			String path = BuiltInRegistries.ITEM.getKey(item).getPath().replace('_', ' ');
			out.add(new Candidate(item, words(path), joined(null, path)));
			String local = new ItemStack(item).getHoverName().getString();
			out.add(new Candidate(item, words(local), joined(null, local)));
		}
		return out;
	}

	private static Item match(String name, List<Candidate> all) {
		String want = joined(null, name);
		Set<String> ws = words(name);
		for (Candidate c : all) {
			if (c.full().equals(want)) {
				return c.item();
			}
		}
		Item best = null;
		int bestSize = 0;
		for (Candidate c : all) {
			if (!c.words().isEmpty() && ws.containsAll(c.words()) && c.words().size() > bestSize) {
				best = c.item();
				bestSize = c.words().size();
			}
		}
		return best;
	}

	public static Result into(Projects.Project p, String text) {
		List<Candidate> all = candidates();
		int added = 0;
		int unknown = 0;
		for (String line : text.split("\\R")) {
			if (line.isBlank()) {
				continue;
			}
			String name;
			int count;
			Matcher m = NUM_FIRST.matcher(line);
			if (m.matches()) {
				count = Integer.parseInt(m.group(1));
				name = m.group(2);
			} else {
				Matcher m2 = NUM_LAST.matcher(line);
				if (!m2.matches()) {
					continue;
				}
				name = m2.group(1);
				count = Integer.parseInt(m2.group(2));
			}
			if (count <= 0) {
				continue;
			}
			Item item = match(name, all);
			if (item == null) {
				unknown++;
				continue;
			}
			p.items.merge(Projects.id(item), count, Integer::sum);
			added++;
		}
		return new Result(added, unknown);
	}
}

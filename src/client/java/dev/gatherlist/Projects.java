package dev.gatherlist;

import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Alle Projekte (Materiallisten) und welches gerade aktiv ist. */
public final class Projects {
	public static final class Project {
		public String name = "";
		/** Item-ID -> gebrauchte Anzahl, in der Reihenfolge, wie sie eingetragen wurden. */
		public LinkedHashMap<String, Integer> items = new LinkedHashMap<>();
	}

	private static final class Data {
		int active = 0;
		List<Project> projects = new ArrayList<>();
	}

	private static Data data = new Data();

	private Projects() {
	}

	public static void load() {
		Data d = Json.read("projects.json", new TypeToken<Data>() { }.getType());
		if (d != null && d.projects != null) {
			data = d;
			for (Project p : data.projects) {
				if (p.items == null) {
					p.items = new LinkedHashMap<>();
				}
				if (p.name == null) {
					p.name = "";
				}
			}
		} else {
			Project example = loadExample();
			if (example != null) {
				data.projects.add(example);
			}
			save();
		}
		clampActive();
	}

	private static Project loadExample() {
		try (InputStream in = Projects.class.getResourceAsStream("/assets/gatherlist/examples/fire_pagoda.json")) {
			if (in == null) {
				return null;
			}
			return Json.GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), Project.class);
		} catch (Exception e) {
			GatherlistClient.LOGGER.warn("Gatherlist: Beispielprojekt fehlt", e);
			return null;
		}
	}

	public static void save() {
		Json.write("projects.json", data);
	}

	private static void clampActive() {
		if (data.projects.isEmpty()) {
			data.active = 0;
		} else {
			data.active = Math.max(0, Math.min(data.active, data.projects.size() - 1));
		}
	}

	public static List<Project> all() {
		return data.projects;
	}

	public static Project active() {
		return data.projects.isEmpty() ? null : data.projects.get(data.active);
	}

	public static int activeIndex() {
		return data.active;
	}

	public static void cycle(int dir) {
		if (data.projects.isEmpty()) {
			return;
		}
		int n = data.projects.size();
		data.active = ((data.active + dir) % n + n) % n;
		save();
	}

	public static Project create(String name) {
		Project p = new Project();
		p.name = name;
		data.projects.add(p);
		data.active = data.projects.size() - 1;
		save();
		return p;
	}

	public static void delete(Project p) {
		data.projects.remove(p);
		clampActive();
		save();
	}

	public static Item item(String id) {
		Identifier key = Identifier.tryParse(id);
		if (key == null) {
			return Items.AIR;
		}
		Item item = BuiltInRegistries.ITEM.getValue(key);
		return item == null ? Items.AIR : item;
	}

	public static String id(Item item) {
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}

	/** Die Items des Projekts, ohne unbekannte IDs. */
	public static Map<Item, Integer> resolved(Project p) {
		Map<Item, Integer> out = new LinkedHashMap<>();
		if (p == null) {
			return out;
		}
		for (Map.Entry<String, Integer> e : p.items.entrySet()) {
			Item item = item(e.getKey());
			if (item != Items.AIR && e.getValue() != null && e.getValue() > 0) {
				out.merge(item, e.getValue(), Integer::sum);
			}
		}
		return out;
	}
}

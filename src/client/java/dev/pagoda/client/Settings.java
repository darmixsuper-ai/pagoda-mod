package dev.pagoda.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Einstellungen aus dem Pagoda-Fenster. */
public final class Settings {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final class Data {
		boolean hud = true;
		boolean showDone = false;
	}

	private static Data data = new Data();

	private Settings() {
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("pagoda.json");
	}

	public static void load() {
		try {
			if (Files.exists(file())) {
				Data d = GSON.fromJson(Files.readString(file()), Data.class);
				if (d != null) {
					data = d;
				}
			}
		} catch (Exception e) {
			dev.pagoda.PagodaMod.LOGGER.warn("Pagoda: Einstellungen konnten nicht geladen werden", e);
		}
	}

	private static void save() {
		try {
			Files.writeString(file(), GSON.toJson(data));
		} catch (Exception e) {
			dev.pagoda.PagodaMod.LOGGER.warn("Pagoda: Einstellungen konnten nicht gespeichert werden", e);
		}
	}

	public static boolean hud() {
		return data.hud;
	}

	public static boolean showDone() {
		return data.showDone;
	}

	public static void toggleHud() {
		data.hud = !data.hud;
		save();
	}

	public static void toggleShowDone() {
		data.showDone = !data.showDone;
		save();
	}
}

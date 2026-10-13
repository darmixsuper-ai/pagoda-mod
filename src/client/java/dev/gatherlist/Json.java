package dev.gatherlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Speichert und laedt die Dateien im config/gatherlist-Ordner. */
public final class Json {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private Json() {
	}

	public static Path dir() {
		return FabricLoader.getInstance().getConfigDir().resolve("gatherlist");
	}

	public static <X> X read(String name, Type type) {
		try {
			Path p = dir().resolve(name);
			if (Files.exists(p)) {
				return GSON.fromJson(Files.readString(p), type);
			}
		} catch (Exception e) {
			GatherlistClient.LOGGER.warn("Gatherlist: {} konnte nicht geladen werden", name, e);
		}
		return null;
	}

	public static void write(String name, Object value) {
		try {
			Files.createDirectories(dir());
			Files.writeString(dir().resolve(name), GSON.toJson(value));
		} catch (Exception e) {
			GatherlistClient.LOGGER.warn("Gatherlist: {} konnte nicht gespeichert werden", name, e);
		}
	}
}

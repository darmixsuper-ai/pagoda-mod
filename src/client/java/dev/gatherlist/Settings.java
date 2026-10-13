package dev.gatherlist;

/** Einstellungen: HUD an/aus, fertige Items zeigen. */
public final class Settings {
	private static final class Data {
		boolean hud = true;
		boolean showDone = false;
	}

	private static Data data = new Data();

	private Settings() {
	}

	public static void load() {
		Data d = Json.read("settings.json", Data.class);
		if (d != null) {
			data = d;
		}
	}

	private static void save() {
		Json.write("settings.json", data);
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

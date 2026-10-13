package dev.gatherlist;

import net.minecraft.client.resources.language.I18n;

/** Kurze Hilfe fuer Uebersetzungen (en_us, de_de, de_ch ...). */
public final class T {
	private T() {
	}

	public static String t(String key, Object... args) {
		return I18n.get("gatherlist." + key, args);
	}
}

package dev.gatherlist;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Farben und kleine Zeichenhilfen. */
public final class Ui {
	public static final int PANEL = 0xEC12141A;
	public static final int ACCENT = 0xFF5FB3A1;
	public static final int GOLD = 0xFFE8B04A;
	public static final int GREEN = 0xFF6FBF4A;
	public static final int TEXT = 0xFFFFFFFF;
	public static final int MUTED = 0xFF8C93A0;
	public static final int WARN = 0xFFF2C14E;
	public static final int RED = 0xFFFF6B5A;

	private Ui() {
	}

	public static boolean in(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
		g.fill(x, y, x + w, y + h, PANEL);
		g.outline(x, y, w, h, 0x605FB3A1);
	}

	public static int buttonW(Font font, String label) {
		return font.width(label) + 12;
	}

	/** Ein Knopf; gibt seine Breite zurueck. */
	public static int button(GuiGraphicsExtractor g, Font font, String label, int x, int y, int mx, int my, int color) {
		int w = buttonW(font, label);
		boolean hover = in(mx, my, x, y, w, 16);
		g.fill(x, y, x + w, y + 16, hover ? 0x48FFFFFF : 0x28FFFFFF);
		g.text(font, label, x + 6, y + 4, color, false);
		return w;
	}

	public static void toggle(GuiGraphicsExtractor g, Font font, String label, boolean on, int x, int y, int mx, int my) {
		int w = font.width(label) + 16;
		g.fill(x, y, x + w, y + 16, in(mx, my, x, y, w, 16) ? 0x48FFFFFF : 0x28FFFFFF);
		g.fill(x + 4, y + 5, x + 10, y + 11, on ? GREEN : 0xFF4A4F58);
		g.text(font, label, x + 13, y + 4, on ? TEXT : MUTED, false);
	}

	public static int toggleW(Font font, String label) {
		return font.width(label) + 16;
	}

	public static void progress(GuiGraphicsExtractor g, int x, int y, int w, int h, int done, int total) {
		g.fill(x, y, x + w, y + h, 0xFF2A2E36);
		int fw = (int) (w * (total == 0 ? 0 : (double) done / total));
		g.fill(x, y, x + fw, y + h, GREEN);
	}
}

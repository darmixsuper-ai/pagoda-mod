package dev.gatherlist;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Ein einfaches Eingabefeld. */
public final class TextBox {
	public String text = "";
	public boolean focused;
	public int x;
	public int y;
	public int w;
	public int h = 16;
	private final int max;
	private final boolean digits;
	private final String placeholder;

	public TextBox(int max, boolean digits, String placeholder) {
		this.max = max;
		this.digits = digits;
		this.placeholder = placeholder;
	}

	public void place(int x, int y, int w) {
		this.x = x;
		this.y = y;
		this.w = w;
	}

	public boolean contains(double mx, double my) {
		return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;
	}

	public void render(GuiGraphicsExtractor g, Font font) {
		g.fill(this.x, this.y, this.x + this.w, this.y + this.h, this.focused ? 0x40FFFFFF : 0x28FFFFFF);
		g.outline(this.x, this.y, this.w, this.h, this.focused ? Ui.ACCENT : 0x30FFFFFF);
		boolean empty = this.text.isEmpty();
		String shown = empty && !this.focused ? this.placeholder : font.plainSubstrByWidth(this.text, this.w - 14) + (this.focused ? "_" : "");
		g.text(font, shown, this.x + 5, this.y + 4, empty && !this.focused ? Ui.MUTED : Ui.TEXT, false);
	}

	/** Gibt true zurueck, wenn sich der Text geaendert hat. */
	public boolean type(String c) {
		if (!this.focused || this.text.length() >= this.max) {
			return false;
		}
		if (this.digits && !c.chars().allMatch(Character::isDigit)) {
			return false;
		}
		this.text += c;
		return true;
	}

	public boolean backspace() {
		if (!this.focused || this.text.isEmpty()) {
			return false;
		}
		this.text = this.text.substring(0, this.text.length() - 1);
		return true;
	}
}

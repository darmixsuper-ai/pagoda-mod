package dev.pagoda.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Das Pagoda-Fenster. Oeffnen und schliessen mit der Taste rechts neben der 0. */
public class PagodaScreen extends Screen {
	private static final int GOLD = 0xFFE8B04A;
	private static final int GREEN = 0xFF6FA03B;
	private static final int ROW = 20;

	private final List<Stand.Entry> rows = new ArrayList<>();
	private String search = "";
	private double scroll;
	private boolean confirmForget;
	private int px;
	private int py;
	private int pw;
	private int ph;

	public PagodaScreen() {
		super(Component.literal("Pagoda"));
	}

	@Override
	protected void init() {
		this.pw = Math.min(320, this.width - 20);
		this.ph = Math.min(280, this.height - 20);
		this.px = (this.width - this.pw) / 2;
		this.py = (this.height - this.ph) / 2;
		this.refresh();
	}

	private void refresh() {
		this.rows.clear();
		String q = this.search.toLowerCase(Locale.ROOT).trim();
		List<Stand.Entry> doneRows = new ArrayList<>();
		for (Stand.Entry e : Stand.entries()) {
			if (!q.isEmpty() && !e.name().toLowerCase(Locale.ROOT).contains(q) && !e.id().contains(q.replace(' ', '_'))) {
				continue;
			}
			if (e.done()) {
				if (Settings.showDone()) {
					doneRows.add(e);
				}
			} else {
				this.rows.add(e);
			}
		}
		this.rows.addAll(doneRows);
	}

	// ---- Positionen ----
	private int searchX() {
		return this.px + 10;
	}

	private int searchY() {
		return this.py + 36;
	}

	private int searchW() {
		return this.pw - 20 - this.toggleW("HUD") - this.toggleW("Fertige") - 12;
	}

	private int toggleW(String label) {
		return this.minecraft.font.width(label) + 16;
	}

	private int hudX() {
		return this.searchX() + this.searchW() + 6;
	}

	private int doneX() {
		return this.hudX() + this.toggleW("HUD") + 6;
	}

	private int listTop() {
		return this.py + 58;
	}

	private int listBottom() {
		return this.py + this.ph - 22;
	}

	private int maxScroll() {
		return Math.max(0, this.rows.size() * ROW - (this.listBottom() - this.listTop()));
	}

	private static boolean in(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	private String forgetLabel() {
		return this.confirmForget ? "Sicher? Nochmal klicken" : "Truhen vergessen";
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void tick() {
		if (this.minecraft != null && this.minecraft.player != null && this.minecraft.player.tickCount % 10 == 0) {
			this.refresh();
		}
	}

	private void toggle(GuiGraphicsExtractor g, String label, boolean on, int x, int y, boolean hover) {
		Minecraft mc = this.minecraft;
		int w = this.toggleW(label);
		g.fill(x, y, x + w, y + 16, hover ? 0x40FFFFFF : 0x28FFFFFF);
		g.fill(x + 4, y + 5, x + 10, y + 11, on ? GREEN : 0xFF4A4436);
		g.text(mc.font, label, x + 13, y + 4, on ? 0xFFFFFFFF : 0xFFA09A88, false);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float a) {
		super.extractRenderState(g, mx, my, a);
		Minecraft mc = this.minecraft;
		int x0 = this.px;
		int x1 = this.px + this.pw;

		g.fill(x0, this.py, x1, this.py + this.ph, 0xE8141008);
		g.outline(x0, this.py, this.pw, this.ph, 0x60E8B04A);
		g.text(mc.font, "Pagoda – das fehlt noch", x0 + 10, this.py + 9, GOLD, true);
		String prog = Stand.done() + " / " + Stand.total() + " fertig";
		g.text(mc.font, prog, x1 - 10 - mc.font.width(prog), this.py + 9, 0xFFB8C99A, false);

		int barY = this.py + 24;
		g.fill(x0 + 10, barY, x1 - 10, barY + 6, 0xFF2E2A20);
		int fillW = (int) ((this.pw - 20) * (Stand.total() == 0 ? 0 : (double) Stand.done() / Stand.total()));
		g.fill(x0 + 10, barY, x0 + 10 + fillW, barY + 6, GREEN);

		// Suchleiste
		int sx = this.searchX();
		int sy = this.searchY();
		int sw = this.searchW();
		g.fill(sx, sy, sx + sw, sy + 16, 0x30FFFFFF);
		g.outline(sx, sy, sw, 16, 0x30E8B04A);
		boolean empty = this.search.isEmpty();
		String shown = empty ? "Suchen ..." : mc.font.plainSubstrByWidth(this.search, sw - 14) + "_";
		g.text(mc.font, shown, sx + 5, sy + 4, empty ? 0xFF8A8270 : 0xFFFFFFFF, false);

		// Schalter
		this.toggle(g, "HUD", Settings.hud(), this.hudX(), sy, in(mx, my, this.hudX(), sy, this.toggleW("HUD"), 16));
		this.toggle(g, "Fertige", Settings.showDone(), this.doneX(), sy, in(mx, my, this.doneX(), sy, this.toggleW("Fertige"), 16));

		// Liste
		this.scroll = Math.max(0, Math.min(this.scroll, this.maxScroll()));
		if (this.rows.isEmpty()) {
			String msg = !this.search.isEmpty() ? "Nichts gefunden." : "Du hast alles für die Pagoda!";
			g.centeredText(mc.font, msg, this.width / 2, this.listTop() + 30, !this.search.isEmpty() ? 0xFFA09A88 : GOLD);
		}
		g.enableScissor(x0 + 2, this.listTop(), x1 - 2, this.listBottom());
		for (int i = 0; i < this.rows.size(); i++) {
			Stand.Entry r = this.rows.get(i);
			int y = this.listTop() + i * ROW - (int) this.scroll;
			if (y + ROW < this.listTop() || y > this.listBottom()) {
				continue;
			}
			if (i % 2 == 0) {
				g.fill(x0 + 6, y, x1 - 6, y + ROW, 0x18FFFFFF);
			}
			g.item(r.icon(), x0 + 10, y + 2);
			String right = r.done() ? "✔ fertig" : "fehlt " + r.missing();
			String small = r.have() + "/" + r.need();
			int rw = mc.font.width(right);
			int smw = mc.font.width(small);
			int nameW = this.pw - 52 - rw - smw - 16;
			g.text(mc.font, mc.font.plainSubstrByWidth(r.name(), nameW), x0 + 32, y + 6, r.done() ? 0xFF8A8270 : 0xFFFFFFFF, false);
			g.text(mc.font, small, x1 - 12 - rw - 8 - smw, y + 6, 0xFF8A8270, false);
			g.text(mc.font, right, x1 - 12 - rw, y + 6, r.done() ? GREEN : 0xFFF2C14E, false);
		}
		g.disableScissor();

		int chests = Lager.chests(mc).size();
		String info = "Gezählt: Inventar + " + chests + (chests == 1 ? " Truhe" : " Truhen");
		g.text(mc.font, info, x0 + 10, this.py + this.ph - 14, 0xFF8A8270, false);
		String btn = this.forgetLabel();
		int bx = x1 - 10 - mc.font.width(btn);
		boolean hover = in(mx, my, bx - 4, this.py + this.ph - 18, x1 - 6 - (bx - 4), 14);
		g.text(mc.font, btn, bx, this.py + this.ph - 14, this.confirmForget ? 0xFFFF6B5A : (hover ? 0xFFFFFFFF : 0xFFB0A890), false);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		Minecraft mc = this.minecraft;
		double mx = event.x();
		double my = event.y();
		if (event.button() == 0) {
			int sy = this.searchY();
			if (in(mx, my, this.hudX(), sy, this.toggleW("HUD"), 16)) {
				Settings.toggleHud();
				return true;
			}
			if (in(mx, my, this.doneX(), sy, this.toggleW("Fertige"), 16)) {
				Settings.toggleShowDone();
				this.refresh();
				return true;
			}
			int bx = this.px + this.pw - 10 - mc.font.width(this.forgetLabel());
			if (in(mx, my, bx - 4, this.py + this.ph - 18, this.px + this.pw - 6 - (bx - 4), 14)) {
				if (this.confirmForget) {
					Lager.forget(mc);
					this.confirmForget = false;
					Stand.update(mc);
					this.refresh();
				} else {
					this.confirmForget = true;
				}
				return true;
			}
		}
		this.confirmForget = false;
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		this.scroll -= scrollY * ROW * 2;
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == PagodaClient.KEY) {
			this.onClose();
			return true;
		}
		if (event.key() == 259 && !this.search.isEmpty()) {
			this.search = this.search.substring(0, this.search.length() - 1);
			this.scroll = 0;
			this.refresh();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		String c = event.codepointAsString();
		if (event.isAllowedChatCharacter() && !c.equals("'") && !c.equals("?") && this.search.length() < 24) {
			this.search += c;
			this.scroll = 0;
			this.refresh();
			return true;
		}
		return super.charTyped(event);
	}
}

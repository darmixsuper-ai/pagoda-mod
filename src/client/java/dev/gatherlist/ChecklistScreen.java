package dev.gatherlist;

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

/** Das Hauptfenster: was fehlt noch fuer das aktive Projekt. */
public class ChecklistScreen extends Screen {
	private static final int ROW = 20;

	private final List<Stand.Entry> rows = new ArrayList<>();
	private final TextBox search = new TextBox(24, false, T.t("search"));
	private double scroll;
	private boolean confirmForget;
	private int px;
	private int py;
	private int pw;
	private int ph;

	public ChecklistScreen() {
		super(Component.literal("Gatherlist"));
		this.search.focused = true;
	}

	@Override
	protected void init() {
		this.pw = Math.min(340, this.width - 20);
		this.ph = Math.min(290, this.height - 20);
		this.px = (this.width - this.pw) / 2;
		this.py = (this.height - this.ph) / 2;
		Stand.update(this.minecraft);
		this.refresh();
	}

	private void refresh() {
		this.rows.clear();
		String q = this.search.text.toLowerCase(Locale.ROOT).trim();
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
	private int x1() {
		return this.px + this.pw;
	}

	private int editX() {
		return this.x1() - 10 - Ui.buttonW(this.font, T.t("new")) - 4 - Ui.buttonW(this.font, T.t("edit"));
	}

	private int newX() {
		return this.x1() - 10 - Ui.buttonW(this.font, T.t("new"));
	}

	private int prevX() {
		return this.px + 8;
	}

	private int nextX() {
		return this.editX() - 6 - Ui.buttonW(this.font, ">");
	}

	private int rowY() {
		return this.py + 44;
	}

	private int hudX() {
		return this.x1() - 10 - Ui.toggleW(this.font, T.t("show_done")) - 6 - Ui.toggleW(this.font, T.t("hud"));
	}

	private int doneX() {
		return this.x1() - 10 - Ui.toggleW(this.font, T.t("show_done"));
	}

	private int listTop() {
		return this.py + 66;
	}

	private int listBottom() {
		return this.py + this.ph - 22;
	}

	private int maxScroll() {
		return Math.max(0, this.rows.size() * ROW - (this.listBottom() - this.listTop()));
	}

	private String forgetLabel() {
		return this.confirmForget ? T.t("forget_confirm") : T.t("forget");
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

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float a) {
		super.extractRenderState(g, mx, my, a);
		Minecraft mc = this.minecraft;
		int x0 = this.px;
		int x1 = this.x1();
		Ui.panel(g, x0, this.py, this.pw, this.ph);

		// Kopfzeile: Projekt wechseln, bearbeiten, neu
		Projects.Project p = Projects.active();
		int hy = this.py + 8;
		Ui.button(g, this.font, "<", this.prevX(), hy, mx, my, Ui.TEXT);
		int nameX = this.prevX() + Ui.buttonW(this.font, "<") + 6;
		String name = p == null ? T.t("no_project") : (p.name.isBlank() ? T.t("unnamed") : p.name);
		g.text(this.font, this.font.plainSubstrByWidth(name, this.nextX() - nameX - 6), nameX, hy + 4, Ui.GOLD, true);
		Ui.button(g, this.font, ">", this.nextX(), hy, mx, my, Ui.TEXT);
		Ui.button(g, this.font, T.t("edit"), this.editX(), hy, mx, my, p == null ? Ui.MUTED : Ui.TEXT);
		Ui.button(g, this.font, T.t("new"), this.newX(), hy, mx, my, Ui.ACCENT);

		// Fortschritt
		String prog = T.t("progress", Stand.done(), Stand.total());
		g.text(this.font, prog, x0 + 10, this.py + 30, Ui.MUTED, false);
		int bx = x0 + 16 + this.font.width(prog);
		Ui.progress(g, bx, this.py + 32, x1 - 10 - bx, 5, Stand.done(), Stand.total());

		// Suche und Schalter
		this.search.place(x0 + 10, this.rowY(), this.hudX() - 6 - (x0 + 10));
		this.search.render(g, this.font);
		Ui.toggle(g, this.font, T.t("hud"), Settings.hud(), this.hudX(), this.rowY(), mx, my);
		Ui.toggle(g, this.font, T.t("show_done"), Settings.showDone(), this.doneX(), this.rowY(), mx, my);

		// Liste
		this.scroll = Math.max(0, Math.min(this.scroll, this.maxScroll()));
		if (this.rows.isEmpty()) {
			String msg;
			if (p == null) {
				msg = T.t("hint_new");
			} else if (Stand.total() == 0) {
				msg = T.t("hint_empty");
			} else if (!this.search.text.isEmpty()) {
				msg = T.t("nothing_found");
			} else {
				msg = T.t("all_done");
			}
			g.centeredText(this.font, msg, x0 + this.pw / 2, this.listTop() + 30, Ui.GOLD);
		}
		g.enableScissor(x0 + 2, this.listTop(), x1 - 2, this.listBottom());
		for (int i = 0; i < this.rows.size(); i++) {
			Stand.Entry r = this.rows.get(i);
			int y = this.listTop() + i * ROW - (int) this.scroll;
			if (y + ROW < this.listTop() || y > this.listBottom()) {
				continue;
			}
			if (i % 2 == 0) {
				g.fill(x0 + 6, y, x1 - 6, y + ROW, 0x14FFFFFF);
			}
			g.item(r.icon(), x0 + 10, y + 2);
			String right = r.done() ? T.t("done") : T.t("missing", r.missing());
			String small = r.have() + "/" + r.need();
			int rw = this.font.width(right);
			int sw = this.font.width(small);
			g.text(this.font, this.font.plainSubstrByWidth(r.name(), this.pw - 52 - rw - sw - 16), x0 + 32, y + 6, r.done() ? Ui.MUTED : Ui.TEXT, false);
			g.text(this.font, small, x1 - 12 - rw - 8 - sw, y + 6, Ui.MUTED, false);
			g.text(this.font, right, x1 - 12 - rw, y + 6, r.done() ? Ui.GREEN : Ui.WARN, false);
		}
		g.disableScissor();

		// Fusszeile
		int chests = Storage.chests(mc).size();
		g.text(this.font, T.t("counted", chests), x0 + 10, this.py + this.ph - 14, Ui.MUTED, false);
		String fl = this.forgetLabel();
		int fx = x1 - 10 - this.font.width(fl);
		boolean hover = Ui.in(mx, my, fx - 4, this.py + this.ph - 18, x1 - 6 - (fx - 4), 14);
		g.text(this.font, fl, fx, this.py + this.ph - 14, this.confirmForget ? Ui.RED : (hover ? Ui.TEXT : Ui.MUTED), false);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		Minecraft mc = this.minecraft;
		double mx = event.x();
		double my = event.y();
		if (event.button() != 0) {
			return super.mouseClicked(event, doubleClick);
		}
		int hy = this.py + 8;
		if (Ui.in(mx, my, this.prevX(), hy, Ui.buttonW(this.font, "<"), 16)) {
			this.switchProject(-1);
			return true;
		}
		if (Ui.in(mx, my, this.nextX(), hy, Ui.buttonW(this.font, ">"), 16)) {
			this.switchProject(1);
			return true;
		}
		if (Ui.in(mx, my, this.editX(), hy, Ui.buttonW(this.font, T.t("edit")), 16) && Projects.active() != null) {
			mc.gui.setScreen(new EditScreen(Projects.active(), this));
			return true;
		}
		if (Ui.in(mx, my, this.newX(), hy, Ui.buttonW(this.font, T.t("new")), 16)) {
			Projects.Project p = Projects.create(T.t("project_n", Projects.all().size() + 1));
			mc.gui.setScreen(new EditScreen(p, this));
			return true;
		}
		if (Ui.in(mx, my, this.hudX(), this.rowY(), Ui.toggleW(this.font, T.t("hud")), 16)) {
			Settings.toggleHud();
			return true;
		}
		if (Ui.in(mx, my, this.doneX(), this.rowY(), Ui.toggleW(this.font, T.t("show_done")), 16)) {
			Settings.toggleShowDone();
			this.refresh();
			return true;
		}
		int fx = this.x1() - 10 - this.font.width(this.forgetLabel());
		if (Ui.in(mx, my, fx - 4, this.py + this.ph - 18, this.x1() - 6 - (fx - 4), 14)) {
			if (this.confirmForget) {
				Storage.forget(mc);
				this.confirmForget = false;
				Stand.update(mc);
				this.refresh();
			} else {
				this.confirmForget = true;
			}
			return true;
		}
		this.confirmForget = false;
		return super.mouseClicked(event, doubleClick);
	}

	private void switchProject(int dir) {
		Projects.cycle(dir);
		Stand.update(this.minecraft);
		this.scroll = 0;
		this.refresh();
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		this.scroll -= scrollY * ROW * 2;
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (GatherlistClient.OPEN.matches(event)) {
			this.onClose();
			return true;
		}
		if (event.key() == 259 && this.search.backspace()) {
			this.scroll = 0;
			this.refresh();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		String c = event.codepointAsString();
		if (event.isAllowedChatCharacter() && !GatherlistClient.isOpenKeyChar(c) && this.search.type(c)) {
			this.scroll = 0;
			this.refresh();
			return true;
		}
		return super.charTyped(event);
	}

	@Override
	public void onClose() {
		Projects.save();
		super.onClose();
	}
}

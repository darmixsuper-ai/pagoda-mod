package dev.pagoda.client;

import dev.pagoda.PagodaList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Das kleine Pagoda-Fenster. Oeffnen und schliessen mit der Taste rechts neben der 0. */
public class PagodaScreen extends Screen {
	private static final int GOLD = 0xFFE8B04A;
	private static final int ROW = 20;

	private record Row(ItemStack icon, String name, int have, int need) {
	}

	private final List<Row> rows = new ArrayList<>();
	private int done;
	private int total;
	private int chestCount;
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
		this.pw = Math.min(300, this.width - 20);
		this.ph = Math.min(260, this.height - 20);
		this.px = (this.width - this.pw) / 2;
		this.py = (this.height - this.ph) / 2;
		this.refresh();
	}

	private void refresh() {
		Minecraft mc = Minecraft.getInstance();
		this.rows.clear();
		this.done = 0;
		this.total = PagodaList.TARGETS.size();
		this.chestCount = Lager.chests(mc).size();
		if (mc.player == null) {
			return;
		}
		for (Map.Entry<Item, Integer> e : PagodaList.TARGETS.entrySet()) {
			int have = Lager.have(mc, e.getKey());
			if (have >= e.getValue()) {
				this.done++;
				continue;
			}
			ItemStack icon = new ItemStack(e.getKey());
			this.rows.add(new Row(icon, icon.getHoverName().getString(), have, e.getValue()));
		}
	}

	private int listTop() {
		return this.py + 46;
	}

	private int listBottom() {
		return this.py + this.ph - 22;
	}

	private int maxScroll() {
		return Math.max(0, this.rows.size() * ROW - (this.listBottom() - this.listTop()));
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
		int x1 = this.px + this.pw;

		g.fill(x0, this.py, x1, this.py + this.ph, 0xE8141008);
		g.outline(x0, this.py, this.pw, this.ph, 0x60E8B04A);
		g.text(mc.font, "Pagoda – das fehlt noch", x0 + 10, this.py + 9, GOLD, true);
		String prog = this.done + " / " + this.total + " fertig";
		g.text(mc.font, prog, x1 - 10 - mc.font.width(prog), this.py + 9, 0xFFB8C99A, false);

		int barY = this.py + 24;
		g.fill(x0 + 10, barY, x1 - 10, barY + 6, 0xFF2E2A20);
		int fillW = (int) ((this.pw - 20) * (this.total == 0 ? 0 : (double) this.done / this.total));
		g.fill(x0 + 10, barY, x0 + 10 + fillW, barY + 6, 0xFF6FA03B);

		this.scroll = Math.max(0, Math.min(this.scroll, this.maxScroll()));
		if (this.rows.isEmpty()) {
			g.centeredText(mc.font, "Du hast alles für die Pagoda!", this.width / 2, this.listTop() + 30, GOLD);
		}
		g.enableScissor(x0 + 2, this.listTop(), x1 - 2, this.listBottom());
		for (int i = 0; i < this.rows.size(); i++) {
			Row r = this.rows.get(i);
			int y = this.listTop() + i * ROW - (int) this.scroll;
			if (y + ROW < this.listTop() || y > this.listBottom()) {
				continue;
			}
			if (i % 2 == 0) {
				g.fill(x0 + 6, y, x1 - 6, y + ROW, 0x18FFFFFF);
			}
			g.item(r.icon(), x0 + 10, y + 2);
			int fehlt = r.need() - r.have();
			String right = "fehlt " + fehlt;
			String small = r.have() + "/" + r.need();
			int rw = mc.font.width(right);
			int sw = mc.font.width(small);
			int nameW = this.pw - 52 - rw - sw - 16;
			g.text(mc.font, mc.font.plainSubstrByWidth(r.name(), nameW), x0 + 32, y + 6, 0xFFFFFFFF, false);
			g.text(mc.font, small, x1 - 12 - rw - 8 - sw, y + 6, 0xFF8A8270, false);
			g.text(mc.font, right, x1 - 12 - rw, y + 6, 0xFFF2C14E, false);
		}
		g.disableScissor();

		String info = "Gezählt: Inventar + " + this.chestCount + (this.chestCount == 1 ? " Truhe" : " Truhen");
		g.text(mc.font, info, x0 + 10, this.py + this.ph - 14, 0xFF8A8270, false);
		String btn = this.confirmForget ? "Sicher? Nochmal klicken" : "Truhen vergessen";
		int bw = mc.font.width(btn);
		int bx = x1 - 10 - bw;
		boolean hover = mx >= bx - 4 && mx <= x1 - 6 && my >= this.py + this.ph - 18 && my <= this.py + this.ph - 4;
		g.text(mc.font, btn, bx, this.py + this.ph - 14, this.confirmForget ? 0xFFFF6B5A : (hover ? 0xFFFFFFFF : 0xFFB0A890), false);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		Minecraft mc = this.minecraft;
		String btn = this.confirmForget ? "Sicher? Nochmal klicken" : "Truhen vergessen";
		int bx = this.px + this.pw - 10 - mc.font.width(btn);
		if (event.button() == 0 && event.x() >= bx - 4 && event.x() <= this.px + this.pw - 6
				&& event.y() >= this.py + this.ph - 18 && event.y() <= this.py + this.ph - 4) {
			if (this.confirmForget) {
				Lager.forget(mc);
				this.confirmForget = false;
				this.refresh();
			} else {
				this.confirmForget = true;
			}
			return true;
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
		return super.keyPressed(event);
	}
}

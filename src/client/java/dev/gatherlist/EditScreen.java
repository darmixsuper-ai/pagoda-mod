package dev.gatherlist;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Projekt bearbeiten: Name, Items suchen und hinzufuegen, Mengen aendern, Liste einfuegen. */
public class EditScreen extends Screen {
	private static final int ROW = 18;

	private record Pick(ItemStack icon, String name, String id) {
	}

	private static List<Pick> allItems;

	private final Projects.Project project;
	private final Screen parent;
	private final TextBox name = new TextBox(40, false, "");
	private final TextBox search = new TextBox(32, false, T.t("search_items"));
	private final TextBox amount = new TextBox(7, true, "");
	private final List<Pick> results = new ArrayList<>();
	private String editing;
	private double scrollLeft;
	private double scrollRight;
	private boolean confirmDelete;
	private String status = "";
	private int statusColor = Ui.MUTED;
	private int px;
	private int py;
	private int pw;
	private int ph;

	public EditScreen(Projects.Project project, Screen parent) {
		super(Component.literal("Gatherlist"));
		this.project = project;
		this.parent = parent;
		this.name.text = project.name;
		this.search.focused = true;
	}

	private static List<Pick> allItems() {
		if (allItems == null) {
			allItems = new ArrayList<>();
			for (Item item : BuiltInRegistries.ITEM) {
				if (item == Items.AIR) {
					continue;
				}
				ItemStack st = new ItemStack(item);
				allItems.add(new Pick(st, st.getHoverName().getString(), Projects.id(item)));
			}
		}
		return allItems;
	}

	@Override
	protected void init() {
		this.pw = Math.min(420, this.width - 20);
		this.ph = Math.min(300, this.height - 20);
		this.px = (this.width - this.pw) / 2;
		this.py = (this.height - this.ph) / 2;
		this.filter();
	}

	private void filter() {
		this.results.clear();
		String q = this.search.text.toLowerCase(Locale.ROOT).trim();
		for (Pick p : allItems()) {
			if (q.isEmpty() || p.name().toLowerCase(Locale.ROOT).contains(q) || p.id().contains(q.replace(' ', '_'))) {
				this.results.add(p);
			}
		}
		this.scrollLeft = 0;
	}

	// ---- Positionen ----
	private int colW() {
		return (this.pw - 30) / 2;
	}

	private int leftX() {
		return this.px + 10;
	}

	private int rightX() {
		return this.px + 20 + this.colW();
	}

	private int top() {
		return this.py + 66;
	}

	private int bottom() {
		return this.py + this.ph - 44;
	}

	private int footY() {
		return this.py + this.ph - 24;
	}

	private List<Map.Entry<String, Integer>> entries() {
		return new ArrayList<>(this.project.items.entrySet());
	}

	private static boolean shift() {
		Minecraft mc = Minecraft.getInstance();
		return InputConstants.isKeyDown(mc.getWindow(), 340) || InputConstants.isKeyDown(mc.getWindow(), 344);
	}

	private void add(String id, int n) {
		this.project.items.merge(id, n, Integer::sum);
		Projects.save();
	}

	private void change(String id, int delta) {
		Integer cur = this.project.items.get(id);
		if (cur == null) {
			return;
		}
		this.project.items.put(id, Math.max(1, cur + delta));
		Projects.save();
	}

	private void applyAmount() {
		if (this.editing != null) {
			try {
				int n = Integer.parseInt(this.amount.text);
				if (n > 0) {
					this.project.items.put(this.editing, n);
				}
			} catch (NumberFormatException ignored) {
				// leer gelassen: alte Menge bleibt
			}
			Projects.save();
		}
		this.editing = null;
		this.amount.focused = false;
		this.search.focused = true;
	}

	private String pasteLabel() {
		return T.t("paste");
	}

	private String deleteLabel() {
		return this.confirmDelete ? T.t("delete_confirm") : T.t("delete");
	}

	private int pasteX() {
		return this.px + 10;
	}

	private int deleteX() {
		return this.pasteX() + Ui.buttonW(this.font, this.pasteLabel()) + 6;
	}

	private int doneX() {
		return this.px + this.pw - 10 - Ui.buttonW(this.font, T.t("finish"));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float a) {
		super.extractRenderState(g, mx, my, a);
		int x0 = this.px;
		int x1 = this.px + this.pw;
		Ui.panel(g, x0, this.py, this.pw, this.ph);
		g.text(this.font, T.t("edit_title"), x0 + 10, this.py + 9, Ui.GOLD, true);

		g.text(this.font, T.t("name"), x0 + 10, this.py + 29, Ui.MUTED, false);
		int nx = x0 + 14 + this.font.width(T.t("name"));
		this.name.place(nx, this.py + 25, x1 - 10 - nx);
		this.name.render(g, this.font);

		int lx = this.leftX();
		int rx = this.rightX();
		int cw = this.colW();
		this.search.place(lx, this.py + 46, cw);
		this.search.render(g, this.font);
		String count = T.t("in_project", this.project.items.size());
		g.text(this.font, count, rx, this.py + 50, Ui.MUTED, false);

		// Links: alle Items
		this.scrollLeft = Math.max(0, Math.min(this.scrollLeft, Math.max(0, this.results.size() * ROW - (this.bottom() - this.top()))));
		g.enableScissor(lx, this.top(), lx + cw, this.bottom());
		for (int i = 0; i < this.results.size(); i++) {
			int y = this.top() + i * ROW - (int) this.scrollLeft;
			if (y + ROW < this.top() || y > this.bottom()) {
				continue;
			}
			Pick p = this.results.get(i);
			boolean hover = Ui.in(mx, my, lx, y, cw, ROW) && my >= this.top() && my < this.bottom();
			g.fill(lx, y, lx + cw, y + ROW, hover ? 0x30FFFFFF : (i % 2 == 0 ? 0x12FFFFFF : 0));
			g.item(p.icon(), lx + 2, y + 1);
			g.text(this.font, this.font.plainSubstrByWidth(p.name(), cw - 40), lx + 22, y + 5, Ui.TEXT, false);
			if (hover) {
				g.text(this.font, "+", lx + cw - 10, y + 5, Ui.GREEN, false);
			}
		}
		g.disableScissor();

		// Rechts: Items im Projekt
		List<Map.Entry<String, Integer>> list = this.entries();
		this.scrollRight = Math.max(0, Math.min(this.scrollRight, Math.max(0, list.size() * ROW - (this.bottom() - this.top()))));
		g.fill(rx, this.top(), rx + cw, this.bottom(), 0x10FFFFFF);
		if (list.isEmpty()) {
			g.centeredText(this.font, T.t("add_hint"), rx + cw / 2, this.top() + 20, Ui.MUTED);
		}
		g.enableScissor(rx, this.top(), rx + cw, this.bottom());
		for (int i = 0; i < list.size(); i++) {
			int y = this.top() + i * ROW - (int) this.scrollRight;
			if (y + ROW < this.top() || y > this.bottom()) {
				continue;
			}
			Map.Entry<String, Integer> e = list.get(i);
			Item item = Projects.item(e.getKey());
			ItemStack st = new ItemStack(item);
			g.item(st, rx + 2, y + 1);
			// Knoepfe rechts: - + x
			int bx = rx + cw - 42;
			g.text(this.font, "-", bx + 3, y + 5, Ui.in(mx, my, bx, y, 12, ROW) ? Ui.TEXT : Ui.MUTED, false);
			g.text(this.font, "+", bx + 17, y + 5, Ui.in(mx, my, bx + 14, y, 12, ROW) ? Ui.TEXT : Ui.MUTED, false);
			g.text(this.font, "x", bx + 31, y + 5, Ui.in(mx, my, bx + 28, y, 12, ROW) ? Ui.RED : Ui.MUTED, false);
			int ax = bx - 46;
			if (e.getKey().equals(this.editing)) {
				this.amount.place(ax, y + 1, 44);
				this.amount.render(g, this.font);
			} else {
				String n = String.valueOf(e.getValue());
				boolean hover = Ui.in(mx, my, ax, y, 44, ROW);
				g.text(this.font, n, ax + 44 - this.font.width(n) - 2, y + 5, hover ? Ui.TEXT : Ui.WARN, false);
			}
			String label = item == Items.AIR ? e.getKey() : st.getHoverName().getString();
			g.text(this.font, this.font.plainSubstrByWidth(label, ax - rx - 26), rx + 22, y + 5, Ui.TEXT, false);
		}
		g.disableScissor();

		// Fusszeile
		g.text(this.font, this.status, x0 + 10, this.bottom() + 6, this.statusColor, false);
		int fy = this.footY();
		Ui.button(g, this.font, this.pasteLabel(), this.pasteX(), fy, mx, my, Ui.ACCENT);
		Ui.button(g, this.font, this.deleteLabel(), this.deleteX(), fy, mx, my, this.confirmDelete ? Ui.RED : Ui.MUTED);
		Ui.button(g, this.font, T.t("finish"), this.doneX(), fy, mx, my, Ui.GREEN);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x();
		double my = event.y();
		if (event.button() != 0) {
			return super.mouseClicked(event, doubleClick);
		}
		boolean wasDelete = this.confirmDelete;
		this.confirmDelete = false;

		if (this.editing != null && !this.amount.contains(mx, my)) {
			this.applyAmount();
		}
		if (this.name.contains(mx, my)) {
			this.name.focused = true;
			this.search.focused = false;
			return true;
		}
		if (this.search.contains(mx, my)) {
			this.search.focused = true;
			this.name.focused = false;
			return true;
		}
		this.saveName();

		int lx = this.leftX();
		int rx = this.rightX();
		int cw = this.colW();
		if (my >= this.top() && my < this.bottom()) {
			if (mx >= lx && mx < lx + cw) {
				int i = (int) ((my - this.top() + this.scrollLeft) / ROW);
				if (i >= 0 && i < this.results.size()) {
					Pick p = this.results.get(i);
					this.add(p.id(), shift() ? 64 : 1);
					this.status = T.t("added", p.name());
					this.statusColor = Ui.GREEN;
				}
				return true;
			}
			if (mx >= rx && mx < rx + cw) {
				List<Map.Entry<String, Integer>> list = this.entries();
				int i = (int) ((my - this.top() + this.scrollRight) / ROW);
				if (i >= 0 && i < list.size()) {
					String id = list.get(i).getKey();
					int bx = rx + cw - 42;
					int ax = bx - 46;
					int step = shift() ? 64 : 1;
					if (mx >= bx && mx < bx + 12) {
						this.change(id, -step);
					} else if (mx >= bx + 14 && mx < bx + 26) {
						this.change(id, step);
					} else if (mx >= bx + 28 && mx < bx + 40) {
						this.project.items.remove(id);
						Projects.save();
					} else if (mx >= ax && mx < ax + 44) {
						this.editing = id;
						this.amount.text = "";
						this.amount.focused = true;
						this.search.focused = false;
						this.name.focused = false;
					}
				}
				return true;
			}
		}

		int fy = this.footY();
		if (Ui.in(mx, my, this.pasteX(), fy, Ui.buttonW(this.font, this.pasteLabel()), 16)) {
			this.paste();
			return true;
		}
		if (Ui.in(mx, my, this.deleteX(), fy, Ui.buttonW(this.font, this.deleteLabel()), 16)) {
			if (wasDelete) {
				Projects.delete(this.project);
				this.minecraft.gui.setScreen(this.parent);
			} else {
				this.confirmDelete = true;
			}
			return true;
		}
		if (Ui.in(mx, my, this.doneX(), fy, Ui.buttonW(this.font, T.t("finish")), 16)) {
			this.onClose();
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	private void paste() {
		String text = this.minecraft.keyboardHandler.getClipboard();
		if (text == null || text.isBlank()) {
			this.status = T.t("paste_empty");
			this.statusColor = Ui.WARN;
			return;
		}
		ListImport.Result r = ListImport.into(this.project, text);
		Projects.save();
		this.status = r.unknown() > 0 ? T.t("paste_result_unknown", r.added(), r.unknown()) : T.t("paste_result", r.added());
		this.statusColor = r.added() > 0 ? Ui.GREEN : Ui.WARN;
	}

	private void saveName() {
		if (this.name.focused) {
			this.name.focused = false;
			this.search.focused = true;
		}
		this.project.name = this.name.text.trim();
		Projects.save();
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if (x >= this.rightX()) {
			this.scrollRight -= scrollY * ROW * 2;
		} else {
			this.scrollLeft -= scrollY * ROW * 2;
		}
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int k = event.key();
		if (k == 257 || k == 335) {
			if (this.editing != null) {
				this.applyAmount();
			} else if (this.name.focused) {
				this.saveName();
			}
			return true;
		}
		if (k == 259) {
			if (this.amount.backspace() || this.name.backspace()) {
				return true;
			}
			if (this.search.backspace()) {
				this.filter();
				return true;
			}
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (!event.isAllowedChatCharacter()) {
			return super.charTyped(event);
		}
		String c = event.codepointAsString();
		if (this.amount.type(c) || this.name.type(c)) {
			return true;
		}
		if (this.search.type(c)) {
			this.filter();
			return true;
		}
		return super.charTyped(event);
	}

	@Override
	public void onClose() {
		this.applyAmount();
		this.saveName();
		Stand.update(this.minecraft);
		this.minecraft.gui.setScreen(this.parent);
	}
}

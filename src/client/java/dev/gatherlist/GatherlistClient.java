package dev.gatherlist;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatherlistClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("gatherlist");
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("gatherlist", "main"));
	/** Standard: die Taste rechts neben der 0 (US: "-", CH/DE: "'" bzw. "ß"). Aenderbar unter Steuerung. */
	public static final KeyMapping OPEN = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.gatherlist.open", InputConstants.Type.KEYSYM, InputConstants.KEY_MINUS, CATEGORY));
	private static final int HUD_ROWS = 5;
	private static int timer;

	@Override
	public void onInitializeClient() {
		Settings.load();
		Projects.load();
		Storage.load();
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			Storage.tick(mc);
			if (mc.player != null && ++timer >= 10) {
				timer = 0;
				Stand.update(mc);
			}
			while (OPEN.consumeClick()) {
				if (mc.player != null && mc.gui.screen() == null) {
					mc.gui.setScreen(new ChecklistScreen());
				}
			}
		});
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("gatherlist", "hud"), (g, deltaTracker) -> renderHud(g));
	}

	/** Zeichen, die die Oeffnen-Taste tippt; die gehoeren nicht in die Suche. */
	public static boolean isOpenKeyChar(String c) {
		return c.equals("'") || c.equals("?");
	}

	private static void renderHud(GuiGraphicsExtractor g) {
		Minecraft mc = Minecraft.getInstance();
		Projects.Project p = Projects.active();
		if (!Settings.hud() || mc.player == null || p == null || Stand.total() == 0
				|| mc.gui.screen() instanceof ChecklistScreen || mc.gui.screen() instanceof EditScreen) {
			return;
		}
		int w = 160;
		int x = mc.getWindow().getGuiScaledWidth() - w - 6;
		int y = 6;
		int shown = 0;
		for (Stand.Entry e : Stand.entries()) {
			if (!e.done() && shown < HUD_ROWS) {
				shown++;
			}
		}
		int h = 22 + shown * 18 + (shown > 0 ? 2 : 0);
		g.fill(x, y, x + w, y + h, 0x90000000);
		String prog = Stand.done() + "/" + Stand.total();
		int pwid = mc.font.width(prog);
		String title = p.name.isBlank() ? T.t("unnamed") : p.name;
		g.text(mc.font, mc.font.plainSubstrByWidth(title, w - 14 - pwid), x + 5, y + 4, Ui.GOLD, true);
		g.text(mc.font, prog, x + w - 5 - pwid, y + 4, Ui.MUTED, false);
		Ui.progress(g, x + 5, y + 15, w - 10, 3, Stand.done(), Stand.total());
		int row = 0;
		for (Stand.Entry e : Stand.entries()) {
			if (e.done()) {
				continue;
			}
			if (row >= HUD_ROWS) {
				break;
			}
			int ry = y + 22 + row * 18;
			g.item(e.icon(), x + 4, ry);
			String miss = "-" + e.missing();
			int mw = mc.font.width(miss);
			g.text(mc.font, mc.font.plainSubstrByWidth(e.name(), w - 30 - mw - 6), x + 24, ry + 5, Ui.TEXT, false);
			g.text(mc.font, miss, x + w - 5 - mw, ry + 5, Ui.WARN, false);
			row++;
		}
	}
}

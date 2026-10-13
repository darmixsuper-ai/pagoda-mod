package dev.pagoda.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class PagodaClient implements ClientModInitializer {
	/** Die Taste rechts neben der 0 (auf der Schweizer Tastatur ' und ?). */
	public static final int KEY = 45;
	private static final int HUD_ROWS = 5;
	private static boolean lastDown;
	private static int timer;

	@Override
	public void onInitializeClient() {
		Settings.load();
		Lager.load();
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			Lager.tick(mc);
			if (mc.player != null && ++timer >= 10) {
				timer = 0;
				Stand.update(mc);
			}
			boolean down = InputConstants.isKeyDown(mc.getWindow(), KEY);
			if (down && !lastDown && mc.player != null && mc.gui.screen() == null) {
				Stand.update(mc);
				mc.gui.setScreen(new PagodaScreen());
			}
			lastDown = down;
		});
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("pagoda", "hud"), (g, deltaTracker) -> renderHud(g));
	}

	private static void renderHud(GuiGraphicsExtractor g) {
		Minecraft mc = Minecraft.getInstance();
		if (!Settings.hud() || mc.player == null || mc.gui.screen() instanceof PagodaScreen) {
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
		String title = "Pagoda";
		String prog = Stand.done() + "/" + Stand.total();
		g.text(mc.font, title, x + 5, y + 4, 0xFFE8B04A, true);
		g.text(mc.font, prog, x + w - 5 - mc.font.width(prog), y + 4, 0xFFB8C99A, false);
		g.fill(x + 5, y + 15, x + w - 5, y + 18, 0xFF2E2A20);
		int fw = (int) ((w - 10) * (Stand.total() == 0 ? 0 : (double) Stand.done() / Stand.total()));
		g.fill(x + 5, y + 15, x + 5 + fw, y + 18, 0xFF6FA03B);
		if (shown == 0) {
			return;
		}
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
			g.text(mc.font, mc.font.plainSubstrByWidth(e.name(), w - 30 - mw - 6), x + 24, ry + 5, 0xFFFFFFFF, false);
			g.text(mc.font, miss, x + w - 5 - mw, ry + 5, 0xFFF2C14E, false);
			row++;
		}
	}
}

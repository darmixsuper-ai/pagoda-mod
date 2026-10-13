package dev.pagoda.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class PagodaClient implements ClientModInitializer {
	/** Die Taste rechts neben der 0 (auf der Schweizer Tastatur ' und ?). */
	public static final int KEY = 45;
	private static boolean lastDown;

	@Override
	public void onInitializeClient() {
		Lager.load();
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			Lager.tick(mc);
			boolean down = InputConstants.isKeyDown(mc.getWindow(), KEY);
			if (down && !lastDown && mc.player != null && mc.gui.screen() == null) {
				mc.gui.setScreen(new PagodaScreen());
			}
			lastDown = down;
		});
	}

	static boolean keyIsDown(Minecraft mc) {
		return InputConstants.isKeyDown(mc.getWindow(), KEY);
	}
}

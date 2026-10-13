package dev.pagoda.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.pagoda.PagodaLogic;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Merkt sich, was in den Truhen liegt, die du geoeffnet hast (pro Server bzw. Welt).
 * Alles laeuft nur bei dir im Spiel, der Server merkt davon nichts.
 */
public final class Lager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	/** Welt/Server -> Truhe -> Item-ID -> Anzahl */
	private static Map<String, Map<String, Map<String, Integer>>> data = new HashMap<>();
	private static String openKey;
	private static boolean dirty;

	private Lager() {
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("pagoda-lager.json");
	}

	public static void load() {
		try {
			Path p = file();
			if (Files.exists(p)) {
				Map<String, Map<String, Map<String, Integer>>> d = GSON.fromJson(Files.readString(p),
						new TypeToken<Map<String, Map<String, Map<String, Integer>>>>() { }.getType());
				if (d != null) {
					data = d;
				}
			}
		} catch (Exception e) {
			dev.pagoda.PagodaMod.LOGGER.warn("Pagoda: Lager konnte nicht geladen werden", e);
		}
	}

	public static void save() {
		try {
			Files.writeString(file(), GSON.toJson(data));
			dirty = false;
		} catch (Exception e) {
			dev.pagoda.PagodaMod.LOGGER.warn("Pagoda: Lager konnte nicht gespeichert werden", e);
		}
	}

	static String world(Minecraft mc) {
		ServerData sd = mc.getCurrentServer();
		if (sd != null) {
			return "server:" + sd.ip;
		}
		if (mc.getSingleplayerServer() != null) {
			return "welt:" + mc.getSingleplayerServer().getWorldData().getLevelName();
		}
		return "unbekannt";
	}

	static Map<String, Map<String, Integer>> chests(Minecraft mc) {
		return data.computeIfAbsent(world(mc), k -> new HashMap<>());
	}

	static void forget(Minecraft mc) {
		data.remove(world(mc));
		save();
	}

	private static boolean isStorage(AbstractContainerMenu menu) {
		return menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu;
	}

	/** Welche Truhe gerade offen ist: die, auf die du beim Oeffnen geschaut hast. */
	private static String keyFor(Minecraft mc) {
		HitResult hit = mc.hitResult;
		if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK || mc.level == null) {
			return "";
		}
		BlockPos pos = bhr.getBlockPos();
		BlockState state = mc.level.getBlockState(pos);
		if (state.getBlock() instanceof EnderChestBlock) {
			return "endertruhe";
		}
		if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
			BlockPos other = pos.relative(ChestBlock.getConnectedDirection(state));
			if (other.asLong() < pos.asLong()) {
				pos = other;
			}
		}
		return mc.level.dimension() + "@" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
	}

	public static void tick(Minecraft mc) {
		Screen screen = mc.gui.screen();
		if (mc.player != null && screen instanceof AbstractContainerScreen<?> cs && isStorage(cs.getMenu())) {
			if (openKey == null) {
				openKey = keyFor(mc);
			}
			if (!openKey.isEmpty()) {
				snapshot(mc, cs.getMenu());
			}
			return;
		}
		openKey = null;
		if (dirty) {
			save();
		}
	}

	private static void snapshot(Minecraft mc, AbstractContainerMenu menu) {
		Map<String, Integer> counts = new HashMap<>();
		for (Slot slot : menu.slots) {
			if (slot.container == mc.player.getInventory()) {
				continue;
			}
			ItemStack st = slot.getItem();
			if (st.isEmpty()) {
				continue;
			}
			counts.merge(id(st.getItem()), st.getCount(), Integer::sum);
			if (PagodaLogic.isShulker(st)) {
				for (ItemStack inner : PagodaLogic.contents(st)) {
					if (!inner.isEmpty()) {
						counts.merge(id(inner.getItem()), inner.getCount(), Integer::sum);
					}
				}
			}
		}
		Map<String, Map<String, Integer>> chests = chests(mc);
		if (!counts.equals(chests.get(openKey))) {
			chests.put(openKey, counts);
			dirty = true;
		}
	}

	static String id(Item item) {
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}

	/** Wie viele du insgesamt hast: Inventar (mit Shulker-Kisten) + alle gemerkten Truhen. */
	static int have(Minecraft mc, Item item) {
		int n = PagodaLogic.countContainer(mc.player.getInventory(), item);
		String id = id(item);
		for (Map<String, Integer> c : chests(mc).values()) {
			n += c.getOrDefault(id, 0);
		}
		return n;
	}
}

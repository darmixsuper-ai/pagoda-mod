package dev.gatherlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
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
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Merkt sich, was in den Truhen liegt, die du geoeffnet hast (pro Server bzw. Welt).
 * Alles laeuft nur bei dir im Spiel, der Server merkt davon nichts.
 */
public final class Storage {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	/** Welt/Server -> Truhe -> Item-ID -> Anzahl */
	private static Map<String, Map<String, Map<String, Integer>>> data = new HashMap<>();
	private static String openKey;
	private static boolean dirty;
	private static int cleanupTimer;

	private Storage() {
	}

	public static void load() {
		java.lang.reflect.Type type = new TypeToken<Map<String, Map<String, Map<String, Integer>>>>() { }.getType();
		Map<String, Map<String, Map<String, Integer>>> d = Json.read("chests.json", type);
		if (d == null) {
			// Uebernimmt die Truhen aus der alten Pagoda-Version
			try {
				Path old = FabricLoader.getInstance().getConfigDir().resolve("pagoda-lager.json");
				if (Files.exists(old)) {
					d = GSON.fromJson(Files.readString(old), type);
				}
			} catch (Exception e) {
				GatherlistClient.LOGGER.warn("Gatherlist: alte Truhen konnten nicht uebernommen werden", e);
			}
		}
		if (d != null) {
			data = d;
		}
	}

	public static void save() {
		Json.write("chests.json", data);
		dirty = false;
	}

	public static String world(Minecraft mc) {
		ServerData sd = mc.getCurrentServer();
		if (sd != null) {
			return "server:" + sd.ip;
		}
		if (mc.getSingleplayerServer() != null) {
			return "welt:" + mc.getSingleplayerServer().getWorldData().getLevelName();
		}
		return "unbekannt";
	}

	public static Map<String, Map<String, Integer>> chests(Minecraft mc) {
		return data.computeIfAbsent(world(mc), k -> new HashMap<>());
	}

	public static void forget(Minecraft mc) {
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
		String suffix = "";
		if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
			BlockPos other = pos.relative(ChestBlock.getConnectedDirection(state));
			if (other.asLong() < pos.asLong()) {
				pos = other;
			}
			suffix = "|d";
		}
		return mc.level.dimension() + "@" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + suffix;
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
		if (mc.player != null && mc.level != null && ++cleanupTimer >= 5) {
			cleanupTimer = 0;
			cleanup(mc);
		}
		if (dirty) {
			save();
		}
	}

	private static boolean isStorageBlock(BlockState state) {
		return state.getBlock() instanceof ChestBlock || state.getBlock() instanceof BarrelBlock
				|| state.getBlock() instanceof ShulkerBoxBlock;
	}

	/**
	 * Vergisst Truhen, die abgebaut wurden (oder bei denen eine Haelfte der Doppeltruhe fehlt).
	 * Sonst wuerden die Items doppelt gezaehlt, sobald du sie wieder aufsammelst.
	 */
	private static void cleanup(Minecraft mc) {
		String dim = mc.level.dimension().toString();
		Iterator<Map.Entry<String, Map<String, Integer>>> it = chests(mc).entrySet().iterator();
		while (it.hasNext()) {
			String key = it.next().getKey();
			int at = key.lastIndexOf('@');
			if (at < 0 || !key.substring(0, at).equals(dim)) {
				continue;
			}
			boolean dbl = key.endsWith("|d");
			String[] xyz = key.substring(at + 1, dbl ? key.length() - 2 : key.length()).split(",");
			if (xyz.length != 3) {
				continue;
			}
			BlockPos pos;
			try {
				pos = new BlockPos(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
			} catch (NumberFormatException e) {
				continue;
			}
			if (!mc.level.isLoaded(pos)) {
				continue;
			}
			BlockState state = mc.level.getBlockState(pos);
			boolean ok = dbl
					? state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE
					: isStorageBlock(state);
			if (!ok) {
				it.remove();
				dirty = true;
			}
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
			if (Counter.isShulker(st)) {
				for (ItemStack inner : Counter.contents(st)) {
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

	public static String id(Item item) {
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}

	/** Wie viele du insgesamt hast: Inventar (mit Shulker-Kisten) + alle gemerkten Truhen. */
	public static int have(Minecraft mc, Item item) {
		int n = Counter.countContainer(mc.player.getInventory(), item);
		String id = id(item);
		for (Map<String, Integer> c : chests(mc).values()) {
			n += c.getOrDefault(id, 0);
		}
		return n;
	}
}

package dev.pagoda;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Alle Materialien aus Billies Kommentar, Innen und Aussen zusammengezaehlt. */
public final class PagodaList {
	public static final Map<Item, Integer> TARGETS = new LinkedHashMap<>();

	private PagodaList() {
	}

	private static void add(Item item, int count) {
		TARGETS.merge(item, count, Integer::sum);
	}

	/** Fuer Items, die es in dieser Version nicht als eigenes Feld in Items gibt. */
	private static void addId(String id, int count) {
		Item item = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(id));
		if (item == null || item == Items.AIR) {
			PagodaMod.LOGGER.warn("Pagoda: Item {} nicht gefunden", id);
			return;
		}
		add(item, count);
	}

	static {
		// Fichtenholz
		add(Items.SPRUCE_LOG, 2059);
		add(Items.STRIPPED_SPRUCE_LOG, 128);
		add(Items.SPRUCE_PLANKS, 2706);
		add(Items.SPRUCE_STAIRS, 188);
		add(Items.SPRUCE_SLAB, 12);
		add(Items.SPRUCE_FENCE, 4);
		add(Items.SPRUCE_DOOR, 6);
		add(Items.SPRUCE_TRAPDOOR, 284);
		add(Items.SPRUCE_SIGN, 96);
		add(Items.SPRUCE_PRESSURE_PLATE, 28);
		add(Items.SPRUCE_SAPLING, 4);
		// Schwarzeiche
		add(Items.DARK_OAK_STAIRS, 585);
		add(Items.DARK_OAK_SLAB, 8);
		add(Items.DARK_OAK_FENCE, 831);
		add(Items.DARK_OAK_FENCE_GATE, 160);
		add(Items.DARK_OAK_DOOR, 24);
		add(Items.DARK_OAK_TRAPDOOR, 156);
		add(Items.DARK_OAK_PRESSURE_PLATE, 28);
		add(Items.DARK_OAK_SAPLING, 4);
		// Anderes Holz
		add(Items.OAK_TRAPDOOR, 80);
		add(Items.JUNGLE_STAIRS, 64);
		add(Items.JUNGLE_FENCE, 8);
		add(Items.JUNGLE_FENCE_GATE, 48);
		add(Items.JUNGLE_TRAPDOOR, 241);
		add(Items.ACACIA_FENCE, 128);
		add(Items.LADDER, 5);
		add(Items.SCAFFOLDING, 4);
		// Stein
		add(Items.DEEPSLATE_BRICKS, 56);
		add(Items.DEEPSLATE_BRICK_STAIRS, 528);
		add(Items.DEEPSLATE_BRICK_SLAB, 197);
		add(Items.DEEPSLATE_BRICK_WALL, 607);
		add(Items.STONE, 73);
		add(Items.COBBLESTONE, 73);
		add(Items.ANDESITE, 73);
		add(Items.STONE_STAIRS, 11);
		add(Items.COBBLESTONE_STAIRS, 12);
		add(Items.ANDESITE_STAIRS, 11);
		add(Items.DEAD_BRAIN_CORAL_BLOCK, 48);
		// Licht und Glas
		add(Items.LANTERN, 380);
		add(Items.GLOWSTONE, 211);
		add(Items.GLASS_PANE, 192);
		add(Items.CAMPFIRE, 16);
		add(Items.TORCH, 4);
		addId("blue_candle", 12);
		add(Items.IRON_CHAIN, 2);
		// Truhen und Arbeitsbloecke
		add(Items.CHEST, 129);
		add(Items.BARREL, 157);
		add(Items.ENDER_CHEST, 2);
		add(Items.BOOKSHELF, 79);
		add(Items.GRINDSTONE, 48);
		add(Items.CRAFTING_TABLE, 1);
		add(Items.SMOKER, 12);
		add(Items.BLAST_FURNACE, 12);
		add(Items.STONECUTTER, 2);
		add(Items.SMITHING_TABLE, 4);
		add(Items.LOOM, 2);
		add(Items.ANVIL, 4);
		add(Items.ENCHANTING_TABLE, 1);
		// Teppich und Wolle
		addId("white_carpet", 24);
		addId("lime_carpet", 28);
		addId("pink_carpet", 4);
		addId("purple_carpet", 16);
		addId("red_carpet", 16);
		addId("black_carpet", 32);
		addId("black_wool", 4);
		// Deko
		add(Items.FLOWER_POT, 52);
		add(Items.GLOW_ITEM_FRAME, 32);
		add(Items.ITEM_FRAME, 24);
		add(Items.ARMOR_STAND, 24);
		addId("blue_bed", 1);
		add(Items.DRAGON_HEAD, 2);
		add(Items.SPYGLASS, 4);
		add(Items.COMPASS, 4);
		add(Items.CAKE, 1);
		add(Items.MELON, 1);
		add(Items.GLOW_BERRIES, 1);
		add(Items.WATER_BUCKET, 2);
		// Pflanzen
		add(Items.GRASS_BLOCK, 165);
		add(Items.OAK_LEAVES, 36);
		add(Items.SUGAR_CANE, 132);
		add(Items.BAMBOO, 86);
		add(Items.CARROT, 64);
		add(Items.SWEET_BERRIES, 25);
		add(Items.SEA_PICKLE, 6);
		add(Items.DEAD_BRAIN_CORAL, 4);
		add(Items.SEAGRASS, 1);
		add(Items.FERN, 1);
		add(Items.LILY_PAD, 1);
		// Ruestung und Extras
		add(Items.ENCHANTED_BOOK, 1);
		add(Items.GOLDEN_APPLE, 1);
		add(Items.IRON_HELMET, 1);
		add(Items.IRON_CHESTPLATE, 1);
		add(Items.IRON_BOOTS, 1);
		add(Items.GOLDEN_HELMET, 1);
		add(Items.GOLDEN_CHESTPLATE, 1);
		add(Items.GOLDEN_BOOTS, 1);
		add(Items.DIAMOND_HELMET, 1);
		add(Items.DIAMOND_CHESTPLATE, 1);
		add(Items.DIAMOND_BOOTS, 1);
		add(Items.NETHERITE_HELMET, 1);
	}
}

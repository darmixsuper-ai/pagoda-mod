package dev.pagoda;

import java.util.Map;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PagodaMod implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("pagoda").executes(ctx -> showList(ctx.getSource()))));
	}

	private static int showList(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		int done = 0;
		player.sendSystemMessage(Component.literal("★ Das fehlt dir noch für die Pagoda:").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
		for (Map.Entry<Item, Integer> e : PagodaList.TARGETS.entrySet()) {
			int have = PagodaLogic.countEverywhere(player, e.getKey());
			int need = e.getValue() - have;
			if (need <= 0) {
				done++;
				continue;
			}
			player.sendSystemMessage(Component.literal(" ✘ ").withStyle(ChatFormatting.RED)
					.append(new ItemStack(e.getKey()).getHoverName().copy().withStyle(ChatFormatting.WHITE))
					.append(Component.literal("  fehlt ").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(String.valueOf(need)).withStyle(ChatFormatting.YELLOW))
					.append(Component.literal("  (" + have + "/" + e.getValue() + ")").withStyle(ChatFormatting.DARK_GRAY)));
		}
		int total = PagodaList.TARGETS.size();
		player.sendSystemMessage(Component.literal(" ✔ " + done + " von " + total + " fertig").withStyle(ChatFormatting.GREEN));
		if (done == total) {
			player.sendSystemMessage(Component.literal(" ★ Du hast alles für die Pagoda!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
		}
		player.sendSystemMessage(Component.literal(" Gezählt wird: Inventar, Endertruhe und alle Shulker-Kisten darin.").withStyle(ChatFormatting.DARK_GRAY));
		return done;
	}
}

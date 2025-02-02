package bluemoonjune.atomic.command;

import bluemoonjune.atomic.Atomic;
import bluemoonjune.atomic.network.PacketToggleFeature;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.server.MinecraftServer;

public class CommandAtomic implements CommandManager.CommandRegistry {

	@Override
	public void register(CommandDispatcher<CommandSource> commandDispatcher) {
		LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.<CommandSource>literal("atomic");

		for (String feature : Atomic.FEATURES.keySet()) {
			RequiredArgumentBuilder<CommandSource, Boolean> featureValueArgument;
			featureValueArgument = RequiredArgumentBuilder.<CommandSource, Boolean>argument("value", BoolArgumentType.bool()).requires(CommandSource::hasAdmin).executes((c) -> {

				boolean v = c.getArgument("value", Boolean.class);
				Atomic.FEATURES.put(feature, v);
				c.getSource().sendMessage(String.format("%s Atomic feature %s.", v ? "Enabled" : "Disabled", feature));
				return 1;
			});

			builder.then(LiteralArgumentBuilder.<CommandSource>literal(feature).executes((c) -> {
				boolean v = Atomic.FEATURES.get(feature);
				c.getSource().sendMessage(String.format("%s is %s", feature, v ? "§5Enabled" : "§eDisabled"));
				return 1;
			}).then(featureValueArgument));

		}

		builder.then(LiteralArgumentBuilder.<CommandSource>literal("all").executes((c) -> {
			for (String feature : Atomic.FEATURES.keySet()) {
				boolean v = Atomic.FEATURES.get(feature);
				c.getSource().sendMessage(String.format("%s: %s", feature, v ? "§3Enabled" : "§2Disabled"));
			}
			return 1;
		}).then(RequiredArgumentBuilder.<CommandSource, Boolean>argument("value", BoolArgumentType.bool()).requires(CommandSource::hasAdmin).executes((c) -> {
			int changed = 0;
			boolean v = c.getArgument("value", Boolean.class);
			for (String feature : Atomic.FEATURES.keySet()) {
				if (Atomic.FEATURES.get(feature) != v) {
					Atomic.FEATURES.put(feature, v);
					if (!c.getSource().getWorld().isClientSide) {
						MinecraftServer.getInstance().playerList.sendPacketToAllPlayers(new PacketToggleFeature(feature, v));
					}
					changed++;
				}
			}
			c.getSource().sendMessage(String.format(changed > 0 ? "%s %d Atomic features." : "No Atomic features were %s", v ? "Enabled" : "Disabled", changed));
			return 1;
		})));

		commandDispatcher.register(builder);
	}
}

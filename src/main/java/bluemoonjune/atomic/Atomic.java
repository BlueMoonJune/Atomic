package bluemoonjune.atomic;

import bluemoonjune.atomic.command.CommandAtomic;
import bluemoonjune.atomic.network.PacketToggleFeature;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.event.defs.CommonEvents;
import turniplabs.halplibe.util.GameStartEntrypoint;
import turniplabs.halplibe.util.RecipeEntrypoint;
import turniplabs.halplibe.util.dependency.Key;

import java.util.HashMap;
import java.util.Map;


public class Atomic implements ModInitializer {
    public static final String MOD_ID = "atomic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Map<String, Boolean> FEATURES = new HashMap<>();
	public static final Map<String, Integer> FEATURE_IDS = new HashMap<>();
	public static final Map<Integer, String> ID_FEATURES = new HashMap<>();
	public static final Map<String, String> BEST_VERSION = new HashMap<>();
	public static final Map<String, String> NEEDED_VERSION = new HashMap<>();

	public static boolean crushing = false;
	public static final Map<ItemStack, ItemStack> PRESSING = new HashMap<>();
	public static final Map<Block<?>, ItemStack> CRUSHING = new HashMap<>();

	public static void registerFeature(String name) {
		FEATURE_IDS.put(name, FEATURES.size());
		ID_FEATURES.put(FEATURES.size(), name);
		FEATURES.put(name, false);
	}

	public static void registerCommands() {
		CommandManager.registerCommand(new CommandAtomic());
	}

	@Override
    public void onInitialize() {
		LOGGER.info("Atomic initialized.");

		Packet.addMapping(202, true, true, PacketToggleFeature.class);

		registerFeature("BasketTippingPlayers");
		registerFeature("BasketTippingActivators");
		registerFeature("Craftivators");
		registerFeature("ReinforcedPressing");
		registerFeature("ReinforcedCrushing");

		CommonEvents.RECIPES_READY.listen(Key.of(MOD_ID), this::onRecipesReady);
    }

	public void onRecipesReady() {
		PRESSING.put(new ItemStack(Items.SUGARCANE, 1), new ItemStack(Items.PAPER, 2));
		PRESSING.put(new ItemStack(Items.INGOT_STEEL_CRUDE, 3), new ItemStack(Items.INGOT_STEEL, 1));
		PRESSING.put(new ItemStack(Items.BONE, 1), new ItemStack(Items.DYE, 3, 15));

		CRUSHING.put(Blocks.SUGARCANE, new ItemStack(Items.DUST_SUGAR, 3));
		CRUSHING.put(Blocks.WOOL, new ItemStack(Items.STRING, 4));
	}
}

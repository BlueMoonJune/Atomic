package bluemoonjune.atomic.mixin.craftivators;

import bluemoonjune.atomic.Atomic;
import bluemoonjune.atomic.MenuNull;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicWorkbench;
import net.minecraft.core.block.entity.TileEntityActivator;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerCrafting;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = BlockLogicWorkbench.class)
public abstract class BlockLogicWorkbenchMixin extends BlockLogic {

	public BlockLogicWorkbenchMixin(Block<?> block, Material material) {
		super(block, material);
	}

	@Override
	public void onActivatorInteracted(@NotNull World world, @NotNull TilePosc pos, @NotNull TileEntityActivator activator, @NotNull Direction direction) {
		if (!Atomic.FEATURES.get("Craftivators")) return;
		ContainerCrafting crafting = new ContainerCrafting(new MenuNull(), 3, 3);
		for (int i = 0; i < 9; i++) {
			crafting.setItem(i, activator.getItem(i));
		}
		ItemStack result = Registries.RECIPES.findMatchingRecipe(crafting);
		Registries.RECIPES.onCraftResult(crafting);

		for (int i = 0; i < 9; i++) {
			activator.setItem(i, crafting.getItem(i));
		}
		world.dropItem(pos.x(), pos.y()+1, pos.z(), result);
		for (Player player : world.players) {
			world.playSoundEffect(player, SoundCategory.WORLD_SOUNDS, pos.x(), pos.y(), pos.z(), "step.wood", 1, 1);
		}
	}
}

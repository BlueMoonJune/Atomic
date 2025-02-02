package bluemoonjune.atomic.mixin.reinforced;

import bluemoonjune.atomic.Atomic;
import bluemoonjune.atomic.MenuNull;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.piston.BlockLogicPistonBase;
import net.minecraft.core.block.piston.BlockLogicPistonBaseSteel;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerCrafting;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.phys.AABB;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(value = BlockLogicPistonBaseSteel.class)
public abstract class BlockLogicPistonBaseSteelMixin extends BlockLogicPistonBase {

	@Shadow
	private Entity flungBlock;
	public final boolean[][] compactingShapes = {
		{true, true, true, true, true, true, true, true, true},
		{true, true, true, true, false, true, true, true, true},
		{true, true, false, true, true, false, false, false, false},
	};
	public final int[] compactingCounts = {
		9, 8, 4
	};

	public BlockLogicPistonBaseSteelMixin(Block<?> block, int maxPushedBlocks) {
		super(block, maxPushedBlocks);
	}

	@Inject(
		method = "tryExtend",
		at = @At("HEAD"),
		remap = false
	)
	public void setCrushFlag(World world, int x, int y, int z, Direction direction, int maxPushedBlocks, CallbackInfoReturnable<Boolean> cir) {
		Atomic.crushing = true;
	}

	@Inject(
		method = "tryExtend",
		at = @At("RETURN"),
		remap = false
	)
	public void unsetCrushFlag(World world, int x, int y, int z, Direction direction, int maxPushedBlocks, CallbackInfoReturnable<Boolean> cir) {
		Atomic.crushing = false;
	}

	@Inject(
		method = "extendEvent",
		at = @At("HEAD"),
		remap = false
	)
	public void pressItems(World world, int x, int y, int z, int data, Direction direction, CallbackInfo ci) {
		if (!Atomic.FEATURES.get("ReinforcedPressing")) {
			return;
		}
		Block<?> block = world.getBlock(x + direction.getOffsetX() * 2, y + direction.getOffsetY() * 2, z + direction.getOffsetZ() * 2);

		if (!BlockTags.PISTON_CRUSHING.appliesTo(block)) return;
		for (Entity entity : new ArrayList<Entity>(world.getEntitiesWithinAABBExcludingEntity((Entity)null, AABB.getTemporaryBB((double)x + (double)direction.getOffsetX(), (double)y + (double)direction.getOffsetY(), (double)z + (double)direction.getOffsetZ(), (double)x + (double)direction.getOffsetX() + (double)1.0F, (double)y + (double)direction.getOffsetY() + (double)1.0F, (double)z + (double)direction.getOffsetZ() + (double)1.0F)))) {
			if (entity instanceof EntityItem) {
				EntityItem itemEntity = (EntityItem)entity;
				if (itemEntity.age < 5) return;
				ItemStack item = itemEntity.item;

				for (ItemStack ingredient : Atomic.PRESSING.keySet()) {
					if (item.itemID == ingredient.itemID && item.stackSize >= ingredient.stackSize) {
						world.dropItem(x+direction.getOffsetX(), y+direction.getOffsetY(), z+direction.getOffsetZ(), Atomic.PRESSING.get(ingredient).copy());
						item.splitStack(ingredient.stackSize);
						for (Player player : world.players) {
							world.playSoundEffect(player, SoundCategory.WORLD_SOUNDS, x, y, z, "step.wood", 1, 1);
						}
						return;
					}
				}

				for (int i = 0; i < compactingShapes.length; i++) {
					if (item.stackSize < compactingCounts[i]) {
						continue;
					}
					ContainerCrafting crafting = new ContainerCrafting(new MenuNull(), 3, 3);
					for (int j = 0; j < 9; j++) {
						if (compactingShapes[i][j]) {
							ItemStack newItem = new ItemStack(item);
							newItem.stackSize = 1;
							crafting.setItem(j, newItem);
						}
					}
					ItemStack result = Registries.RECIPES.findMatchingRecipe(crafting);
					if (result != null) {
						Registries.RECIPES.onCraftResult(crafting);
						item.stackSize -= compactingCounts[i];
						world.dropItem(x+direction.getOffsetX(), y+direction.getOffsetY(), z+direction.getOffsetZ(), result);
						for (Player player : world.players) {
							world.playSoundEffect(player, SoundCategory.WORLD_SOUNDS, x, y, z, "step.wood", 1, 1);
						}
						return;
					}
				}
			}
		}
	}
}

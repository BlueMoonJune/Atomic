package bluemoonjune.atomic.mixin.reinforced;

import bluemoonjune.atomic.Atomic;
import bluemoonjune.atomic.MenuNull;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.piston.BlockLogicPistonBase;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerCrafting;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.phys.AABB;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(value = BlockLogicPistonBase.class)
public abstract class BlockLogicPistonBaseSteelMixin extends BlockLogic {

	public final boolean[][] compactingShapes = {
		{true, true, true, true, true, true, true, true, true},
		{true, true, true, true, false, true, true, true, true},
		{true, true, false, true, true, false, false, false, false},
	};
	public final int[] compactingCounts = {
		9, 8, 4
	};

	public BlockLogicPistonBaseSteelMixin(@NotNull Block<?> block, @NotNull Material material) {
		super(block, material);
	}

	@WrapMethod(
		method = "extend"
	)
	public boolean setCrushFlag(World world, TilePosc basePos, int data, int lineInfo, Operation<Boolean> original) {
		Atomic.crushing = true;
		var ret = original.call(world, basePos, data, lineInfo);
		Atomic.crushing = false;
		return ret;
	}

	@Inject(
		method = "breakTail",
		at = @At("HEAD"),
		remap = false
	)
	public void pressItems(EnumDropCause cause, World world, TilePosc headPos, Direction direction, int off, CallbackInfo ci) {
		if (!Atomic.FEATURES.get("ReinforcedPressing")) {
			return;
		}

		var x = headPos.x();
		var y = headPos.y();
		var z = headPos.z();

		Block<?> block = world.getBlockType(headPos.add(direction, new TilePos()));

		if (!block.isIn(BlockTags.PISTON_CRUSHING)) return;
		for (Entity entity : new ArrayList<>(world.getEntitiesWithinAABBExcludingEntity(null, new AABBd(x, y, z, x + 1.0, y + 1.0, z + 1.0)))) {
			if (entity instanceof EntityItem) {
				EntityItem itemEntity = (EntityItem)entity;
				if (itemEntity.age < 5) return;
				ItemStack item = itemEntity.item;

				for (ItemStack ingredient : Atomic.PRESSING.keySet()) {
					if (item.itemID == ingredient.itemID && item.stackSize >= ingredient.stackSize) {
						world.dropItem(x+direction.offsetX(), y+direction.offsetY(), z+direction.offsetZ(), Atomic.PRESSING.get(ingredient).copy());
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
						world.dropItem(x, y, z, result);
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

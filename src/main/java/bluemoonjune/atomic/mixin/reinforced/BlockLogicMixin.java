package bluemoonjune.atomic.mixin.reinforced;

import bluemoonjune.atomic.Atomic;
import bluemoonjune.atomic.MenuNull;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.IItemConvertible;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerCrafting;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BlockLogic.class)
public abstract class BlockLogicMixin implements IItemConvertible {
	@Shadow
	@Final
	@NotNull
	public Block<?> block;

	@WrapMethod(
		method = "getBreakResult(Lnet/minecraft/core/world/World;Lnet/minecraft/core/enums/EnumDropCause;ILnet/minecraft/core/block/entity/TileEntity;)[Lnet/minecraft/core/item/ItemStack;",
		remap = false
	)
	public ItemStack[] crushResourceBlock(World world, EnumDropCause dropCause, int data, TileEntity tileEntity, Operation<ItemStack[]> original) {
		if (Atomic.FEATURES.get("ReinforcedCrushing") && dropCause == EnumDropCause.PISTON_CRUSH) {
			ItemStack item = new ItemStack(this.block);
			if (Atomic.CRUSHING.containsKey(this.block)) {
				return (new ItemStack[]{Atomic.CRUSHING.get(this.block)});
			}
			if (!item.getItem().namespaceID.value().contains("block")) return null;
			ContainerCrafting crafting = new ContainerCrafting(new MenuNull(), 3, 3);
			crafting.setItem(0, item);
			ItemStack result = Registries.RECIPES.findMatchingRecipe(crafting);
			if (result != null) {
				return (new ItemStack[]{result});
			}
		}
		return original.call(world, dropCause, data, tileEntity);
	}
}

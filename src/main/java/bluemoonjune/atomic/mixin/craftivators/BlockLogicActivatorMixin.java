package bluemoonjune.atomic.mixin.craftivators;

import bluemoonjune.atomic.Atomic;
import net.minecraft.core.block.*;
import net.minecraft.core.block.entity.TileEntityActivator;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BlockLogicActivator.class)
public abstract class BlockLogicActivatorMixin {

	@Redirect(
		method = "useItem",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/block/entity/TileEntityActivator;getNextStack()Lnet/minecraft/core/item/ItemStack;"),
		remap = false
	)
	public ItemStack useWorkbenchRegardless(TileEntityActivator instance) {

		if (!Atomic.FEATURES.get("Craftivators")) return instance.getNextStack();
		int x = instance.tilePos.x;
		int y = instance.tilePos.y;
		int z = instance.tilePos.z;
		World world = instance.worldObj;

		assert world != null;
		Direction direction = BlockLogicRotatable.getDirectionFromMeta(world.getBlockMetadata(x, y, z));
		int xOffset = direction.offsetX();
		int yOffset = direction.offsetY();
		int zOffset = direction.offsetZ();
		Block<?> block = world.getBlock(x + xOffset, y + yOffset, z + zOffset);
		if (block.getLogic() instanceof BlockLogicWorkbench) {
			instance.shiftSelector();
			return null;
		}
		return instance.getNextStack();
	}
}

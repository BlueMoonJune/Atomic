package bluemoonjune.atomic.mixin.reinforced;

import bluemoonjune.atomic.Atomic;
import net.minecraft.core.block.BlockLogicSugarcane;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockLogicSugarcane.class)
public abstract class BlockLogicSugarcaneMixin {

	@Inject(
		method = "getBreakResult",
		at = @At("HEAD"),
		remap = false,
		cancellable = true
	)
	public void crushFromPiston(World world, EnumDropCause dropCause, int data, TileEntity tileEntity, CallbackInfoReturnable<ItemStack[]> cir) {
		if (Atomic.FEATURES.get("ReinforcedCrushing") && Atomic.crushing) {
			cir.setReturnValue(new ItemStack[]{new ItemStack(Items.DUST_SUGAR, 3)});
			cir.cancel();
		}
	}
}

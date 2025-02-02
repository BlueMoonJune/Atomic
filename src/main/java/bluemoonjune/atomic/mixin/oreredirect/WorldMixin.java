package bluemoonjune.atomic.mixin.oreredirect;

import bluemoonjune.atomic.Atomic;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicOreRedstone;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = World.class)
public abstract class WorldMixin implements WorldSource {
	@Shadow
	public abstract @Nullable Block<?> getBlock(int x, int y, int z);

	@Inject(
		method = "getSignal",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/World;hasDirectSignal(III)Z"),
		remap = false,
		cancellable = true
	)
	public void checkRedstoneOreSignal(int x, int y, int z, Side side, CallbackInfoReturnable<Boolean> cir) {
		if (!Atomic.FEATURES.get("OreRedirect")) return;
		Block<?> block = getBlock(x, y, z);
		if (block != null && block.getLogic() instanceof BlockLogicOreRedstone) {
			if (block.getSignal(this, x, y, z, side)) {
				cir.setReturnValue(true);
				cir.cancel();
			}
		}
	}
}

package bluemoonjune.atomic.mixin.oreredirect;

import bluemoonjune.atomic.Atomic;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicOreRedstone;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = World.class)
public abstract class WorldMixin implements WorldSource {

	@Shadow
	public abstract @NotNull Block<?> getBlockType(@NotNull TilePosc tilePos);

	@Inject(
		method = "hasSignal",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/World;hasDirectSignal(Lnet/minecraft/core/world/pos/TilePosc;)Z"),
		remap = false,
		cancellable = true
	)
	public void checkRedstoneOreSignal(TilePosc tilePos, Side side, CallbackInfoReturnable<Boolean> cir) {
		if (!Atomic.FEATURES.get("OreRedirect")) return;
		Block<?> block = getBlockType(tilePos);
		if (block.getLogic() instanceof BlockLogicOreRedstone) {
			if (block.isEmittingSignal(this, tilePos, side)) {
				cir.setReturnValue(true);
				cir.cancel();
			}
		}
	}
}

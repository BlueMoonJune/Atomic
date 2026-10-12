package bluemoonjune.atomic.mixin.fluids;

import bluemoonjune.atomic.Atomic;
import net.minecraft.core.block.BlockLogicFluid;
import net.minecraft.core.block.FluidLava;
import net.minecraft.core.world.Dimension;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(FluidLava.class)
public class FluidLavaMixin {

	@Inject(
		method = "canBecomeSource",
		at = @At("HEAD"),
		cancellable = true
	)
	public void canBecomeSource(BlockLogicFluid logicFluid, World world, TilePosc tilePos, Random rand, CallbackInfoReturnable<Boolean> cir) {
		if (Atomic.FEATURES.get("NetherLavaSources") && world.dimension == Dimension.NETHER) {
			cir.setReturnValue(true);
		}
	}
}

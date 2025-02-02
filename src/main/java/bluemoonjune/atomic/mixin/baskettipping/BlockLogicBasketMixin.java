package bluemoonjune.atomic.mixin.baskettipping;

import bluemoonjune.atomic.Atomic;
import bluemoonjune.atomic.baskettipping.IFlip;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicBasket;
import net.minecraft.core.block.entity.TileEntityActivator;
import net.minecraft.core.block.entity.TileEntityBasket;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockLogicBasket.class)
public abstract class BlockLogicBasketMixin extends BlockLogic {

//	public final int FLIP = 0b100000;

	public BlockLogicBasketMixin(Block<?> block, Material material) {
		super(block, material);
	}

	@Inject(
		method = "onBlockRightClicked",
		at = @At("HEAD"),
		cancellable = true,
		remap = false
	)
	public void onBlockRightClicked(World world, int x, int y, int z, Player entityplayer, Side side, double xPlaced, double yPlaced, CallbackInfoReturnable<Boolean> cir) {
		if (Atomic.FEATURES.get("BasketTipping")) {
			flip(world, x, y, z);
			cir.setReturnValue(true);
			cir.cancel();
		}

	}

	@Inject(
		method = "onActivatorInteract",
		at = @At("HEAD"),
		cancellable = true,
		remap = false
	)
	public void onActivatorInteract(World world, int x, int y, int z, TileEntityActivator activator, Direction direction, CallbackInfo ci) {
		if (Atomic.FEATURES.get("BasketTipping")) {
			flip(world, x, y, z);
			ci.cancel();
		}

	}

	@Unique
	public void flip(World world, int x, int y, int z) {
		for (Player player : world.players) {
			world.playSoundEffect(player, SoundCategory.WORLD_SOUNDS, x, y, z, "step.cloth", 1, 1);
		}
		world.setBlockMetadata(x, y, z, world.getBlockMetadata(x, y, z) | 1);
		if (world.isClientSide) return;
		TileEntityBasket te = (TileEntityBasket) world.getTileEntity(x, y, z);
		((IFlip)te).flip(20);
	}
}

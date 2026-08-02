package bluemoonjune.atomic.mixin.baskettipping;

import bluemoonjune.atomic.Atomic;
import bluemoonjune.atomic.baskettipping.IFlip;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicBasket;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityActivator;
import net.minecraft.core.block.entity.TileEntityBasket;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
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
		method = "onInteracted",
		at = @At("HEAD"),
		cancellable = true,
		remap = false
	)
	public void onBlockRightClicked(World world, TilePosc tilePos, Player player, Side side, double xHit, double yHit, CallbackInfoReturnable<Boolean> cir) {
		if (Atomic.FEATURES.get("BasketTippingPlayers")) {
			flip(world, tilePos);
			cir.setReturnValue(true);
			cir.cancel();
		}

	}

	@Inject(
		method = "onActivatorInteracted",
		at = @At("HEAD"),
		cancellable = true,
		remap = false
	)
	public void onActivatorInteract(World world, TilePosc tilePos, TileEntityActivator activator, Direction direction, CallbackInfo ci) {
		if (Atomic.FEATURES.get("BasketTippingActivators")) {
			flip(world, tilePos);
			ci.cancel();
		}

	}

	@Unique
	public void flip(World world, TilePosc pos) {
		for (Player player : world.players) {
			world.playSoundEffect(player, SoundCategory.WORLD_SOUNDS, pos.x(), pos.y(), pos.z(), "step.cloth", 1, 1);
		}
		world.setBlockData(pos, world.getBlockData(pos) | 1);
		if (world.isClientSide) return;
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof IFlip) {
			((IFlip)te).flip(20);
		}
	}
}

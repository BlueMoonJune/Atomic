package bluemoonjune.atomic.mixin.baskettipping;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.block.model.generic.BlockModelGenericBasket;
import net.minecraft.client.render.block.model.generic.BlockModelGenericSlab;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicBasket;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.util.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.useless.dragonfly.models.block.StaticBlockModel;

@Mixin(value = BlockModelGenericBasket.class)
public abstract class BlockModelBasketMixin<T extends BlockLogicBasket> extends BlockModelGeneric<T> {

	@Unique
	private StaticBlockModel tipped;

	public BlockModelBasketMixin(@NotNull Block<T> block, @NotNull StaticBlockModel staticModel) {
		super(block, staticModel);
	}

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	public void loadModel(Block<?> block, CallbackInfo ci) {
		tipped = BlockModelDispatcher.loadDataModel("atomic:block/basket_tipped").asModel();
	}

	@Override
	public @NotNull StaticBlockModel getModelFromData(int data) {
		if ((data & 1) == 1) {
			return tipped;
		}
		return super.getModelFromData(data);
	}
}


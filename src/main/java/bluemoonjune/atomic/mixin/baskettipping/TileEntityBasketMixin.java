package bluemoonjune.atomic.mixin.baskettipping;

import bluemoonjune.atomic.Atomic;
import bluemoonjune.atomic.baskettipping.IFlip;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.block.BlockLogicFurnace;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.*;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(value = TileEntityBasket.class)
public abstract class TileEntityBasketMixin extends TileEntity implements IFlip {
	public int flipTime = 0;

	@Shadow(remap = false)
	@Final
	private Object2IntMap<TileEntityBasket.BasketEntry> contents;


	@Inject(
		method = "tick",
		at = @At("TAIL"),
		remap = false
	)
	public void flipCheck(CallbackInfo ci) {
		if (!Atomic.FEATURES.get("BasketTippingPlayers") && !Atomic.FEATURES.get("BasketTippingActivators") || worldObj == null) return;
		if (flipTime > 0) {
			flipTime--;
			if (flipTime == 0) {

				worldObj.setBlockMetadata(tilePos.x, tilePos.y, tilePos.z, worldObj.getBlockMetadata(tilePos.x, tilePos.y, tilePos.z) & ~1);
				worldObj.notifyBlockChange(tilePos.x, tilePos.y, tilePos.z, Blocks.BASKET.id());
			}
		}
	}

	@Shadow
	public abstract void updateNumUnits();

	@Override
	public void flip(int flipTime) {
		this.flipTime = flipTime;
		if (worldObj == null) return;
		TileEntity below = worldObj.getTileEntity(tilePos.x, tilePos.y - 1, tilePos.z);

		int globalOffset = below instanceof TileEntityActivator ? ((TileEntityActivator) below).stackSelector : 0;

		if (below instanceof Container) {
			Container container = (Container) below;
			List<TileEntityBasket.BasketEntry> toRemove = new ArrayList<>();

			for (Map.Entry<TileEntityBasket.BasketEntry, Integer> entry : this.contents.object2IntEntrySet()) {
				TileEntityBasket.BasketEntry basketEntry = (TileEntityBasket.BasketEntry) entry.getKey();
				ItemStack basketEntryStack = new ItemStack(basketEntry.id(), (Integer) entry.getValue(), basketEntry.metadata(), basketEntry.tag());

				int size = container.getContainerSize();

				int offset = globalOffset;
				int skipslot = -1;
				if (below instanceof TileEntityFurnace furnace) {
					if (furnace.getBurnTimeFromItem(basketEntryStack) > 0) {
						offset = TileEntityFurnace.SLOT_FUEL;
					}
					skipslot = TileEntityFurnace.SLOT_RESULT;
				} else if (below instanceof TileEntityTrommel trommel) {
					if (trommel.getItemBurnTime(basketEntryStack) > 0) {
						offset = TileEntityTrommel.SLOT_FUEL;
					}
				} else if (below instanceof TileEntityFurnaceBlast blast) {
					if (blast.getBurnTimeFromItem(basketEntryStack) > 0) {
						offset = TileEntityFurnaceBlast.SLOT_FUEL;
					}
					skipslot = TileEntityFurnaceBlast.SLOT_RESULT;
				}

				for (int j = 0; j < size; j++) {
					int i = (j + offset) % size;
					if (i == skipslot) continue;
					ItemStack slot = container.getItem(i);
					if (below instanceof TileEntityActivator act && act.locked(i)) {
						continue;
					} else if (slot == null) {
						container.setItem(i, basketEntryStack.splitStack(Math.min(basketEntryStack.getMaxStackSize(), basketEntryStack.stackSize)));
					} else if (slot.canStackWith(basketEntryStack)) {
						int amt = Math.min(basketEntryStack.stackSize, slot.getMaxStackSize() - slot.stackSize);
						basketEntryStack.stackSize -= amt;
						slot.stackSize += amt;
					}
					if (basketEntryStack.stackSize <= 0) {
						toRemove.add(basketEntry);
						break;
					}
				}
				this.contents.put(basketEntry, basketEntryStack.stackSize);

			}

			for (TileEntityBasket.BasketEntry entry : toRemove) {
				this.contents.remove(entry);
			}

			updateNumUnits();
			worldObj.notifyBlockChange(this.tilePos.x, this.tilePos.y, this.tilePos.z, Blocks.BASKET.id());
			return;
		}
		dropContents(worldObj, tilePos.x, tilePos.y, tilePos.z);
	}

	@Inject(
		method = "dropItemStack",
		at = @At("HEAD"),
		cancellable = true,
		remap = false
	)
	private void dropItemStack(Random rand, ItemStack itemstack, CallbackInfo ci) {
		if (!Atomic.FEATURES.get("BasketTippingPlayers") && !Atomic.FEATURES.get("BasketTippingActivators")) return;
		ci.cancel();
		float f = 0.5f;
		float f1 = 0.5f;
		float f2 = 0.5f;
		World workingWorld;
		if (this.worldObj != null) {
			workingWorld = this.worldObj;
		} else {
			if (this.carriedBlock == null) {
				return;
			}

			workingWorld = this.carriedBlock.world;
		}

		EntityItem item = new EntityItem(workingWorld, (double)((float)this.tilePos.x + f), (double)((float)this.tilePos.y + f1), (double)((float)this.tilePos.z + f2), itemstack);
		item.xd = 0;
		item.yd = 0;
		item.zd = 0;
		workingWorld.entityJoinedWorld(item);
	}

	@Inject(
		method = "importItemStack",
		at = @At("HEAD"),
		cancellable = true,
		remap = false
	)
	public void dontPickupIfFlipped(CallbackInfoReturnable<Boolean> ci) {
		if (!Atomic.FEATURES.get("BasketTippingPlayers") && !Atomic.FEATURES.get("BasketTippingActivators")) return;
		if (flipTime > 0) {
			ci.setReturnValue(false);
			ci.cancel();
		}
	}

	@Shadow
	private int numUnitsInside;

	@Shadow
	protected abstract int getItemSizeUnits(Item item);
	@Shadow
	public abstract int getMaxUnits();

	@Inject(
		method = "importItemStack",
		at = @At(value = "RETURN", ordinal = 1),
		remap = false
	)
	public void overflowPatch(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (!Atomic.FEATURES.get("BasketTippingPlayers") && !Atomic.FEATURES.get("BasketTippingActivators")) return;
		updateNumUnits();
	}


}

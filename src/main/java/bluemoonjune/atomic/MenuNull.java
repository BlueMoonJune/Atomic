package bluemoonjune.atomic;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.InventoryAction;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.inventory.menu.MenuAbstract;
import net.minecraft.core.player.inventory.slot.Slot;

public class MenuNull extends MenuAbstract {
	@Override
	public IntList getMoveSlots(InventoryAction inventoryAction, Slot slot, int i, Player player) {
		return null;
	}

	@Override
	public IntList getTargetSlots(InventoryAction inventoryAction, Slot slot, int i, Player player) {
		return null;
	}

	@Override
	public boolean stillValid(Player player) {
		return false;
	}
}

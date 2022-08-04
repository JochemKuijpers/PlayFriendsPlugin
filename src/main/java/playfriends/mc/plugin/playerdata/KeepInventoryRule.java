package playfriends.mc.plugin.playerdata;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

/** The value for the keep inventory rule. */
public enum KeepInventoryRule {
	/** Keep nothing, vanilla behavior. */
	NONE(itemStack -> false),

	/** Only keep armor slots. */
	ARMOR(itemStack -> {
		if (itemStack == null) return false;
		final EquipmentSlot equipmentSlot = itemStack.getType().getEquipmentSlot();
		return equipmentSlot != EquipmentSlot.HAND && equipmentSlot != EquipmentSlot.OFF_HAND;
	}),

	/** Only keep enchanted items. */
	ENCHANTED(itemStack -> {
		if (itemStack == null) return false;
		if (!itemStack.getType().isItem()) return false;
		return itemStack.getItemMeta().hasEnchants();
	}),

	/** Keep everything. */
	ALL(item -> true);

	/** The keep predicate, for filtering which item stacks in the inventory to keep. */
	private final Predicate<ItemStack> keepPredicate;

	KeepInventoryRule(Predicate<ItemStack> keepPredicate) {
		this.keepPredicate = keepPredicate;
	}

	public Predicate<ItemStack> getPredicate() {
		return keepPredicate;
	}
}

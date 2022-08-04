package playfriends.mc.plugin.features.keepinventory;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import playfriends.mc.plugin.MessageUtils;
import playfriends.mc.plugin.api.ConfigAwareListener;
import playfriends.mc.plugin.playerdata.KeepInventoryRule;
import playfriends.mc.plugin.playerdata.PlayerData;
import playfriends.mc.plugin.playerdata.PlayerDataManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/** The keep inventory and keep XP event handler. */
public class KeepInventoryHandler implements ConfigAwareListener {
	/** The player data manager. */
	private final PlayerDataManager playerDataManager;

	/** The player's saved items for restoring on respawn. */
	private final Map<UUID, SavedItems> playerSavedItems = new HashMap<>();

	private String itemsAndXpRestoredMessage;
	private String itemsRestoredMessage;
	private String xpRestoredMessage;
	private String keepInventoryNoneMessage;
	private String keepInventoryArmorMessage;
	private String keepInventoryEnchantedMessage;
	private String keepInventoryAllMessage;
	private String keepXpDisabledMessage;
	private String keepXpEnabledMessage;

	public KeepInventoryHandler(PlayerDataManager playerDataManager) {
		this.playerDataManager = playerDataManager;
	}

	@Override
	public void updateConfig(FileConfiguration newConfig) {
		itemsAndXpRestoredMessage = newConfig.getString("keepinventory.messages.items-and-xp-restored");
		itemsRestoredMessage = newConfig.getString("keepinventory.messages.items-restored");
		xpRestoredMessage = newConfig.getString("keepinventory.messages.xp-restored");
		keepInventoryNoneMessage = newConfig.getString("keepinventory.messages.set-none");
		keepInventoryArmorMessage = newConfig.getString("keepinventory.messages.set-armor");
		keepInventoryEnchantedMessage = newConfig.getString("keepinventory.messages.set-enchanted");
		keepInventoryAllMessage = newConfig.getString("keepinventory.messages.set-all");
		keepXpDisabledMessage = newConfig.getString("keepinventory.messages.set-xp-disabled");
		keepXpEnabledMessage = newConfig.getString("keepinventory.messages.set-xp-enabled");
	}

	@EventHandler
	public void onPlayerKeepInventory(PlayerKeepInventoryEvent event) {
		final Player player = event.getPlayer();
		final PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
		final KeepInventoryRule newKeepInventoryRule = event.getKeepInventoryRule();
		playerData.setKeepInventory(newKeepInventoryRule);

		final String message = switch (newKeepInventoryRule) {
			case NONE -> keepInventoryNoneMessage;
			case ARMOR -> keepInventoryArmorMessage;
			case ENCHANTED -> keepInventoryEnchantedMessage;
			case ALL -> keepInventoryAllMessage;
		};
		player.sendMessage(MessageUtils.formatMessage(message));
	}

	@EventHandler void onPlayerKeepXp(PlayerKeepXpEvent event) {
		final Player player = event.getPlayer();
		final PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
		boolean keepXp = !playerData.isKeepXp();

		playerData.setKeepXp(keepXp);
		player.sendMessage(MessageUtils.formatMessage(keepXp ? keepXpEnabledMessage : keepXpDisabledMessage));
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final UUID playerId = player.getUniqueId();
		final PlayerData playerData = playerDataManager.getPlayerData(playerId);

		KeepInventoryRule keepInventory = playerData.getKeepInventory();
		if (keepInventory == null) {
			keepInventory = KeepInventoryRule.NONE;
		}

		// keepxp rule:
		if (playerData.isKeepXp()) {
			event.setKeepLevel(true);
			event.setDroppedExp(0);
		}

		// keepinventory is handled below
		event.setKeepInventory(false);

		// clear the predetermined drop list to avoid adding duplicates
		final List<ItemStack> drops = event.getDrops();
		drops.clear();

		// selectively keep inventory, filter for the type of items we want to keep and drop the rest
		final Predicate<ItemStack> keepPredicate = keepInventory.getPredicate();
		final ItemStack[] inventory = player.getInventory().getContents();
		final ItemStack[] armor = player.getInventory().getArmorContents();

		boolean retainedItems = filterInventoryAndDropItems(keepPredicate, inventory, drops);
		retainedItems = filterInventoryAndDropItems(keepPredicate, armor, drops) || retainedItems;

		// store the items, if we retained any in either inventory array
		if (retainedItems) {
			playerSavedItems.put(playerId, new SavedItems(inventory, armor));
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		final PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

		// Restore items if there are any to restore
		final SavedItems savedItems = playerSavedItems.remove(player.getUniqueId());
		if (savedItems != null) {
			if (savedItems.inventory != null) {
				player.getInventory().setContents(savedItems.inventory);
			}
			if (savedItems.armor != null) {
				player.getInventory().setArmorContents(savedItems.armor);
			}
		}

		// Let the player know what happened
		if (savedItems != null && playerData.isKeepXp()) {
			player.sendMessage(MessageUtils.formatMessage(itemsAndXpRestoredMessage));
		}
		if (savedItems != null && !playerData.isKeepXp()) {
			player.sendMessage(MessageUtils.formatMessage(itemsRestoredMessage));
		}
		if (savedItems == null && playerData.isKeepXp()) {
			player.sendMessage(MessageUtils.formatMessage(xpRestoredMessage));
		}
	}

	/**
	 * Filters the inventory with the given keepPredicate.
	 * If it returns false for an item stack, the item stack is added to the drop list and cleared from the inventory.
	 * Otherwise, the item stack is kept in the inventory and not added to the drop list
	 *
	 * @param keepPredicate the keep predicate
	 * @param inventory     the inventory to filter items from
	 * @param drops         the list of drops to add to
	 * @return true if the inventory retains at least one item stack, false otherwise
	 */
	private boolean filterInventoryAndDropItems(Predicate<ItemStack> keepPredicate, ItemStack[] inventory, List<ItemStack> drops) {
		boolean retainedItems = false;
		for (int i = 0; i < inventory.length; i++) {
			final ItemStack itemStack = inventory[i];
			if (keepPredicate.test(itemStack)) {
				retainedItems = true;
			} else {
				// move the item stack to the drop list
				drops.add(itemStack);
				inventory[i] = null;
			}
		}
		return retainedItems;
	}

	/** The pair of item stacks to keep on player death. */
	private record SavedItems(ItemStack[] inventory, ItemStack[] armor) {}
}

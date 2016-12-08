package net.cjservers.resourcegens.utilities;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.cjservers.resourcegens.Main;
import net.cjservers.resourcegens.objects.ResourceGen;

public class PlayerEvents implements Listener {

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		ItemStack item = e.getItemInHand();
		List<String> lore = item.getItemMeta().getLore();

		if (lore != null && lore.get(4).equals(ChatColor.GOLD + "Resource Gen")) {
			String name = e.getPlayer().getName() + "Gen" + (int)(Math.random() * 10000);
			while (Main.getInstance().genConfig.contains(name)) {
				name = e.getPlayer().getName() + "Gen" + (int)(Math.random() * 10000);
			}
			Utils.newGen(e.getPlayer().getName(), Integer.valueOf(lore.get(1).substring(11)), lore.get(2).substring(10),
					lore.get(3).substring(10), name, e.getBlock().getLocation());
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt) {
		UUID userId = evt.getPlayer().getUniqueId();
		FileConfiguration config = Main.getInstance().conf;
		FileConfiguration genConfig = Main.getInstance().genConfig;
		Map<String, ResourceGen> genList = Main.getInstance().getUtils().createGenList();
		if (!(genList == null)) {
			for (Entry<String, ResourceGen> entry : genList.entrySet()) {
				String name = entry.getKey();
				String owner = entry.getValue().getOwner();
				String type = entry.getValue().getType();
				String world = entry.getValue().getWorld();
				int x = entry.getValue().getX();
				int y = entry.getValue().getY();
				int z = entry.getValue().getZ();
				int level = entry.getValue().getLevel();
				Long time = config.getLong("levels." + level);
				Material resource = entry.getValue().getResource();
				if (userId.toString().equals(owner) && type.equalsIgnoreCase("online")) {
					if (!(world == null || !config.getConfigurationSection("levels").contains("" + level))) {
						Location loc = new Location(Bukkit.getWorld(world), x, y, z);
						Block generator = loc.getBlock();
						if (generator.getType() == Material.CHEST) {
							new BukkitRunnable() {
								public void run() {
									if (!genConfig.contains(entry.getKey())) {
										cancel();
									}
									if (!Bukkit.getOfflinePlayer(UUID.fromString(genConfig.getString(name + ".owner")))
											.isOnline()) {
										cancel();
									}
									Chest chest = (Chest) generator.getState();
									ItemStack item = new ItemStack(resource, 1);
									chest.getBlockInventory().addItem(item);
								}
							}.runTaskTimer(Main.getInstance(), 0L, time);
						} else {
							Main.getInstance().getLogger().severe(ChatColor.RED + "Generator: " + ChatColor.GOLD + name
									+ ChatColor.RED + " is not a chest!");
						}
					} else {
						Main.getInstance().getLogger().severe(
								ChatColor.RED + "Generator: " + ChatColor.GOLD + name + ChatColor.RED + " is invalid!");
						Main.getInstance().getLogger().severe(ChatColor.RED + "Check level and world!");
					}
				}
			}
		}
	}
}

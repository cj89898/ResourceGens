package net.cjservers.resourcegens.utilities;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.cjservers.resourcegens.Main;
import net.cjservers.resourcegens.objects.ResourceGen;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Generation {

	public static void Generator() {
		FileConfiguration config = Main.getInstance().conf;
		FileConfiguration genConfig = Main.getInstance().genConfig;
		Map<String, ResourceGen> genList = Main.getInstance().getUtils().createGenList();
		if (!(genList == null)) {
			for (Entry<String, ResourceGen> entry : genList.entrySet()) {
				String name = entry.getKey();
				String world = entry.getValue().getWorld();
				int x = entry.getValue().getX();
				int y = entry.getValue().getY();
				int z = entry.getValue().getZ();
				int level = entry.getValue().getLevel();
				Long time = config.getLong("levels." + level);
				Material resource = entry.getValue().getResource();
				if (!(world == null || !config.getConfigurationSection("levels").contains("" + level))) {
					Location loc = new Location(Bukkit.getWorld(world), x, y, z);
					Block generator = loc.getBlock();
					if (generator.getType() == Material.CHEST) {
						new BukkitRunnable() {
							public void run() {
								if (!genConfig.contains(entry.getKey())) {
									cancel();
								}
								if ((genConfig.getString(name + ".owner").equalsIgnoreCase("server")
										|| (genConfig.getString(name + ".type").equalsIgnoreCase("always")))) {
								} else {
									if (!Bukkit.getOfflinePlayer(UUID.fromString(genConfig.getString(name + ".owner")))
											.isOnline()) {
										cancel();
									}
								}
								Chest chest = (Chest) generator.getState();
								ItemStack item = new ItemStack(resource, 1);
								chest.getBlockInventory().addItem(item);
							}
						}.runTaskTimerAsynchronously(Main.getInstance(), 0L, time);
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

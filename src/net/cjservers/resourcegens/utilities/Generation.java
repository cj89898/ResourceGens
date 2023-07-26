package net.cjservers.resourcegens.utilities;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.cjservers.resourcegens.Main;
import net.cjservers.resourcegens.objects.ResourceGen;

public class Generation {
	
	public static void Generate() {
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
					if (!(loc.getBlock() == null || loc.getBlock().getType() == Material.AIR)) {
						Block generator = loc.getBlock();
						if (generator.getType() == Material.CHEST) {
							new InnerClass(name, generator, resource).runTaskTimerAsynchronously(Main.getInstance(), 0L,
									time);
						} else {
							Main.getInstance().getLogger().severe(ChatColor.RED + "Generator: " + ChatColor.GOLD + name
									+ ChatColor.RED + " is not a chest!");
						}
					} else {
						Main.getInstance().genConfig.set(entry.getKey(), null);
						Utils.save(Main.getInstance().genConfig, "generators.yml");
						Main.getInstance().getLogger().severe(ChatColor.RED + "Coordinates " + loc.getBlockX() + ", "
								+ loc.getBlockY() + ", " + loc.getBlockZ() + " is not a valid generator!");
						Main.getInstance().getLogger().severe(ChatColor.RED + name + " was automatically removed");
					}
					
				} else {
					Main.getInstance().getLogger().severe(
							ChatColor.RED + "Generator: " + ChatColor.GOLD + name + ChatColor.RED + " is invalid!");
					Main.getInstance().getLogger().severe(ChatColor.RED + "Check level and world!");
				}
			}
		}
	}
	
	static class InnerClass extends BukkitRunnable {
		
		private String name;
		private Block generator;
		private Material resource;
		private boolean firstTime;
		
		public InnerClass(String name, Block generator, Material resource) {
			this.name = name;
			this.generator = generator;
			this.resource = resource;
			firstTime = true;
		}
		
		@Override
		public void run() {
			FileConfiguration genConfig = Main.getInstance().genConfig;
			if (!genConfig.contains(name)) {
				cancel();
			}
			Main.getInstance().genIDs.put(name, this.getTaskId());
			if (!firstTime) {
				if (genConfig.getString(name + ".owner").equalsIgnoreCase("server")
						|| (genConfig.getString(name + ".type").equalsIgnoreCase("always"))
						|| Bukkit.getOfflinePlayer(UUID.fromString(genConfig.getString(name + ".owner"))).isOnline()) {
					Chest chest = (Chest) generator.getState();
					ItemStack item = new ItemStack(resource, 1);
					chest.getInventory().addItem(item);
				}
			} else {
				firstTime = false;
			}
		}
	}
}

package net.cjservers.resourcegens.utilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.cjservers.resourcegens.Main;
import net.cjservers.resourcegens.objects.ResourceGen;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Utils {

	Main plugin;

	public Utils(Main plugin) {
		this.plugin = plugin;
	}

	FileConfiguration genConfig;

	public Map<String, ResourceGen> createGenList() {
		genConfig = Main.getInstance().genConfig;
		Logger logger = plugin.getLogger();
		Map<String, ResourceGen> resourceGens = new HashMap<>();
		if (genConfig.getConfigurationSection("").getKeys(false).isEmpty()) {
			return null;
		}
		genConfig.getConfigurationSection("").getKeys(false).forEach(str -> {
			Material resource;
			try {
				resource = Material.valueOf(genConfig.getString(str + ".resource"));
			} catch (Exception e2) {
				logger.severe("you dun goofed @ " + str + ".resource");
				return;
			}
			String owner = genConfig.getString(str + ".owner");
			String type = genConfig.getString(str + ".type");
			String world = genConfig.getString(str + ".world");
			int x = genConfig.getInt(str + ".x");
			int y = genConfig.getInt(str + ".y");
			int z = genConfig.getInt(str + ".z");
			int level = genConfig.getInt(str + ".level");
			resourceGens.put(str, new ResourceGen(owner, type, world, x, y, z, level, resource));
		});
		return resourceGens;
	}

	private static final String DIRECTORY = "plugins/ResourceGens/";

	private static File getFile(String name) throws IOException {
		File file = new File(DIRECTORY, name);

		return file.createNewFile() ? file : file.exists() ? file : null;
	}

	public static FileConfiguration getConfiguration(String name) {
		try {
			File file = getFile(name);

			if (file != null) {
				return YamlConfiguration.loadConfiguration(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void save(FileConfiguration configuration, String name) {
		try {
			File file = getFile(name);

			if (file != null) {
				configuration.save(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void reload(FileConfiguration configuration, String name) {
		try {
			File file = getFile(name);

			if (file != null) {
				configuration = YamlConfiguration.loadConfiguration(file);
				configuration.save(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void reloadGens() {
		Bukkit.getServer().getScheduler().cancelTasks(Main.getInstance());
		Generation.Generator();
	}

	public static String newGen(String owner, int level, String resource, String type, String name, Location loc) {
		try {
			Material.valueOf(resource);
		} catch (IllegalArgumentException e) {
			return ChatColor.RED + "Invalid resource: " + ChatColor.GOLD + resource;
		}
		if (Main.getInstance().genConfig.contains(name)) {
			return ChatColor.RED + "Generator Already Exists!!";
		}
		String b4 = "" + Main.getInstance().conf.getConfigurationSection("levels").getKeys(false);
		String levels = b4.substring(1, b4.length() - 1);
		if (!(Main.getInstance().conf.getConfigurationSection("levels").contains("" + level))) {
			return ChatColor.RED + "Invalid Level!\n" + ChatColor.GREEN + "Valid Levels: " + levels;
		}
		String world = loc.getWorld().getName();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		Map<String, ResourceGen> genList = Main.getInstance().getUtils().createGenList();
		if (!(genList == null)) {
			for (Entry<String, ResourceGen> entry : genList.entrySet()) {
				String world1 = entry.getValue().getWorld();
				int x1 = entry.getValue().getX();
				int y1 = entry.getValue().getY();
				int z1 = entry.getValue().getZ();
				if (world.equalsIgnoreCase(world1) && x == x1 && y == y1 && z == z1) {
					return ChatColor.RED + "A generator already exists here!";
				}
			}
		}
		if (!owner.equalsIgnoreCase("server")) {
			if (Bukkit.getPlayer(owner) != null) {
				owner = Bukkit.getPlayer(owner).getUniqueId().toString();
			} else {
				return ChatColor.RED + "Invalid Player!";
			}
		}
		if (type.equalsIgnoreCase("always") || type.equalsIgnoreCase("online")) {
			type = type.toLowerCase();
		} else {
			return ChatColor.RED + "Type must be 'always' or 'online'";
		}

		Main.getInstance().genConfig.set(name + ".owner", owner);
		Main.getInstance().genConfig.set(name + ".type", type);
		Main.getInstance().genConfig.set(name + ".world", world);
		Main.getInstance().genConfig.set(name + ".x", x);
		Main.getInstance().genConfig.set(name + ".y", y);
		Main.getInstance().genConfig.set(name + ".z", z);
		Main.getInstance().genConfig.set(name + ".level", level);
		Main.getInstance().genConfig.set(name + ".resource", resource);
		Utils.save(Main.getInstance().genConfig, "generators.yml");
		Utils.reloadGens();

		return ChatColor.GREEN + "Resource Generator level " + ChatColor.GOLD + level + ChatColor.GREEN + " added at "
				+ ChatColor.GREEN + "X=" + ChatColor.GOLD + x + ChatColor.GREEN + " Y=" + ChatColor.GOLD + y
				+ ChatColor.GREEN + " Z=" + ChatColor.GOLD + z + ChatColor.GREEN + ", in world " + ChatColor.GOLD
				+ world + ChatColor.GREEN + ", generating " + ChatColor.GOLD + resource;
	}
}
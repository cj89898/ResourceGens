package net.cjservers.resourcegens;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.cjservers.resourcegens.hooks.Vault;
import net.cjservers.resourcegens.objects.ResourceGen;
import net.cjservers.resourcegens.utilities.Generation;
import net.cjservers.resourcegens.utilities.PlayerEvents;
import net.cjservers.resourcegens.utilities.Utils;

public class Main extends JavaPlugin implements Listener {

	private static Main instance;
	private Utils utils;
	public String version;

	private File confYml;
	private File genYml;
	public FileConfiguration conf;
	public FileConfiguration genConfig;

	public boolean vaultEnabled = false;;
	public Vault econ = new Vault(this);
	public boolean econEnabled = false;
	public Map<String, Integer> genIDs = new HashMap<>();

	@Override
	public void onEnable() {
		instance = this;
		utils = new Utils(this);

		confYml = new File(getDataFolder(), "config.yml");
		genYml = new File(getDataFolder(), "generators.yml");

		fixConf();
		conf = Utils.getConfiguration("config.yml");
		genConfig = Utils.getConfiguration("generators.yml");

		version = getDescription().getVersion();

		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new PlayerEvents(), this);

		this.getCommand("resourcegens").setExecutor(new Commands(this));

		if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			vaultEnabled = true;
			if (!econ.setupEconomy()) {
				getLogger().severe(String.format("[ResourceGens] Cannot hook into Vault Economy!"));
				getLogger().severe(String.format("[ResourceGens] Make sure to have a Vault compatible economy plugin installed!"));
			} else {
				econ.setupEconomy();
				econEnabled = true;
			}
		} else {
			getLogger().severe("[ResourceGens] Vault not found! Continuing on...");
		}

		Generation.Generate();
	}

	@Override
	public void onDisable() {

	}

	public static Main getInstance() {
		return instance;
	}

	public Utils getUtils() {
		return utils;
	}

	public void fixConf() {
		if (!(confYml.exists()) || Utils.getConfiguration("config.yml").get("Pricing") == null) {
			confYml.delete();
			saveDefaultConfig();
			System.out.println("[ResourceGens] - Created config.yml");
		}
		if (!(genYml.exists())) {
			try {
				genYml.createNewFile();
				System.out.println("[ResourceGens] - Created generators.yml");
			} catch (IOException e) {
				Bukkit.getServer().getLogger()
						.severe(ChatColor.RED + "[ResourceGens] - Could not create generators.yml");
			}
		}
	}

	public void reloadConfigs() {
		conf = Utils.getConfiguration("config.yml");
		genConfig = Utils.getConfiguration("generators.yml");
	}

	public void checkGens() {
		Map<String, ResourceGen> genList = Main.getInstance().getUtils().createGenList();
		if (!(genList == null)) {
			for (Entry<String, ResourceGen> entry : genList.entrySet()) {
				World world = Bukkit.getWorld(entry.getValue().getWorld());
				if (world == null) {
					getLogger().severe(ChatColor.RED + "[ResourceGens] Generator: " + entry.getValue() + " has an invalid world!");
				}
			}
		}
	}
}

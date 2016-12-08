package net.cjservers.resourcegens.hooks;

import net.cjservers.resourcegens.Main;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.permissions.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {
	
	private final Main plugin;
	
	public Vault(Main plugin) {
		this.plugin = plugin;
	}
	
	
	public static Permission permission = null;
	public static Economy economy = null;
	public static Chat chat = null;
	
	public boolean setupEconomy(){
		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if(economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		
		return (economy != null);
	}
}
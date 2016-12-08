package net.cjservers.resourcegens.utilities;

import java.util.Map;
import java.util.Map.Entry;

import net.cjservers.resourcegens.Main;
import net.cjservers.resourcegens.objects.ResourceGen;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class DestroyGen implements Listener{
	Main plugin;
	
	public DestroyGen(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents((Listener) this, plugin);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e){
		Player player = e.getPlayer();
		Block block = e.getBlock();
		Location loc = block.getLocation();
		String world = loc.getWorld().getName();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		Map<String, ResourceGen> genList = Main.getInstance().getUtils().createGenList();
		if(!(genList == null)){
			for (Entry<String, ResourceGen> entry : genList.entrySet()){
				String world1 = entry.getValue().getWorld();
				int x1 = entry.getValue().getX();
				int y1 = entry.getValue().getY();
				int z1 = entry.getValue().getZ();
				if(world.equals(world1) && x == x1 && y == y1 && z == z1){
					if(player.getGameMode() == GameMode.CREATIVE && player.isSneaking()){
						Main.getInstance().genConfig.set(entry.getKey(), null);
						Utils.save(Main.getInstance().genConfig, "generators.yml");
						player.sendMessage(ChatColor.GREEN+"Resource Generator "+ChatColor.GOLD+entry.getKey()+ChatColor.GREEN+" Removed!");
						return;
					}
					player.sendMessage(ChatColor.RED+"This is a resource generator!");
					player.sendMessage(ChatColor.RED+"You must be in creative mode AND sneaking to break!");
					e.setCancelled(true);
				}
			}
		}
	}
}
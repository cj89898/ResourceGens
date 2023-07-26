package net.cjservers.resourcegens.utilities;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import net.cjservers.resourcegens.Main;
import net.cjservers.resourcegens.objects.ResourceGen;

public class PlayerEvents implements Listener {

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		ItemStack item = e.getItemInHand();
		List<String> lore = item.getItemMeta().getLore();

		if (lore != null && lore.size() > 4 && lore.get(4).equals(ChatColor.GOLD + "Resource Gen")) {
			String name = e.getPlayer().getName() + "Gen" + (int) (Math.random() * 10000);
			while (Main.getInstance().genConfig.contains(name)) {
				name = e.getPlayer().getName() + "Gen" + (int) (Math.random() * 10000);
			}
			Utils.newGen(e.getPlayer().getName(), Integer.valueOf(lore.get(1).substring(11)), lore.get(2).substring(10),
					lore.get(3).substring(10), name, e.getBlock().getLocation());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		Player player = e.getPlayer();
		Block block = e.getBlock();
		Location loc = block.getLocation();
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
				if (world.equals(world1) && x == x1 && y == y1 && z == z1) {
					if (player.getGameMode() == GameMode.CREATIVE) {
						if (player.isSneaking()) {
							Main.getInstance().genConfig.set(entry.getKey(), null);
							Utils.save(Main.getInstance().genConfig, "generators.yml");
							player.sendMessage(ChatColor.GREEN + "Resource Generator " + ChatColor.GOLD + entry.getKey()
									+ ChatColor.GREEN + " Removed!");
							return;
						} else {
							player.sendMessage(ChatColor.RED + "This is a resource generator!");
							player.sendMessage(ChatColor.RED
									+ "Since you are in creative mode, you must be sneaking to break it!");
						}
					} else {
						if (player.isSneaking()) {
							Main.getInstance().genConfig.set(entry.getKey(), null);
							Utils.save(Main.getInstance().genConfig, "generators.yml");
							player.sendMessage(ChatColor.GREEN + "Resource Generator Removed!");
							e.getBlock().setType(Material.AIR);
							Utils.giveGen(player.getName(), entry.getValue().getLevel(),
									entry.getValue().getResource().toString(), entry.getValue().getType(),
									(CommandSender) player);
						} else {
							player.sendMessage(ChatColor.RED + "This is a resource generator!");
							player.sendMessage(ChatColor.RED + "You must be sneaking to break it!");
						}
					}
					e.setCancelled(true);
				}
			}
		}
	}
}

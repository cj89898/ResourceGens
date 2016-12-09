package net.cjservers.resourcegens;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockIterator;

import net.cjservers.resourcegens.hooks.Vault;
import net.cjservers.resourcegens.objects.ResourceGen;
import net.cjservers.resourcegens.utilities.Utils;
import net.milkbowl.vault.economy.EconomyResponse;

public class Commands implements CommandExecutor {
	private final Main plugin;

	public Commands(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (cmd.getName().equalsIgnoreCase("resourcegens")) {
				if ((args.length > 3) && (args[0].equalsIgnoreCase("add"))) {
					if (!(p.hasPermission("resourcegens.add"))) {
						sender.sendMessage(ChatColor.RED + "You do not have permission!");
						return true;
					}
					BlockIterator iter = new BlockIterator(p, 10);
					Block lastBlock = iter.next();
					while (iter.hasNext()) {
						lastBlock = iter.next();
						if (lastBlock.getType() == Material.AIR) {
							continue;
						}
						break;
					}
					if (!(lastBlock.getType() == Material.CHEST)) {
						sender.sendMessage(ChatColor.RED + "Block is not a chest!");
						return true;
					}
					String resource = args[2].toUpperCase();
					String name = args[3];
					int level = Integer.valueOf(args[1]);
					Location loc = lastBlock.getLocation();
					String owner;
					String type;

					if (args.length > 4) {
						owner = args[4];
					} else {
						owner = "server";
					}
					if (args.length > 5) {
						type = args[5].toLowerCase();
					} else {
						type = "always";
					}
					sender.sendMessage(Utils.newGen(owner, level, resource, type, name, loc));
					return true;
				} else if ((args.length > 1) && (args[0].equalsIgnoreCase("remove"))) {
					if (!(p.hasPermission("resourcegens.remove"))) {
						sender.sendMessage(ChatColor.RED + "You do not have permission!");
						return true;
					}
					String name = args[1];
					if (!(Main.getInstance().genConfig.contains(name))) {
						sender.sendMessage(ChatColor.RED + "Generator Doesn't Exist!!");
						return true;
					}
					Main.getInstance().genConfig.set(name, null);
					Utils.save(Main.getInstance().genConfig, "generators.yml");
					sender.sendMessage(ChatColor.GREEN + "Resource Generator " + ChatColor.GOLD + name + ChatColor.GREEN
							+ " Removed!");
					return true;
				} else if ((args.length > 1) && (args[0].equalsIgnoreCase("give"))) {
					if (!(p.hasPermission("resourcegens.add"))) {
						sender.sendMessage(ChatColor.RED + "You do not have permission!");
						return true;
					}
					String owner = args[1];
					int level = Integer.valueOf(args[2]);
					String resource = args[3].toUpperCase();
					String type = args[4];
					if (Bukkit.getPlayer(owner) == null) {
						sender.sendMessage(ChatColor.RED + "Invalid Player!");
						return true;
					}
					String b4 = "" + Main.getInstance().conf.getConfigurationSection("levels").getKeys(false);
					String levels = b4.substring(1, b4.length() - 1);
					if (!(Main.getInstance().conf.getConfigurationSection("levels").contains("" + level))) {
						sender.sendMessage(
								ChatColor.RED + "Invalid Level!\n" + ChatColor.GREEN + "Valid Levels: " + levels);
						return true;
					}
					try {
						Material.valueOf(resource);
					} catch (IllegalArgumentException e) {
						sender.sendMessage(ChatColor.RED + "Invalid resource: " + ChatColor.GOLD + resource);
						return true;
					}
					if (type.equalsIgnoreCase("always") || type.equalsIgnoreCase("online")) {
						type = type.toLowerCase();
					} else {
						sender.sendMessage(ChatColor.RED + "Type must be 'always' or 'online'");
						return true;
					}

					ItemStack item = new ItemStack(Material.CHEST, 1);
					ItemMeta itemMeta = item.getItemMeta();
					itemMeta.setDisplayName(ChatColor.GOLD + "Generator");
					itemMeta.setLore(Arrays.asList(ChatColor.GOLD + "Owner: " + ChatColor.GREEN + owner,
							ChatColor.GOLD + "Level: " + ChatColor.GREEN + level,
							ChatColor.GOLD + "Item: " + ChatColor.GREEN + resource,
							ChatColor.GOLD + "Type: " + ChatColor.GREEN + type, ChatColor.GOLD + "Resource Gen"));
					item.setItemMeta(itemMeta);
					Bukkit.getPlayer(owner).getInventory().addItem(item);

					return true;
				} else if ((args.length > 1) && (args[0].equalsIgnoreCase("makeowner"))) {
					if (!(p.hasPermission("resourcegens.add"))) {
						sender.sendMessage(ChatColor.RED + "You do not have permission!");
						return true;
					}
					UUID userId = Bukkit.getPlayer(args[1]).getUniqueId();
					if (args.length > 2) {
						if (!(Main.getInstance().genConfig.contains(args[2]))) {
							sender.sendMessage(ChatColor.RED + "Generator Doesn't Exist!!");
							return true;
						}
						Main.getInstance().genConfig.set(args[2] + ".owner", userId.toString());
						sender.sendMessage(
								ChatColor.GREEN + "User: " + ChatColor.GOLD + Bukkit.getPlayer(userId).getName()
										+ ChatColor.GREEN + " is now the owner of " + ChatColor.GOLD + args[2]);
						Utils.save(Main.getInstance().genConfig, "generators.yml");
					} else {
						BlockIterator iter = new BlockIterator(p, 10);
						Block lastBlock = iter.next();
						while (iter.hasNext()) {
							lastBlock = iter.next();
							if (lastBlock.getType() == Material.AIR) {
								continue;
							}
							break;
						}
						Location loc = lastBlock.getLocation();
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
									Main.getInstance().genConfig.set(entry.getKey() + ".owner", userId.toString());
									sender.sendMessage(ChatColor.GREEN + "User: " + ChatColor.GOLD
											+ Bukkit.getPlayer(userId).getName() + ChatColor.GREEN
											+ " is now the owner of " + ChatColor.GOLD + entry.getKey());
									Utils.save(Main.getInstance().genConfig, "generators.yml");
									return true;
								}
							}
						}
					}
					return true;
				} else if ((args.length > 1) && (args[0].equalsIgnoreCase("settype"))) {
					if (!(p.hasPermission("resourcegens.add"))) {
						sender.sendMessage(ChatColor.RED + "You do not have permission!");
						return true;
					}
					String type = args[1];
					if (!(type.equalsIgnoreCase("always"))) {
						if (!(type.equalsIgnoreCase("online"))) {
							sender.sendMessage(ChatColor.RED + "Invalid Type! Must be " + ChatColor.GOLD + "always"
									+ ChatColor.GREEN + " or " + ChatColor.GOLD + "online");
							return true;
						}
					}
					if (args.length > 2) {
						if (!(Main.getInstance().genConfig.contains(args[2]))) {
							sender.sendMessage(ChatColor.RED + "Generator Doesn't Exist!!");
							return true;
						}
						Main.getInstance().genConfig.set(args[2] + ".type", type);
						sender.sendMessage(ChatColor.GREEN + "Generator: " + ChatColor.GOLD + args[2] + ChatColor.GREEN
								+ " is now " + ChatColor.GOLD + type.toLowerCase());
						Utils.save(Main.getInstance().genConfig, "generators.yml");
					} else {
						BlockIterator iter = new BlockIterator(p, 10);
						Block lastBlock = iter.next();
						while (iter.hasNext()) {
							lastBlock = iter.next();
							if (lastBlock.getType() == Material.AIR) {
								continue;
							}
							break;
						}
						Location loc = lastBlock.getLocation();
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
									Main.getInstance().genConfig.set(entry.getKey() + ".type", type);
									sender.sendMessage(ChatColor.GREEN + "Generator: " + ChatColor.GOLD + entry.getKey()
											+ ChatColor.GREEN + " is now " + ChatColor.GOLD + type.toLowerCase());
									Utils.save(Main.getInstance().genConfig, "generators.yml");
									return true;
								}
							}
						}
					}
					return true;
				} else if ((args.length > 0) && (args[0].equalsIgnoreCase("list"))) {
					if (!(p.hasPermission("resourcegens.list"))) {
						sender.sendMessage(ChatColor.RED + "You do not have permission!");
						return true;
					}
					if (Main.getInstance().genConfig.getConfigurationSection("").getKeys(false).isEmpty()) {
						sender.sendMessage(ChatColor.GOLD + "No Resource Generators!");
						return true;
					}
					String b4 = "" + Main.getInstance().genConfig.getConfigurationSection("").getKeys(false);
					String list = b4.substring(1, b4.length() - 1);
					sender.sendMessage(list);
					return true;
				} else if ((args.length > 0) && (args[0].equalsIgnoreCase("levels"))) {
					if (!(p.hasPermission("resourcegens.levels"))) {
						sender.sendMessage(ChatColor.RED + "You do not have permission!");
						return true;
					}
					sender.sendMessage(ChatColor.GOLD + "Levels: "
							+ Main.getInstance().conf.getConfigurationSection("levels").getKeys(false));
					return true;
				} else if ((args.length > 2) && (args[0].equalsIgnoreCase("setprice"))) {
					if (!(p.hasPermission("resourcegens.edit"))) {
						sender.sendMessage(ChatColor.RED + "You do not have permission!");
						return true;
					}
					String resource = args[1].toUpperCase();
					try {
						Material.valueOf(resource);
					} catch (IllegalArgumentException e) {
						sender.sendMessage(ChatColor.RED + "Invalid resource: " + ChatColor.GOLD + resource);
						return true;
					}
					try {
						Double.parseDouble(args[2]);
					} catch (NumberFormatException e) {
						sender.sendMessage(ChatColor.RED + "Not a valid price!");
						return true;
					}
					Main.getInstance().conf.set("Pricing." + resource, Double.parseDouble(args[2]));
					Utils.save(Main.getInstance().conf, "config.yml");
					sender.sendMessage(ChatColor.GOLD + "Price for " + ChatColor.GREEN + resource + " set to "
							+ Double.parseDouble(args[2]));
					return true;
				} else if ((args.length > 0) && (args[0].equalsIgnoreCase("info"))) {
					if (!(p.hasPermission("resourcegens.info"))) {
						sender.sendMessage(ChatColor.RED + "You do not have permission!");
						return true;
					}
					if (args.length > 1) {
						if (!(p.hasPermission("resourcegens.info.others"))) {
							sender.sendMessage(ChatColor.RED + "You do not have permission!");
							return true;
						}
						String gen = args[1];
						if (!(Main.getInstance().genConfig.contains(gen))) {
							sender.sendMessage(
									ChatColor.GOLD + "No Resource Generator with name: " + ChatColor.AQUA + gen);
							sender.sendMessage(ChatColor.GOLD + "Type " + ChatColor.AQUA + "/rg list" + ChatColor.GOLD
									+ " to get all generators!");
							return true;
						}
						listInfo(gen, sender);
						return true;
					} else {
						BlockIterator iter = new BlockIterator(p, 10);
						Block lastBlock = iter.next();
						while (iter.hasNext()) {
							lastBlock = iter.next();
							if (lastBlock.getType() == Material.AIR) {
								continue;
							}
							break;
						}
						Location loc = lastBlock.getLocation();
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
									String gen = entry.getKey();
									listInfo(gen, sender);
									return true;
								}
							}
						}
					}
				} else if ((args.length > 0) && (args[0].equalsIgnoreCase("reload"))) {
					if (!(p.hasPermission("resourcegens.reload"))) {
						sender.sendMessage(ChatColor.RED + "You do not have permission!");
						return true;
					}
					reload(sender);
					return true;
				}
				helpMenu(sender);
			} else if (cmd.getName().equalsIgnoreCase("upgrade")) {
				if (Main.getInstance().econEnabled && Main.getInstance().vaultEnabled) {
					BlockIterator iter = new BlockIterator(p, 10);
					Block lastBlock = iter.next();
					while (iter.hasNext()) {
						lastBlock = iter.next();
						if (lastBlock.getType() == Material.AIR) {
							continue;
						}
						break;
					}
					Location loc = lastBlock.getLocation();
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
								String gen = entry.getKey();
								String owner = Main.getInstance().genConfig.getString(gen + ".owner");
								if (!owner.equalsIgnoreCase("server")) {
									owner = Bukkit.getPlayer(UUID.fromString(owner)).getName();
								}
								if (owner.equalsIgnoreCase(sender.getName())) {
									int level = entry.getValue().getLevel();
									int nextlvl = level + 1;
									double price = level * Main.getInstance().conf.getInt("Pricing.default");
									if (Main.getInstance().conf
											.get("Pricing." + entry.getValue().getResource().toString()) != null) {
										price = Main.getInstance().conf
												.getDouble("Pricing." + entry.getValue().getResource().toString());
									}
									if (!(Main.getInstance().conf.contains("levels." + (nextlvl)))) {
										sender.sendMessage(ChatColor.GREEN + "You are at the max level!");
										return true;
									}
									EconomyResponse r = Vault.economy.withdrawPlayer(p, price);
									if (r.transactionSuccess()) {
										Main.getInstance().genConfig.set(gen + ".level", nextlvl);
										sender.sendMessage(ChatColor.GREEN + "Generator upgraded to level "
												+ ChatColor.GOLD + nextlvl);
										Utils.save(Main.getInstance().genConfig, "generators.yml");
										Utils.reloadGens();
										return true;
									} else {
										sender.sendMessage(ChatColor.RED + "You need " + price
												+ " to purchase an upgrade to level " + ChatColor.GOLD + nextlvl);
										return true;
									}
								} else {
									sender.sendMessage(ChatColor.RED + "You do not own this generator!");
									return true;
								}
							}
						}
					}
				} else {
					sender.sendMessage("Vault Economy is not enabled on this server!");
				}
				return true;
			}
			return true;
		} else {
			if (cmd.getName().equalsIgnoreCase("resourcegens")) {
				if ((args.length > 0) && (args[0].equalsIgnoreCase("add"))) {
					sender.sendMessage("Must be a player!");
					return true;
				} else if ((args.length > 1) && (args[0].equalsIgnoreCase("remove"))) {
					String name = args[1];
					if (!(Main.getInstance().genConfig.contains(name))) {
						sender.sendMessage(ChatColor.RED + "Generator Doesn't Exist!!");
						return true;
					}
					Main.getInstance().genConfig.set(name, null);
					Utils.save(Main.getInstance().genConfig, "generators.yml");
					sender.sendMessage(ChatColor.GREEN + "Resource Generator " + ChatColor.GOLD + name + ChatColor.GREEN
							+ " Removed!");
					return true;
				} else if ((args.length > 0) && (args[0].equalsIgnoreCase("list"))) {
					if (Main.getInstance().genConfig.getConfigurationSection("").getKeys(false).isEmpty()) {
						sender.sendMessage(ChatColor.GOLD + "No Resource Generators!");
						return true;
					}
					String b4 = "" + Main.getInstance().genConfig.getConfigurationSection("").getKeys(false);
					String list = b4.substring(1, b4.length() - 1);
					sender.sendMessage(list);
					return true;
				} else if ((args.length > 0) && (args[0].equalsIgnoreCase("levels"))) {
					sender.sendMessage(ChatColor.GOLD + "Levels: "
							+ Main.getInstance().conf.getConfigurationSection("levels").getKeys(false));
					return true;
				} else if ((args.length > 1) && (args[0].equalsIgnoreCase("info"))) {
					String gen = args[1];
					if (!(Main.getInstance().genConfig.contains(gen))) {
						sender.sendMessage(ChatColor.GOLD + "No Resource Generator with name: " + ChatColor.AQUA + gen);
						sender.sendMessage(ChatColor.GOLD + "Type " + ChatColor.AQUA + "/rg list" + ChatColor.GOLD
								+ " to get all generators!");
						return true;
					}
					listInfo(gen, sender);
					return true;
				} else if ((args.length > 0) && (args[0].equalsIgnoreCase("reload"))) {
					reload(sender);
					return true;
				}
				helpMenu(sender);
				return true;
			}
		}
		return false;
	}

	private void helpMenu(CommandSender sender) {
		sender.sendMessage(
				ChatColor.GOLD + "-------------Resource Generators v" + Main.getInstance().version + "-------------");
		sender.sendMessage(
				"/rg add <level> <resource> <name> (owner) (type) -- Make the chest you're looking at into a generator named x with level x");
		sender.sendMessage("/rg remove <name> -- Removes a generator");
		sender.sendMessage("/rg give <user> <level> <resource> <type> -- Give a user a generator");
		sender.sendMessage("/rg makeowner <user> (name) -- Makes the user an owner of a generator");
		sender.sendMessage("/rg settype <type> (name) -- Changes the type of a generator");
		sender.sendMessage("/rg setprice <resource> <price> -- Changes the price for generator upgrade for a specific resource");
		sender.sendMessage("/rg list -- Lists generators");
		sender.sendMessage("/rg info (name) -- Lists information about a generator");
		sender.sendMessage("/rg levels -- Lists valid levels");
		sender.sendMessage("/rg reload -- Reloads Configs");
	}

	private void listInfo(String gen, CommandSender sender) {
		String world = Main.getInstance().genConfig.getString(gen + ".world");
		int x = Main.getInstance().genConfig.getInt(gen + ".x");
		int y = Main.getInstance().genConfig.getInt(gen + ".y");
		int z = Main.getInstance().genConfig.getInt(gen + ".z");
		int level = Main.getInstance().genConfig.getInt(gen + ".level");
		String resource = Main.getInstance().genConfig.getString(gen + ".resource");
		String owner = Main.getInstance().genConfig.getString(gen + ".owner");
		String type = Main.getInstance().genConfig.getString(gen + ".type");
		if (!owner.equalsIgnoreCase("server")) {
			owner = Bukkit.getPlayer(UUID.fromString(owner)).getName();
		}
		sender.sendMessage(ChatColor.GREEN + "Name: " + ChatColor.GOLD + gen);
		sender.sendMessage(ChatColor.GREEN + "  Owner: " + ChatColor.GOLD + owner);
		sender.sendMessage(ChatColor.GREEN + "  Type: " + ChatColor.GOLD + type);
		sender.sendMessage(ChatColor.GREEN + "  Level: " + ChatColor.GOLD + level);
		sender.sendMessage(ChatColor.GREEN + "  Resource: " + ChatColor.GOLD + resource);
		sender.sendMessage(ChatColor.GREEN + "  World: " + ChatColor.GOLD + world);
		sender.sendMessage(ChatColor.GREEN + "  Location (x, y, z): " + ChatColor.GOLD + x + ChatColor.GREEN + ", "
				+ ChatColor.GOLD + y + ChatColor.GREEN + ", " + ChatColor.GOLD + z);
	}

	private void reload(CommandSender sender) {
		Main.getInstance().fixConf();
		Utils.reload(Main.getInstance().conf, "config.yml");
		Utils.reload(Main.getInstance().genConfig, "generators.yml");
		Main.getInstance().reloadConfigs();
		Utils.reloadGens();
		sender.sendMessage(ChatColor.GREEN + "Configs Reloaded!");
	}
}
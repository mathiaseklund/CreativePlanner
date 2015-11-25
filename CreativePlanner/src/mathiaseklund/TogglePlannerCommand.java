package mathiaseklund;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_8_R3.TileEntityChest;

public class TogglePlannerCommand implements CommandExecutor {

	Main plugin = Main.getMain();
	Messages msg = Messages.getInstance();

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (player.hasPermission("cp.use") || player.isOp()) {
				if (args.length == 0) {
					Methods.togglePlanning(player);
				} else if (args.length == 1) {
					String name = player.getName();
					if (args[0].equalsIgnoreCase("finish")) {
						msg.msg(player, plugin.config.getString("message.usage.finish"));
					} else if (args[0].equalsIgnoreCase("scrap")) {
						List<String> placedBlocks = Lists.placedBlocks.get(name);
						for (String s : placedBlocks) {
							Location loc = Utils.StringToLoc(s.split(" ")[0]);
							loc.getBlock().setType(Material.AIR);
							s = s + " " + name;
							Lists.protectBlocks.remove(s);
						}
						Lists.placedBlocks.remove(name);
						if (plugin.config.getString("message.scrapped") != null) {
							msg.msg(player, plugin.config.getString("message.scrapped"));
						}
					}
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("finish")) {
						final String playerName = player.getName();
						final String buildName = args[1];
						final List<String> placedBlocks = Lists.placedBlocks.get(playerName);
						final Location loc = player.getLocation();
						String location = Utils.LocToString(loc);
						boolean partofbuild = false;
						for (String s : placedBlocks) {
							s = s.split(" ")[0];
							if (s.equalsIgnoreCase(location)) {
								partofbuild = true;

							}
						}
						if (partofbuild) {
							if (plugin.config.getString("message.finished.fail") != null) {
								msg.msg(player, plugin.config.getString("message.finished.fail"));
							}
						} else {
							String uuid = player.getUniqueId().toString();
							loc.getBlock().setType(Material.CHEST);
							location = Utils.LocToString(loc);
							plugin.pdata.set(uuid + ".chest." + buildName, location);
							plugin.pdata.set(uuid + ".blocks." + buildName, placedBlocks);
							plugin.savepd();
							Lists.protectChests.add((location + " " + playerName));

							Chest chest = (Chest) loc.getBlock().getState();
							CraftChest cchest = (CraftChest) loc.getBlock().getState();

							try {
								Field inventoryField = cchest.getClass().getDeclaredField("chest");
								inventoryField.setAccessible(true);
								TileEntityChest teChest = ((TileEntityChest) inventoryField.get(cchest));
								teChest.a(buildName + " " + playerName);
							} catch (Exception e) {
								e.printStackTrace();
							}
							Inventory inv = chest.getInventory();
							for (String s : placedBlocks) {
								int id = Integer.parseInt(s.split(" ")[1]);
								Location location1 = Utils.StringToLoc(s.split(" ")[0]);
								location1.getBlock().setType(Material.AIR);
								ItemStack is = new ItemStack(Material.getMaterial(id));
								inv.addItem(is);
								Lists.protectBlocks.remove((s + " " + playerName));
							}
							Lists.placedBlocks.remove(playerName);
							if (plugin.config.getString("message.finished.success") != null) {
								msg.msg(player, plugin.config.getString("message.finished.success"));
							}

						}
						Methods.togglePlanning(player);
					}
				}
			} else {
				msg.noperm(player);
			}
		} else {
			msg.onlyplayer(sender);
		}
		return false;
	}

}

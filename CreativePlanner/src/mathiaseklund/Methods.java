package mathiaseklund;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.Stairs;

public class Methods {

	public static Main plugin = Main.getMain();
	public static Messages msg = Messages.getInstance();

	public static boolean isPlanning(Player player) {
		boolean isPlanning = false;
		String name = player.getName();
		if (Lists.planning.contains(name)) {
			isPlanning = true;
		}
		return isPlanning;
	}

	public static void togglePlanning(Player player) {
		String name = player.getName();
		String uuid = player.getUniqueId().toString();
		if (!isPlanning(player)) {
			Lists.planning.add(name);
			player.setGameMode(GameMode.CREATIVE);
			String inventory = InventorySerializer.InventoryToString(player.getInventory());
			String location = Utils.LocToString(player.getLocation());
			plugin.pdata.set(uuid + ".location", location);
			plugin.pdata.set(uuid + ".inventory", inventory);
			plugin.savepd();
			player.getInventory().clear();
			msg.msg(player, plugin.config.getString("message.cp.toggled").replace("%state%", "on"));
		} else {
			Lists.planning.remove(name);
			String inventory = plugin.pdata.getString(uuid + ".inventory");
			Inventory inv = InventorySerializer.StringToInventory(inventory);
			player.getInventory().setContents(inv.getContents());
			player.setGameMode(GameMode.SURVIVAL);
			Location loc = Utils.StringToLoc(plugin.pdata.getString(uuid + ".location"));
			player.teleport(loc);
			// TODO UPDATE WITH NEW LISTS AND SHIT TO REVOKE TELEPORT BACK TO
			// OLD LOCATION
			msg.msg(player, plugin.config.getString("message.cp.toggled").replace("%state%", "off"));
		}
	}

	public static boolean newPlayer(Player player) {
		String uuid = player.getUniqueId().toString();
		if (plugin.pdata.getString(uuid + ".name") != null) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean addBlock(Player player, Block placedBlock) {
		boolean placed = false;
		String name = player.getName();
		List<String> blacklistedblocks = plugin.config.getStringList("blacklisted-blocks");
		String type = placedBlock.getType().toString();
		int typeid = placedBlock.getTypeId();
		boolean allow = true;
		for (String s : blacklistedblocks) {
			if (Utils.isInteger(s)) {
				int id = Integer.parseInt(s);
				if (typeid == id) {
					allow = false;
				}
			} else {
				if (type.equalsIgnoreCase(s)) {
					allow = false;
				}
			}
		}
		if (allow) {
			List<String> placedBlocks = Lists.placedBlocks.get(name);
			if (placedBlocks == null) {
				placedBlocks = new ArrayList<String>();
			}
			String placedString = Utils.LocToString(placedBlock.getLocation()) + " " + typeid;
			if (placedBlock.getType().toString().contains("STAIRS")) {
				Stairs stair = (Stairs) placedBlock.getState().getData();
				placedString = placedString + " " + stair.getFacing().toString();
			}
			placedBlocks.add(placedString);
			Lists.protectBlocks.add(placedString + " " + name);
			Lists.placedBlocks.put(name, placedBlocks);
			placed = true;
		}
		return placed;
	}

	public static boolean isYourBlock(Player player, Block block) {
		boolean isYours = false;
		String name = player.getName();
		int typeid = block.getTypeId();
		List<String> placedBlocks = Lists.placedBlocks.get(name);
		if (placedBlocks != null) {
			String blockString = Utils.LocToString(block.getLocation()) + " " + typeid;
			if (block.getType().toString().contains("STAIRS")) {
				Stairs stair = (Stairs) block.getState().getData();
				blockString = blockString + " " + stair.getFacing().toString();
			}
			for (String s : placedBlocks) {
				if (s.equalsIgnoreCase(blockString)) {
					isYours = true;
				}
			}
		}
		return isYours;
	}

	public static boolean removeBlock(Player player, Block block) {
		boolean removed = true;
		String name = player.getName();
		int typeid = block.getTypeId();
		List<String> placedBlocks = Lists.placedBlocks.get(name);
		String blockString = Utils.LocToString(block.getLocation()) + " " + typeid;
		if (block.getType().toString().contains("STAIRS")) {
			Stairs stair = (Stairs) block.getState().getData();
			blockString = blockString + " " + stair.getFacing().toString();
		}
		msg.msg(player, blockString);
		placedBlocks.remove(blockString);
		Lists.placedBlocks.put(name, placedBlocks);
		return removed;
	}
}

package mathiaseklund;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Stairs;

public class PlayerListener implements Listener {

	private static PlayerListener instance = new PlayerListener();

	Main plugin = Main.getMain();
	Messages msg = Messages.getInstance();
	Utils util = Utils.getInstance();

	public static PlayerListener getInstance() {
		return instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();
		String name = player.getName();
		if (Methods.newPlayer(player)) {
			// IS NEW PLAYER
			plugin.pdata.set(uuid + ".name", name);
			plugin.savepd();
		} else {
			// PLAYED BEFORE
			boolean isPlanning = plugin.pdata.getBoolean(uuid + ".isPlanning");
			if (isPlanning) {
				Lists.planning.add(name);
				List<String> placedBlocks = plugin.pdata.getStringList(uuid + ".placedBlocks");
				Lists.placedBlocks.put(name, placedBlocks);
				msg.msg(player, plugin.config.getString("message.cp.toggled").replace("%state%", "on"));
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();
		String name = player.getName();
		if (Methods.isPlanning(player)) {
			plugin.pdata.set(uuid + ".isPlanning", true);
			plugin.pdata.set(uuid + ".placedBlocks", Lists.placedBlocks.get(name));
			plugin.savepd();
			Lists.placedBlocks.remove(name);
			Lists.planning.remove(name);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!event.isCancelled()) {
			Player player = event.getPlayer();
			if (Methods.isPlanning(player)) {
				Block block = event.getBlock();
				if (Methods.addBlock(player, block)) {
					if (plugin.config.getString("message.blockplace.success") != null) {
						msg.msg(player, plugin.config.getString("message.blockplace.success").replace("%location%", Utils.LocToString(block.getLocation())));
					}
				} else {
					if (plugin.config.getString("message.blockplace.fail") != null) {
						msg.msg(player, plugin.config.getString("message.blockplace.fail"));
					}
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.isCancelled()) {
			Player player = event.getPlayer();
			if (Methods.isPlanning(player)) {
				Block block = event.getBlock();
				if (Methods.isYourBlock(player, block)) {
					if (Methods.removeBlock(player, block)) {
						if (plugin.config.getString("message.break.success") != null) {
							String blockString = Utils.LocToString(block.getLocation()) + " " + block.getTypeId();
							msg.msg(player, plugin.config.getString("message.break.success").replace("%block%", blockString));
						}
					} else {
						if (plugin.config.getString("message.break.fail") != null) {
							String blockString = Utils.LocToString(block.getLocation()) + " " + block.getTypeId();
							msg.msg(player, plugin.config.getString("message.break.fail").replace("%block%", blockString));
						}
					}
				} else {
					// NOT YOUR BLOCK, CANCEL BREAK
					if (plugin.config.getString("message.nobreaking") != null) {
						msg.msg(player, plugin.config.getString("message.nobreaking"));
					}
					event.setCancelled(true);
				}
			} else {
				Block block = event.getBlock();
				String blockString = Utils.LocToString(block.getLocation()) + " " + block.getTypeId();
				boolean remove = true;
				for (String s : Lists.protectBlocks) {
					String location = s.split(" ")[0];
					String blockid = s.split(" ")[1];
					String bString = location + " " + blockid;
					if (bString.equalsIgnoreCase(blockString)) {
						remove = false;
					}
				}
				String name = player.getName();
				for (String s : Lists.protectChests) {
					if (s.equalsIgnoreCase((Utils.LocToString(block.getLocation()) + " " + name))) {
						remove = false;
					}
				}
				if (!remove) {
					if (plugin.config.getString("message.protectedblock") != null) {
						msg.msg(player, plugin.config.getString("message.protectedblock"));
					}
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelled()) {
			Player player = event.getPlayer();
			if (Methods.isPlanning(player)) {
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {

				} else {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (!event.isCancelled()) {
			Player player = event.getPlayer();
			if (Methods.isPlanning(player)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!event.isCancelled()) {
			if (event.getDamager() instanceof Player) {
				Player player = (Player) event.getDamager();
				if (Methods.isPlanning(player)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		if (!event.isCancelled()) {
			Player player = event.getPlayer();
			if (Methods.isPlanning(player)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		if (!event.isCancelled()) {
			Player player = event.getPlayer();
			if (Methods.isPlanning(player)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!event.isCancelled()) {
			if (event.getWhoClicked() instanceof Player) {
				Player player = (Player) event.getWhoClicked();
				String playerName = player.getName();
				Inventory inv = event.getInventory();
				String title = inv.getTitle();
				if (title.contains(" ")) {
					String chestowner = title.split(" ")[1];
					if (chestowner.equalsIgnoreCase(playerName)) {
						event.setCancelled(true);
						String buildName = title.split(" ")[0];
						int slot = event.getRawSlot();
						if (slot >= 27) {
							// PLAYER INVENTORY
							int isid = event.getCurrentItem().getTypeId();
							String uuid = player.getUniqueId().toString();
							List<String> blocks = plugin.pdata.getStringList(uuid + ".blocks." + buildName);
							boolean removeone = false;
							String remove = "";
							Location placelocation = null;
							String facing = null;
							for (String s : blocks) {
								int id = Integer.parseInt(s.split(" ")[1]);
								if (id == isid) {
									if (s.split(" ")[2] != null) {
										facing = s.split(" ")[2];
									}
									removeone = true;
									remove = s;
									placelocation = Utils.StringToLoc(s.split(" ")[0]);
									break;
								}
							}
							if (removeone) {
								blocks.remove(remove);
								plugin.pdata.set(uuid + ".blocks." + buildName, blocks);
								plugin.savepd();
								placelocation.getBlock().setType(Material.getMaterial(isid));
								if (facing != null) {
									final Stairs block = (Stairs) placelocation.getBlock().getState().getData();
									if (facing.equalsIgnoreCase("WEST")) {
										final Location ploc = placelocation;
										// Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
										// new Runnable() {
										// @Override
										// public void run() {
										// Get the block state
										BlockState state = ploc.getBlock().getState();
										// Cast metadata to stairs, make it face
										// north
										Stairs stairs = (Stairs) state.getData();
										stairs.setFacingDirection(BlockFace.EAST);
										// Apply new metadata.
										state.setData(stairs);
										// Update the block - do not force & do
										// not cause a block update
										state.update(false, false);
										// }
										// }, 10);
									} else if (facing.equalsIgnoreCase("NORTH")) {
										final Location ploc = placelocation;
										// Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
										// new Runnable() {
										// @Override
										// public void run() {
										// Get the block state
										BlockState state = ploc.getBlock().getState();
										// Cast metadata to stairs, make it face
										// north
										Stairs stairs = (Stairs) state.getData();
										stairs.setFacingDirection(BlockFace.SOUTH);
										// Apply new metadata.
										state.setData(stairs);
										// Update the block - do not force & do
										// not cause a block update
										state.update(false, false);
										// }
										// }, 10);
									} else if (facing.equalsIgnoreCase("SOUTH")) {
										final Location ploc = placelocation;
										// Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
										// new Runnable() {
										// @Override
										// public void run() {
										// Get the block state
										BlockState state = ploc.getBlock().getState();
										// Cast metadata to stairs, make it face
										// north
										Stairs stairs = (Stairs) state.getData();
										stairs.setFacingDirection(BlockFace.NORTH);
										// Apply new metadata.
										state.setData(stairs);
										// Update the block - do not force & do
										// not cause a block update
										state.update(false, false);
										// }
										// }, 10);
									} else if (facing.equalsIgnoreCase("EAST")) {
										final Location ploc = placelocation;
										// Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
										// new Runnable() {
										// @Override
										// public void run() {
										// Get the block state
										BlockState state = ploc.getBlock().getState();
										// Cast metadata to stairs, make it face
										// north
										Stairs stairs = (Stairs) state.getData();
										stairs.setFacingDirection(BlockFace.WEST);
										// Apply new metadata.
										state.setData(stairs);
										// Update the block - do not force & do
										// not cause a block update
										state.update(false, false);
										// }
										// }, 10);
									}

								}
								inv.clear();
								for (String s : blocks) {
									int id = Integer.parseInt(s.split(" ")[1]);
									ItemStack is = new ItemStack(Material.getMaterial(id));
									inv.addItem(is);
								}
								int isamount = event.getCurrentItem().getAmount();
								if (isamount > 1) {
									isamount--;
									event.getCurrentItem().setAmount(isamount);
									ItemStack is = event.getCurrentItem();
									player.getInventory().remove(event.getCurrentItem());
									player.getInventory().addItem(is);
								} else {
									player.getInventory().remove(event.getCurrentItem());
								}
								if (blocks.isEmpty()) {
									Location loc = Utils.StringToLoc(plugin.pdata.getString(uuid + ".chest." + buildName));
									loc.getBlock().setType(Material.AIR);

									plugin.pdata.set(uuid + ".chest." + buildName, null);
									plugin.pdata.set(uuid + ".blocks." + buildName, null);
									plugin.savepd();
									msg.msg(player, plugin.config.getString("message.finishedBuild"));
								}
							}
							// int itemSlot = 0;
							// for (int i = 0; i <= inv.getSize(); i++) {
							// if (inv.getItem(i) != null) {
							// ItemStack item = inv.getItem(i);
							// if (item != null) {
							// if (item.getTypeId() == is.getTypeId()) {
							// itemSlot = i;
							// break;
							// }
							// }
							// }
							// }
							// ItemStack item = inv.getItem(itemSlot);
							// int itemamount = item.getAmount();
							// int isamount = is.getAmount();
							// if (itemamount > 1) {
							// itemamount--;
							// item.setAmount(itemamount);
							// }else {
							// }
						} else {
							event.setCancelled(true);
						}
					} else {
						event.setCancelled(true);
					}
				}
			}
		}
	}
}
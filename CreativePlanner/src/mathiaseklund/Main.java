package mathiaseklund;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	private static Main main;

	File configurationConfig;
	public FileConfiguration config;
	File playerData;
	public FileConfiguration pdata;

	String prefix = "";

	public static Main getMain() {
		return main;
	}

	public void onEnable() {
		main = this;
		configurationConfig = new File(getDataFolder(), "config.yml");
		config = YamlConfiguration.loadConfiguration(configurationConfig);
		playerData = new File(getDataFolder(), "playerData.yml");
		pdata = YamlConfiguration.loadConfiguration(playerData);
		if (!configurationConfig.exists()) {
			loadConfig();
		}
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		getCommand("cpreload").setExecutor(new ReloadCommand());
		getCommand("cp").setExecutor(new TogglePlannerCommand());
	}

	public void onDisable() {
		pdata.set("protectedBlocks", Lists.protectBlocks);
		savepd();
	}

	public void savec() {
		try {
			config.save(configurationConfig);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void savepd() {
		try {
			pdata.save(playerData);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadConfig() {
		config.addDefault("message.finishedBuild", "Congratulations! You've finished your build!");
		config.addDefault("message.finished.success",
				"You've finished planning your build, chest has spawned at your location, place the needed blocks into the chest and the build will build itself.");
		config.addDefault("message.finished.fail", "You can't finish your build at this location, move outside the build area.");
		config.addDefault("message.scrapped", "You've scrapped your current build.");
		config.addDefault("message.usage.scrap", "To scrap a build type: /cp scrap");
		config.addDefault("message.usage.finish", "To finish a build type: /cp finish <name>.");
		config.addDefault("message.protectedblock", "The block you are trying to break is a part of someones build plan.");
		config.addDefault("message.break.success", "Block removal completed. BLOCK STRING: %block%");
		config.addDefault("message.break.fail", "Block removal failed, please contact an admin. BLOCK STRING: %block%");
		config.addDefault("message.nobreaking", "You are not allowed to break blocks while in planning mode.");
		config.addDefault("message.blockplace.success", "You've successfully added a block at %location%.");
		config.addDefault("message.blockplace.fail", "There was an error while placing your block, try again or move somewhere else.");
		ArrayList<String> blacklistedblocks = new ArrayList<String>();
		blacklistedblocks.add("46");
		blacklistedblocks.add("TNT_MINECART");
		config.addDefault("blacklisted-blocks", blacklistedblocks);
		config.addDefault("message.cp.toggled", "You've toggled planning %state%.");
		config.addDefault("message.noperm", "&4Error: You don't have permission to use this function.");
		config.addDefault("message.onlyplayer", "&4Error: Only players may use this function.");
		config.addDefault("prefix", "");
		config.options().copyDefaults(true);
		pdata.options().copyDefaults(true);
		savepd();
		savec();
		prefix = config.getString("prefix");

	}

}

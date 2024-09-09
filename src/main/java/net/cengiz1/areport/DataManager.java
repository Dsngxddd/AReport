package net.cengiz1.areport;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DataManager {
    private net.cengiz1.areport.AReport plugin;
    private File dataFolder;
    private File configFile;
    private FileConfiguration config;

    public DataManager(AReport plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.configFile = new File(dataFolder, "reports.yml");
        setupConfig();
    }

    private void setupConfig() {
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                config = YamlConfiguration.loadConfiguration(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addReport(String reporter, String reportedPlayer, String reason) {
        List<String> reports = config.getStringList("reports");
        reports.add(reporter + ":" + reportedPlayer + ":" + reason);
        config.set("reports", reports);
        saveConfig();
    }

    public List<String> getReports() {
        return config.getStringList("reports");
    }
}

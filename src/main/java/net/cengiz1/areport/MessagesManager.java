package net.cengiz1.areport;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class MessagesManager {

    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final Map<String, String> messages;

    public MessagesManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.messages = new HashMap<>();
        loadMessages();
    }

    private void loadMessages() {
        for (String key : config.getConfigurationSection("messages").getKeys(false)) {
            messages.put(key, config.getString("messages." + key));
        }
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "Message not found: " + key);
    }
}

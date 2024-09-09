package net.cengiz1.areport;

import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.Sound;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AReport extends JavaPlugin implements Listener, CommandExecutor {

    private Map<String, String[]> reports; // Raporları saklamak için bir map
    private String webhookUrl;

    private String reportedPlayerName; // Raporlanan oyuncunun adını saklamak için değişken
    private Inventory reportCategoryMenu;
    private Plugin placeholderAPI;

    @Override
    public void onEnable() {
        getLogger().info("Plugin enabled");
        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("report").setExecutor(this);
        reports = new HashMap<>(); // Raporları saklamak için boş bir map oluştur
        sendEnableMessages();
        webhookUrl = getConfig().getString("discord.webhook_url");
        placeholderAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderAPI != null) {
            getLogger().info("PlaceholderAPI found! Placeholder support enabled.");
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholder support disabled.");
        }
        createReportCategoryMenu();
        onLoad();
    }

    public void sendEnableMessages() {
        Bukkit.getConsoleSender().sendMessage("§6§l		AREPORT 		§b");
        Bukkit.getConsoleSender().sendMessage("§aBy Developer cengiz1");
        Bukkit.getConsoleSender().sendMessage("§aBy Web Site; AlopeNetwork.com");
    }

    private void createReportCategoryMenu() {
        reportCategoryMenu = getServer().createInventory(null, 9, getConfig().getString("inventory_title"));
        Map<String, Integer> categorySlots = new HashMap<>();
        for (String category : getConfig().getConfigurationSection("report_categories").getKeys(false)) {
            int slot = getConfig().getInt("report_categories." + category + ".slot", -1);
            if (slot < 0 || slot >= 9) {
                getLogger().warning("Invalid slot for category " + category + ": " + slot);
                continue;
            }
            categorySlots.put(category, slot);
        }
        for (Map.Entry<String, Integer> entry : categorySlots.entrySet()) {
            String category = entry.getKey();
            int slot = entry.getValue();
            String materialString = getConfig().getString("category_material." + category);
            if (materialString == null) {
                getLogger().warning("Material for category " + category + " not found!");
                continue;
            }

            Material material = Material.matchMaterial(materialString);
            if (material == null) {
                getLogger().warning("Invalid material for category " + category + ": " + materialString);
                continue;
            }

            ItemStack categoryItem = new ItemStack(material);
            Sound clickSound = Sound.valueOf(getConfig().getString("settings.sound"));
            playSoundForItem(categoryItem, clickSound);
            ItemMeta meta = categoryItem.getItemMeta();
            meta.setDisplayName(category);

            List<String> lore = getConfig().getStringList("category_lore." + category);
            if (lore != null) {
                for (int i = 0; i < lore.size(); i++) {
                    lore.set(i, lore.get(i).replace("&", "§"));
                }
                meta.setLore(lore);
            }

            categoryItem.setItemMeta(meta);
            reportCategoryMenu.setItem(slot, categoryItem);
        }
    }

    private void playSoundForItem(ItemStack item, Sound sound) {
        if (item != null && sound != null) {
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("report")) {
            if (!(sender instanceof Player)) {
                String onlyPlayersMessage = getConfig().getString("messages.player_only_command");
                sender.sendMessage(onlyPlayersMessage);
                return true;
            }

            if (args.length < 1) {
                String usageMessage = getConfig().getString("messages.report_command_usage");
                sender.sendMessage(usageMessage);
                return true;
            }

            Player player = (Player) sender;
            reportedPlayerName = args[0]; // Raporlanan oyuncunun adını sakla
            player.openInventory(reportCategoryMenu); // Open report category menu
            return true;
        }
        return false;
    }

    @EventHandler
    public boolean onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().equals(reportCategoryMenu)) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                String categoryName = event.getCurrentItem().getItemMeta().getDisplayName();
                Player player = (Player) event.getWhoClicked();
                String reporter = player.getName(); // Raporlayan oyuncunun adını al
                String reason = categoryName;
                getLogger().info(reporter + " Raporlayan " + reportedPlayerName + " Sebeb: " + reason);
                reports.put(reportedPlayerName, new String[]{reporter, reason});
                saveReports();
                sendDiscord(reporter, reportedPlayerName, reason);
                player.closeInventory();
                sendReportMessage(player);
                player.sendMessage(getConfig().getString("messages.report_sent").replace("%player%", player.getName()));
            }
        }
        return false;
    }

    private void sendReportMessage(Player player) {
        String reportSentMessage = getConfig().getString("messages.report_sent").replace("%player%", player.getName());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Oyuncu giriş işlemleri
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Oyuncu sohbet işlemleri
    }

    @Override
    public void onLoad() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
    }

    private void saveReports() {
        File configFile = new File(getDataFolder(), "reports.yml");
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            FileWriter writer = new FileWriter(configFile);
            PrintWriter out = new PrintWriter(writer);
            for (String reportedPlayer : reports.keySet()) {
                String[] data = reports.get(reportedPlayer);
                String reporter = data[0];
                String reason = data[1];
                out.println("Rapor: " + reportedPlayer + " | RaporEdilen: " + reporter + " | Sebeb: " + reason);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendDiscord(String reporter, String reportedPlayer, String reason) {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");

        // Embed oluşturma
        String json = "{\"embeds\":[{\"title\":\"Oyuncu Rapor Edildi\",\"color\":16711680,\"fields\":[{\"name\":\"Raporlayan Kisi\",\"value\":\"" +
                reporter + "\"},{\"name\":\"Raporlanan Kisi\",\"value\":\"" + reportedPlayer + "\"},{\"name\":\"Sebep\",\"value\":\"" + reason + "\"}]}]}";

        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url(webhookUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            // response ile ilgili işlemler
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

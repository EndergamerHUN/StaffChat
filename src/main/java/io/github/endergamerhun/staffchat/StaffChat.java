package main.java.io.github.endergamerhun.staffchat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class StaffChat extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        FileConfiguration config = getConfig();
        config.addDefault("format", "§7[§4!!§7]§c {displayname}§8 > §f{message}");
        config.addDefault("prefix", "!");
        getServer().getPluginManager().registerEvents(this, this);
        log("Plugin loaded successfully!");
    }

    @Override
    public void onDisable() {
        log("Unloaded plugin");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        final String prefix = this.getConfig().getString("prefix");
        final  String format = this.getConfig().getString("format");

        String message = e.getMessage();

        if (!(message.length() > prefix.length()) || !message.startsWith(prefix)) return;
        Player sender = e.getPlayer();
        if (!sender.hasPermission("staffchat.send")) return;

        message = message.substring(prefix.length());

        e.setFormat(format
                .replace("{displayname}", sender.getDisplayName())
                .replace("{name}", sender.getName())
                .replace("{message}", message)
        );

        e.getRecipients().removeIf(p -> p != sender && !p.hasPermission("staffchat.read") );
    }


    private static void log(String format, Object... objects) {
        String log = String.format(format, objects);
        Bukkit.getConsoleSender().sendMessage("§7[§cStaff§aChat§7]§f " + log);
    }
}

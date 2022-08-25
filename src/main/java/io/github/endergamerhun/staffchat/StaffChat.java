package main.java.io.github.endergamerhun.staffchat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class StaffChat extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        FileConfiguration config = getConfig();
        config.addDefault("format", "§7[§4!!§7]§c {displayname}§8 > §f{message}");
        config.setComments("format", Arrays.asList(
                "The default format for messages sent in StaffChat.",
                "Placeholders:",
                "{displayname}: Displayname of the player",
                "{name}: Username of the player",
                "{message}: The message being sent to StaffChat",
                "{priority}: Message priority. 0 if priority is disabled"));
        config.addDefault("prefix", "!");
        config.setComments("prefix", Arrays.asList("The prefix needed to speak in StaffChat."));
        config.addDefault("priority", false);
        config.setComments("priority", Arrays.asList("Enable the priority system to separate StaffChat based on permissions."));
        config.addDefault("max-priority", 2);
        config.setComments("max-priority", Arrays.asList("The highest priority possible."));
        config.addDefault("use-highest-priority", false);
        config.setComments("use-highest-priority", Arrays.asList("Uses the highest possible priority instead of lowest if priority is not specified."));
        config.addDefault("priority-format.1", "§7[§6StaffChat§7]§e {displayname}§8 > §f{message}");
        config.addDefault("priority-format.2", "§7[§4AdminChat§7]§c {displayname}§8 > §e{message}");
        config.setComments("priority-format", Arrays.asList("The format to use for each priority. Uses the same placeholders as the default format."));
        config.options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("staffchat").setExecutor(this);
        log("Plugin loaded successfully!");
    }

    @Override
    public void onDisable() {
        log("Unloaded plugin");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        final String prefix = this.getConfig().getString("prefix");
        String message = e.getMessage();
        if (!(message.length() > prefix.length()) || !message.startsWith(prefix)) return;
        Player sender = e.getPlayer();
        message = message.substring(prefix.length());

        String format = this.getConfig().getString("format");
        final int maxPrio = getConfig().getInt("max-priority");
        int priority = 0;

        if (getConfig().getBoolean("priority")) {
            try {
                // Number given
                String[] split = message.split(" ");
                priority = Math.min(maxPrio, Integer.parseInt(split[0]));

                if (!canUse(sender,"send",priority)) {
                    // Find the highest wanted permission
                    for (int i = priority; i > 0; i--) {
                        if (sender.hasPermission("staffchat.send."+i)) {
                            priority = i;
                            break;
                        }
                    }
                }
                message = message.substring(split[0].length()+1);
            } catch (Exception err) {
                // There is no number given
                priority = 1;
                if (getConfig().getBoolean("use-highest-priority")) {
                    for (int i = maxPrio; i > 0; i--) {
                        if (sender.hasPermission("staffchat.send."+i))
                            priority = i;
                            break;
                    }
                }
            }
        }
        if (!(message.length() > 0)) return;
        if (!canUse(sender, "send", priority)) return;
        if (getConfig().contains("priority-format." + priority, true)) format = getConfig().getString("priority-format." + priority);

        e.setFormat(format
                .replace("{displayname}", sender.getDisplayName())
                .replace("{name}", sender.getName())
                .replace("{message}", message)
                .replace("{priority}", Integer.toString(priority))
        );

        int finalPriority = priority;
        e.getRecipients().removeIf(p -> p != sender && !canUse(p, "read", finalPriority) );
    }

    private boolean canUse(Player p, String mode, int priority) {
        if (priority == 0) return p.hasPermission("staffchat."+mode);

        final int max = getConfig().getInt("max-priority");
        for (int i = priority; !(i > max); i++) {
            if (p.hasPermission("staffchat."+mode+"."+i)) return true;
        }
        return false;
    }

    private static void log(String format, Object... objects) {
        String log = String.format(format, objects);
        Bukkit.getConsoleSender().sendMessage("§7[§cStaff§aChat§7]§f " + log);
    }
}

package main.java.io.github.endergamerhun.staffchat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class StaffChat extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        saveDefaultConfig();
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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) return true;
        if (args[0].equalsIgnoreCase("reload")) reloadConfig();
        return true;
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

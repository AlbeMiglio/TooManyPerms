package me.alex.toomanyperms;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Main extends JavaPlugin implements Listener {
    private static Main instance;

    public static Main getInstance() { return instance; }

    public void onEnable() {
        instance = this;
        getServer().getLogger().info(color("&cpompiere1 gay"));
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        getCommand("tmp").setExecutor(new CommandTMP());
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    checkPlayer(p);
                }
            }
        }, 20L, 20L);
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
       Player p = event.getPlayer();
       checkPlayer(p);
    }

    private String color(String message) {
        String prefix = getConfig().getString("Messages.Prefix");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    private void deopPlayer(Player target, List<String> cmdlist) {
        target.setOp(false);
        if (getConfig().getBoolean("Punish")) {
            for (String commandline : cmdlist) {
                getServer().dispatchCommand(getServer().getConsoleSender(), ChatColor.translateAlternateColorCodes('&', commandline.replaceAll("%player%", target.getName())));
            }
        }
    }

    private void depexPlayer(Player target, List<String> cmdlist, String permission) {
        String perm = permission.replaceAll("_", ".");
        getServer().dispatchCommand(getServer().getConsoleSender(), "pex user "+target.getName()+" remove "+perm);
        if (getConfig().getBoolean("Punish")) {
            for (String commandline : cmdlist) {
                getServer().dispatchCommand(getServer().getConsoleSender(), ChatColor.translateAlternateColorCodes('&', commandline.replaceAll("%player%", target.getName())));
            }
        }
    }

    private void checkPlayer(Player target) {
        if (getConfig().getBoolean("Operators.UUID-check")) {
            if (!getConfig().getStringList("Operators.UUIDs").contains(target.getUniqueId().toString())) {
                if (target.isOp()) {
                    deopPlayer(target, getConfig().getStringList("Punish Commands"));
                }
            }
        }
        if (getConfig().getBoolean("Operators.Nick-check")) {
            if (!getConfig().getStringList("Operators.Nicknames").contains(target.getName())) {
                if (target.isOp()) {
                    deopPlayer(target, getConfig().getStringList("Punish Commands"));
                }
            }
        }
        for (String permission : getConfig().getConfigurationSection("Permissions").getKeys(false)) {
            if (target.hasPermission(permission)) {
                if (getConfig().getBoolean("Permissions." + permission + ".Nick-check")) {
                    if (!getConfig().getStringList("Permissions." + permission + ".Nicknames").contains(target.getName())) {
                        depexPlayer(target, getConfig().getStringList("Punish Commands"), permission);
                    }
                }
                if (getConfig().getBoolean("Permissions." + permission + ".UUID-check")) {
                    if (!getConfig().getStringList("Permissions." + permission + ".UUIDs").contains(target.getUniqueId().toString())) {
                        depexPlayer(target, getConfig().getStringList("Punish Commands"), permission);
                    }
                }
            }
        }
    }
}

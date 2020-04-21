package it.mycraft.toomanyperms;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import it.mycraft.toomanyperms.commands.CommandTMP;
import it.mycraft.toomanyperms.events.UnfairGroupsDetectedEvent;
import it.mycraft.toomanyperms.events.UnfairOpDetectedEvent;
import it.mycraft.toomanyperms.events.UnfairPermsDetectedEvent;
import net.milkbowl.vault.permission.Permission;
import java.util.ArrayList;

public class TooManyPerms extends JavaPlugin {

    @Getter
    private static TooManyPerms instance;

    @Getter
    private Permission vaultPermission;

    @Getter
    private ConfigManager configManager;

    public void onEnable() {
        instance = this;
        this.configManager = new ConfigManager(this);
        this.getConfigManager().reloadConfiguration();
        getCommand("tmp").setExecutor(new CommandTMP());
        if (!setupPermissions()) {
            Bukkit.getConsoleSender().sendMessage(prefix("&cSEVERE ERROR - CANNOT ENABLE TOOMANYPERMS v" + getDescription().getVersion()));
            Bukkit.getConsoleSender().sendMessage(prefix("&eUNABLE TO FIND VAULT! PLEASE ADD IT TO YOUR SERVER!"));
        }
        Bukkit.getConsoleSender().sendMessage(prefix("&aEnabling..."));
        Bukkit.getConsoleSender().sendMessage(prefix("&aEnabled!"));
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (Bukkit.getServer().getOnlinePlayers() != null) {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    checkPlayer(p);
                }
            }
        }, 30L, 30L);
    }

    public boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        this.vaultPermission = rsp.getProvider();
        return this.vaultPermission != null;
    }

    public String prefix(String message) {
        if (this.getConfigManager().getMessages().getBoolean("Messages.Use-Prefix")) {
            String prefix = this.getConfigManager().getMessages().getString("Messages.Prefix");
            return color(prefix + message);
        } else return color(message);
    }

    public String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private void punishPlayer(Player target) {
        if (this.getConfigManager().getPunishments().getBoolean("Punish")) {
            for (String commandline : this.getConfigManager().getPunishments().getStringList("Punish Commands")) {
                if (commandline.startsWith("@")) {
                    getServer().dispatchCommand(getServer().getConsoleSender(), color(commandline
                            .replaceAll("%player%", target.getName())
                            .replaceFirst("@", "")));
                } else {
                    target.chat("/" + commandline.replaceAll("%player%", target.getName()));
                }
            }
        }
    }

    private void checkPlayer(Player target) {
        if (!this.getExcludedNicknames().contains(target.getName())) {
            /* Check for unfair operators, you just have to enable in config Nick-check and/or UUID-check! */
            if (getConfig().getBoolean("Checks.OP")) {
                if (this.getConfigManager().getPermissions().getBoolean("Operators.Nick-check")) {
                    if (target.isOp()) {
                        if (!this.getAllowedUsersForOp().contains(target.getName())) {
                            target.setOp(false);
                            punishPlayer(target);
                            UnfairOpDetectedEvent event = new UnfairOpDetectedEvent(target);
                            Bukkit.getPluginManager().callEvent(event);
                        }
                    }
                }
                if (this.getConfigManager().getPermissions().getBoolean("Operators.UUID-check")) {
                    if (!this.getAllowedUUIDsForOp().contains(target.getUniqueId().toString())) {
                        if (target.isOp()) {
                            target.setOp(false);
                            punishPlayer(target);
                            UnfairOpDetectedEvent event = new UnfairOpDetectedEvent(target);
                            Bukkit.getPluginManager().callEvent(event);
                        }
                    }
                }
            }
            /* Check for unfair permissions, just add in config as many permissions as you want! 100% lag-free! */
            if (getConfig().getBoolean("Checks.Permissions")) {
                for (String permission : this.getConfigManager().getPermissions().getConfigurationSection("Permissions").getKeys(true)) {
                    if (target.hasPermission(permission.replaceAll("[_]", "."))) {
                        if (this.getConfigManager().getPermissions().getBoolean("Permissions." + permission + ".Nick-check")) {
                            if (!this.getConfigManager().getPermissions().getStringList("Permissions." + permission + ".Nicknames").contains(target.getName())) {
                                removePermission(target.getName(), permission.replaceAll("[_]", "."));
                                punishPlayer(target);
                                UnfairPermsDetectedEvent event = new UnfairPermsDetectedEvent(target, permission.replaceAll("[_]", "."));
                                Bukkit.getPluginManager().callEvent(event);
                            }
                        }
                        if (this.getConfigManager().getPermissions().getBoolean("Permissions." + permission + ".UUID-check")) {
                            if (!this.getConfigManager().getPermissions().getStringList("Permissions." + permission + ".UUIDs").contains(target.getUniqueId().toString())) {
                                removePermission(target.getName(), permission.replaceAll("[_]", "."));
                                punishPlayer(target);
                                UnfairPermsDetectedEvent event = new UnfairPermsDetectedEvent(target, permission.replaceAll("[_]", "."));
                                Bukkit.getPluginManager().callEvent(event);
                            }
                        }
                    }
                }
            }
            /* Check for unfair groups, just add in config as many groups as you prefer! Works also with inheritance groups! */
            if (getConfig().getBoolean("Checks.Groups")) {
                for (String group : this.getConfigManager().getPermissions().getConfigurationSection("Groups").getKeys(false)) {
                    if (getVaultPermission().playerInGroup(target.getWorld().getName(), target, group)) {
                        if (this.getConfigManager().getPermissions().getBoolean("Groups." + group + ".Nick-check")) {
                            if (!this.getAllowedUsersForGroup(group).contains(target.getName())) {
                                removeGroup(target.getName(), group);
                                punishPlayer(target);
                                UnfairGroupsDetectedEvent event = new UnfairGroupsDetectedEvent(target, group);
                                Bukkit.getPluginManager().callEvent(event);
                            }
                        }
                        if (this.getConfigManager().getPermissions().getBoolean("Groups." + group + ".UUID-check")) {
                            if (!this.getAllowedUUIDsForGroup(group).contains(target.getUniqueId().toString())) {
                                removeGroup(target.getName(), group);
                                punishPlayer(target);
                                UnfairGroupsDetectedEvent event = new UnfairGroupsDetectedEvent(target, group);
                                Bukkit.getPluginManager().callEvent(event);
                            }
                        }
                    }
                }
            }
        }
    }

    public void removePermission(String player, String permission) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getConfig().getString("Commands.Remove-Permission")
                .replaceAll("%player%", player)
                .replaceAll("%perm%", permission));
    }

    public void removeGroup(String player, String group) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getConfig().getString("Commands.Remove-Group")
                .replaceAll("%player%", player)
                .replaceAll("%group%", group));
    }

    public ArrayList<String> getAllowedUsersForOp() {
        return (ArrayList<String>) this.getConfigManager().getPermissions().getStringList("Operators.Nicknames");
    }

    public ArrayList<String> getAllowedUUIDsForOp() {
        return (ArrayList<String>) this.getConfigManager().getPermissions().getStringList("Operators.UUIDs");
    }

    public ArrayList<String> getAllowedUsersForPerm(String perm) {
        return (ArrayList<String>) this.getConfigManager().getPermissions().getStringList("Permissions." + perm + ".Nicknames");
    }

    public ArrayList<String> getAllowedUUIDsForPerm(String perm) {
        return (ArrayList<String>) this.getConfigManager().getPermissions().getStringList("Permissions." + perm + ".UUIDs");
    }

    public ArrayList<String> getAllowedUsersForGroup(String group) {
        return (ArrayList<String>) this.getConfigManager().getPermissions().getStringList("Groups." + group + ".Nicknames");
    }

    public ArrayList<String> getAllowedUUIDsForGroup(String group) {
        return (ArrayList<String>) this.getConfigManager().getPermissions().getStringList("Groups." + group + ".UUIDs");
    }

    public ArrayList<String> getExcludedNicknames() {
        return (ArrayList<String>) this.getConfigManager().getPermissions().getStringList("Excluded-Nicknames");
    }

}

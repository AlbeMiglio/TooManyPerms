package it.mycraft.toomanyperms;

import it.mycraft.powerlib.chat.Message;
import it.mycraft.powerlib.config.ConfigManager;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import it.mycraft.toomanyperms.commands.CommandTMP;
import it.mycraft.toomanyperms.events.UnfairGroupsDetectedEvent;
import it.mycraft.toomanyperms.events.UnfairOpDetectedEvent;
import it.mycraft.toomanyperms.events.UnfairPermsDetectedEvent;
import net.milkbowl.vault.permission.Permission;

import java.util.ArrayList;

import static it.mycraft.powerlib.utils.ColorAPI.color;

public class TooManyPerms extends JavaPlugin {

    @Getter
    private static TooManyPerms instance;

    @Getter
    private Permission vaultPermission;

    @Getter
    private ConfigManager configManager;

    @Getter
    private PermissionsManager permissionsManager;

    @Getter
    private FileConfiguration config;

    @Getter
    private FileConfiguration messages;

    @Getter
    private FileConfiguration permissions;

    @Getter
    private FileConfiguration punishments;

    public void onEnable() {
        instance = this;
        this.configManager = new ConfigManager(this);
        this.configManager.create("config.yml");
        this.configManager.create("messages.yml");
        this.configManager.create("permissions.yml");
        this.configManager.create("punishments.yml");
        this.permissionsManager = new PermissionsManager(this);
        getCommand("tmp").setExecutor(new CommandTMP());

        if(!Bukkit.getPluginManager().isPluginEnabled("PowerLib")) {
            new Message(prefix("&cSEVERE ERROR - CANNOT ENABLE TOOMANYPERMS v" + getDescription().getVersion()))
            .send(Bukkit.getConsoleSender());
            new Message(prefix("&eUNABLE TO FIND &n&lPowerLib&e! PLEASE ADD IT TO YOUR SERVER!"))
            .send(Bukkit.getConsoleSender());
        }
        if (!setupPermissions()) {
            new Message(prefix("&cSEVERE ERROR - CANNOT ENABLE TOOMANYPERMS v" + getDescription().getVersion()))
                    .send(Bukkit.getConsoleSender());
            new Message(prefix("&eUNABLE TO FIND &n&lVault&e! PLEASE ADD IT TO YOUR SERVER!"))
                    .send(Bukkit.getConsoleSender());
        }
        Bukkit.getConsoleSender().sendMessage(prefix("&aEnabling..."));
        Bukkit.getConsoleSender().sendMessage(prefix("&aEnabled!"));
        this.startTasks();
    }

    public boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        this.vaultPermission = rsp.getProvider();
        return this.vaultPermission != null;
    }

    public String prefix(String message) {
        if (this.getMessages().getBoolean("Messages.Use-Prefix")) {
            String prefix = this.getMessages().getString("Messages.Prefix");
            return color(prefix + message);
        } else return color(message);
    }

    void punishPlayer(Player target) {
        if (this.getPunishments().getBoolean("Punish")) {
            for (String commandline : this.getPunishments().getStringList("Punish Commands")) {
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

    private void startTasks() {
        new CheckTask(this).runTaskTimerAsynchronously(this, 0, 30L);
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
}

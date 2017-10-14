package me.alex.toomanyperms;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class CommandTMP implements CommandExecutor {

    private Main main = Main.getInstance();
    private FileConfiguration getConfig() {
        return this.main.getConfig();
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ((sender instanceof Player)) {
            if (!sender.hasPermission("tmp.use")) {
                sender.sendMessage(color(getConfig().getString("Messages.Not-Enough-Permissions")));
                return false;
            }
            if (args.length <= 0) {
                usageCommand(sender);
                return false;
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("tmp.reload")) {
                    sender.sendMessage(color(getConfig().getString("Messages.Not-Enough-Permissions")));
                    return false;
                } else {
                    this.main.reloadConfig();
                    this.main.saveConfig();
                    sender.sendMessage(color(getConfig().getString("Messages.Success-Reload")));
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("check")) {
                if (!sender.hasPermission("tmp.check")) {
                    sender.sendMessage(color(getConfig().getString("Messages.Not-Enough-Permissions")));
                    return false;
                }
                if (args.length != 2) {
                    usageCommand(sender);
                    return false;
                }
                String perm = args[1];
                if (getConfig().getConfigurationSection("Permissions").getKeys(false).contains(perm)) {
                    for (String permission : getConfig().getConfigurationSection("Permissions").getKeys(false)) {
                        if (perm.equals(permission)) {
                            if (getConfig().getBoolean("Permissions." + perm + ".Nick-check")) {
                                String list = "";
                                for (String nick : getConfig().getStringList("Permissions." + perm + ".Nicknames")) {
                                    list += nick + "; ";
                                }
                                sender.sendMessage(color(getConfig().getString("Messages.Perm-Nicks-List"))
                                        .replaceAll("%perm%", perm.replaceAll("_", "."))
                                        .replaceAll("%list%", list));
                            }
                            if (getConfig().getBoolean("Permissions." + perm + ".UUID-check")) {
                                String list = "";
                                for (String uuid : getConfig().getStringList("Permissions." + perm + ".UUIDs")) {
                                    list += uuid + "; ";
                                }
                                sender.sendMessage(color(getConfig().getString("Messages.Perm-UUIDs-List"))
                                        .replaceAll("%perm%", perm.replaceAll("_", "."))
                                        .replaceAll("%list%", list));
                            }
                        }
                    }
                } else {
                    sender.sendMessage(color(getConfig().getString("Messages.Perm-Not-Protected").replaceAll("%perm%", perm.replaceAll("_", "."))));
                }
                return false;
            }
            else if (args[0].equalsIgnoreCase("opcheck")) {
                if (!sender.hasPermission("tmp.opcheck")) {
                    sender.sendMessage(color(getConfig().getString("Messages.Not-Enough-Permissions")));
                    return false;
                }
                if (args.length != 1) {
                    usageCommand(sender);
                    return false;
                }
                if (getConfig().getBoolean("Operators.Nick-check")) {
                    String list = "";
                    for (String nick : getConfig().getStringList("Operators.Nicknames")) {
                        list += nick + "; ";
                    }
                    sender.sendMessage(color(getConfig().getString("Messages.Op-Nicks-List"))
                            .replaceAll("%list%", list));
                }
                if (getConfig().getBoolean("Operators.UUID-check")) {
                    String list = "";
                    for (String uuid : getConfig().getStringList("Operators.UUIDs")) {
                        list += uuid + "; ";
                    }
                    sender.sendMessage(color(getConfig().getString("Messages.Op-UUIDs-List"))
                            .replaceAll("%list%", list));
                }
                return false;
            }
            else {
                usageCommand(sender);
                return false;
            }
        } else if (sender instanceof ConsoleCommandSender) {
            if (args.length <= 0) {
                usageCommand(sender);
                return false;
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                this.main.reloadConfig();
                this.main.saveConfig();
                sender.sendMessage(color(getConfig().getString("Messages.Success-Reload")));
                return false;
            }
            else if (args[0].equalsIgnoreCase("check")) {
                if (args.length != 2) {
                    usageCommand(sender);
                    return false;
                }
                String perm = args[1];
                if (getConfig().getConfigurationSection("Permissions").getKeys(false).contains(perm)) {
                    for (String permission : getConfig().getConfigurationSection("Permissions").getKeys(false)) {
                        if (perm.equals(permission)) {
                            if (getConfig().getBoolean("Permissions." + perm + ".Nick-check")) {
                                String list = "";
                                for (String nick : getConfig().getStringList("Permissions." + perm + ".Nicknames")) {
                                    list += nick + "; ";
                                }
                                sender.sendMessage(color(getConfig().getString("Messages.Perm-Nicks-List"))
                                        .replaceAll("%perm%", perm.replaceAll("_", "."))
                                        .replaceAll("%list%", list));
                            }
                            if (getConfig().getBoolean("Permissions." + perm + ".UUID-check")) {
                                String list = "";
                                for (String uuid : getConfig().getStringList("Permissions." + perm + ".UUIDs")) {
                                    list += uuid + "; ";
                                }
                                sender.sendMessage(color(getConfig().getString("Messages.Perm-UUIDs-List"))
                                        .replaceAll("%perm%", perm.replaceAll("_", "."))
                                        .replaceAll("%list%", list));
                            }
                        }
                    }
                }
                else {
                    sender.sendMessage(color(getConfig().getString("Messages.Perm-Not-Protected").replaceAll("%perm%", perm.replaceAll("_", "."))));
                }
                return false;
            }
            else if (args[0].equalsIgnoreCase("opcheck")) {
                if (args.length != 1) {
                    usageCommand(sender);
                    return false;
                }
                if (getConfig().getBoolean("Operators.Nick-check")) {
                    String list = "";
                    for (String nick : getConfig().getStringList("Operators.Nicknames")) {
                        list += nick + "; ";
                    }
                    sender.sendMessage(color(getConfig().getString("Messages.Op-Nicks-List"))
                            .replaceAll("%list%", list));
                }
                if (getConfig().getBoolean("Operators.UUID-check")) {
                    String list = "";
                    for (String uuid : getConfig().getStringList("Operators.UUIDs")) {
                        list += uuid + "; ";
                    }
                    sender.sendMessage(color(getConfig().getString("Messages.Op-UUIDs-List"))
                            .replaceAll("%list%", list));
                }
                return false;
            }
            else {
                usageCommand(sender);
                return false;
            }
        }
        return true;
    }
    private String color(String message) {
            String prefix = getConfig().getString("Messages.Prefix");
            return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
    private void usageCommand(CommandSender sender) {
        for (String line : getConfig().getStringList("Help Lines")) {
            sender.sendMessage(color(line).replaceAll("%player%", sender.getName()));
        }
    }
}

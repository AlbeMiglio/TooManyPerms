package it.mycraft.toomanyperms.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import it.mycraft.toomanyperms.TooManyPerms;
import it.mycraft.toomanyperms.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class CommandTMP implements CommandExecutor {

    private TooManyPerms main = TooManyPerms.getInstance();
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	
            /* Without this check, everyone could do /tmp and see the usageCommand help! */
            if (!hasPermission(sender, Permissions.USE_COMMAND.toString())) {
                sender.sendMessage(color(getMessages().getString("Messages.Not-Enough-Permissions")));
                return false;
            }
            /* If users type just /tmp, it will be sent to them. */
            if (args.length <= 0) {
                usageCommand(sender);
                return false;
            }
            /* Just /tmp reload part of code. */
            else if (args[0].equalsIgnoreCase("reload")) {
                if (!hasPermission(sender, Permissions.RELOAD_COMMAND.toString())) {
                    sender.sendMessage(color(getMessages().getString("Messages.Not-Enough-Permissions")));
                    return false;
                } 
                else {
                	main.reloadConfiguration();
                    sender.sendMessage(color(getMessages().getString("Messages.Success-Reload")));
                    return false;
                }
            }
            /* Just /tmp check part of code. */
            else if (args[0].equalsIgnoreCase("check")) {
                if (!hasPermission(sender, Permissions.CHECK_COMMAND.toString())) {
                    sender.sendMessage(color(getMessages().getString("Messages.Not-Enough-Permissions")));
                    return false;
                }
                if (args.length != 2) {
                    usageCommand(sender);
                    return false;
                }
                String perm = args[1];
                if(args[1].contains(".")) {
                	perm = args[1].replaceAll("[.]", "_");
                }
                
                String nicksList = "";
                String uuidsList = "";
                if(getPermissions().getConfigurationSection("Permissions").getKeys(false).contains(perm)) {
                  if(getPermissions().getBoolean("Permissions."+perm+".Nick-check")) {
                    for (String nick : main.getAllowedUsersForPerm(perm)) {
                        nicksList += nick + "; ";
                    }
                    sender.sendMessage(color(getMessages().getString("Messages.Perm-Nicks-List"))
                	    .replaceAll("%perm%", perm.replaceAll("_", "."))
                		.replaceAll("%list%", nicksList));
                  }
                  if(getPermissions().getBoolean("Permissions."+perm+".UUID-check")) {
                    for (String uuid : main.getAllowedUUIDsForPerm(perm)) {
                        uuidsList += uuid + "; ";
                    }
                    sender.sendMessage(color(getMessages().getString("Messages.Perm-UUIDs-List"))
                        .replaceAll("%perm%", perm.replaceAll("_", "."))
                        .replaceAll("%list%", uuidsList));
                  }
                } 
                else {
                    sender.sendMessage(color(getMessages().getString("Messages.Perm-Not-Protected")
                    	.replaceAll("%perm%", perm.replaceAll("_", "."))));
                }
                return false;
            }
            /* New /tmp groupcheck part of code! Please check for PermissionsEx API for groups check. */
            else if (args[0].equalsIgnoreCase("groupcheck")) {
                if (!hasPermission(sender, Permissions.GROUPCHECK_COMMAND.toString())) {
                    sender.sendMessage(color(getMessages().getString("Messages.Not-Enough-Permissions")));
                    return false;
                }
                if (args.length != 2) {
                    usageCommand(sender);
                    return false;
                }
                String group = args[1];
                String nicksList = "";
                String uuidsList = "";
                for(String check : getPermissions().getConfigurationSection("Groups").getKeys(false)) {
                	if(args[1].equalsIgnoreCase(check)) {
                		group = check;
                	}
                }
                if(getPermissions().getConfigurationSection("Groups").getKeys(false).contains(group)) {
                  if(getPermissions().getBoolean("Groups."+group+".Nick-check")) {
                    for (String nick : main.getAllowedUsersForGroup(group)) {
                        nicksList += nick + "; ";
                    }
                    sender.sendMessage(color(getMessages().getString("Messages.Group-Nicks-List"))
                	    .replaceAll("%group%", group)
                		.replaceAll("%list%", nicksList));
                  }
                  if(getPermissions().getBoolean("Groups."+group+".UUID-check")) {
                    for (String uuid : main.getAllowedUUIDsForGroup(group)) {
                        uuidsList += uuid + "; ";
                    }
                    sender.sendMessage(color(getMessages().getString("Messages.Group-UUIDs-List"))
                        .replaceAll("%group%", group)
                        .replaceAll("%list%", uuidsList));
                  }
                } 
                else {
                    sender.sendMessage(color(getMessages().getString("Messages.Group-Not-Protected")
                    	.replaceAll("%group%", group)));
                }
                return false;
            }
            /* Just /tmp opcheck part of code. */ 
            else if (args[0].equalsIgnoreCase("opcheck")) {
                if (!hasPermission(sender, Permissions.OPCHECK_COMMAND.toString())) {
                    sender.sendMessage(color(getMessages().getString("Messages.Not-Enough-Permissions")));
                    return false;
                }
                if (args.length != 1) {
                    usageCommand(sender);
                    return false;
                }
                if(!((getPermissions().getBoolean("Operators.Nick-check")) && (getPermissions().getBoolean("Operators.Nick-check")))) {
                	sender.sendMessage(color(getMessages().getString("Messages.Op-Protection-Disabled")));
                    return false;
                }
                if (getPermissions().getBoolean("Operators.Nick-check")) {
                    String list = "";
                    for (String nick : getPermissions().getStringList("Operators.Nicknames")) {
                        list += nick + "; ";
                    }
                    sender.sendMessage(color(getMessages().getString("Messages.Op-Nicks-List"))
                            .replaceAll("%list%", list));
                }
                if (getPermissions().getBoolean("Operators.UUID-check")) {
                    String list = "";
                    for (String uuid : getPermissions().getStringList("Operators.UUIDs")) {
                        list += uuid + "; ";
                    }
                    sender.sendMessage(color(getMessages().getString("Messages.Op-UUIDs-List"))
                            .replaceAll("%list%", list));
                }
                return false;
            }
            /* If the args[0] is not reload, check, opcheck or groupcheck, usageCommand will be sent to the commandSender. */ 
            else {
                usageCommand(sender);
                return false;
            }
    }
    
    private boolean hasPermission(CommandSender sender, String permission) {
    	boolean perm = false;
    	if(sender instanceof Player) {
    		if(sender.hasPermission(permission)) {
    			perm = true;
    		}
    		else perm = false;
    	}
    	if(sender instanceof ConsoleCommandSender) {
    		perm = true;
    	}
    	return perm;
    }
    
    private String color(String message) {
            String prefix = getMessages().getString("Messages.Prefix");
            if(getMessages().getBoolean("Messages.Use-Prefix")) {
            return ChatColor.translateAlternateColorCodes('&', prefix + message);
            }
            else return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    private void usageCommand(CommandSender sender) {
        for (String line : getMessages().getStringList("Help Lines")) {
            sender.sendMessage(color(line).replaceAll("%player%", sender.getName()));
        }
    }
    
    private FileConfiguration getMessages() { return main.getMessages(); }
    
    private FileConfiguration getPermissions() { return main.getPermissions(); }
}

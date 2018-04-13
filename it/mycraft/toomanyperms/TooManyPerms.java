package it.mycraft.toomanyperms;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.common.base.Charsets;
import it.mycraft.toomanyperms.commands.CommandTMP;
import it.mycraft.toomanyperms.events.UnfairGroupsDetectedEvent;
import it.mycraft.toomanyperms.events.UnfairOpDetectedEvent;
import it.mycraft.toomanyperms.events.UnfairPermsDetectedEvent;
import net.milkbowl.vault.permission.Permission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;

public class TooManyPerms extends JavaPlugin {
	
    private static TooManyPerms instance;
    public static TooManyPerms getInstance() { return instance; }
    private Permission vaultPermission;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration permissions;
    private FileConfiguration punishments;

    public void onEnable() {
    	
        instance = this;
        getYAML(new File(getDataFolder(), "config.yml"));
        getYAML(new File(getDataFolder(), "messages.yml"));
        getYAML(new File(getDataFolder(), "permissions.yml"));
        getYAML(new File(getDataFolder(), "punishments.yml"));
        getCommand("tmp").setExecutor(new CommandTMP());
        setupPermissions();
        reloadConfiguration();
        Bukkit.getConsoleSender().sendMessage(prefix("&aEnabling..."));
        Bukkit.getConsoleSender().sendMessage(prefix("&aEnabled!"));
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
            	if(Bukkit.getServer().getOnlinePlayers() != null) {
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        checkPlayer(p);
                    }
            	}
            }
        }, 30L, 30L);
    }
    
    public boolean setupPermissions() {
      RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
      this.vaultPermission = ((Permission)rsp.getProvider());
      return this.vaultPermission != null;
    }
    
    public Permission getPermission() {
      return this.vaultPermission;
    }
    
    public void reloadConfiguration() {
    	
    	this.config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
    	this.messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
    	this.permissions = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "permissions.yml"));
    	this.punishments = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "punishments.yml"));
      
        InputStream defConfigStream = getResource("config.yml");
        InputStream defMessagesStream = getResource("messages.yml");
        InputStream defPermissionsStream = getResource("permissions.yml");
        InputStream defPunishmentsStream = getResource("punishments.yml");
      
        if((defConfigStream == null)
        || (defMessagesStream == null)
        || (defPermissionsStream == null)
        || (defPunishmentsStream == null)) { return; }
      
        this.config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
        this.messages.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defMessagesStream, Charsets.UTF_8)));
        this.permissions.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defPermissionsStream, Charsets.UTF_8)));
        this.punishments.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defPunishmentsStream, Charsets.UTF_8)));
    }
    
    public FileConfiguration getConfig() {
        return this.config;
    }
    
    public FileConfiguration getMessages() {
    	return this.messages;
    }
    
    public FileConfiguration getPermissions() {
    	return this.permissions;
    }
    
    public FileConfiguration getPunishments() {
    	return this.punishments;
    }
    
    private void getYAML(File resourcePath) {
        if(!resourcePath.exists()) {
            createYAML(resourcePath.getName(), false);
        }
    }
    
    private void createYAML(String resourcePath, boolean replace) {
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = this.getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + (new File(getDataFolder(), resourcePath+".yml")));
            } else {
                File outFile = new File(getDataFolder(), resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }

                try {
                    if (outFile.exists() && !replace) {
                        Bukkit.getLogger().log(Level.WARNING, outFile.getName() + "loaded with success!");
                    } else {
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];

                        int len;
                        while((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException var10) {
                    Bukkit.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
                }

            }
        } 
        else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }
    
    private String prefix(String message) {
        if(getMessages().getBoolean("Messages.Use-Prefix")) {
        	String prefix = getMessages().getString("Messages.Prefix");
        	return color(prefix + message);
        }
        else return color(message);
    }
    
    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    private void punishPlayer(Player target) {
    	if (getPunishments().getBoolean("Punish")) {
            for (String commandline : getPunishments().getStringList("Punish Commands")) {
            	if(commandline.startsWith("@")) {
                getServer().dispatchCommand(getServer().getConsoleSender(), color(commandline
                		.replaceAll("%player%", target.getName())
                		.replaceFirst("@", "")));
            	}
            	else {
            		target.chat("/"+commandline.replaceAll("%player%", target.getName()));
            	}
            }
        }
    }

    private void checkPlayer(Player target) {
    	/* Check for unfair operators, you just have to enable in config Nick-check and/or UUID-check! */
    if(getConfig().getBoolean("Checks.OP")) {
    	if (getPermissions().getBoolean("Operators.Nick-check")) {
    		if (target.isOp()) {
                if (!getPermissions().getStringList("Operators.Nicknames").contains(target.getName())) {
                    target.setOp(false);
                    punishPlayer(target);
                    UnfairOpDetectedEvent event = new UnfairOpDetectedEvent(target);
                    Bukkit.getPluginManager().callEvent(event);
                }
            }
        }
        if (getPermissions().getBoolean("Operators.UUID-check")) {
            if (!getPermissions().getStringList("Operators.UUIDs").contains(target.getUniqueId().toString())) {
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
    if(getConfig().getBoolean("Checks.Permissions")) {
        for (String permission : getPermissions().getConfigurationSection("Permissions").getKeys(true)) {
            if (target.hasPermission(permission.replaceAll("[_]", "."))) {
                if (getPermissions().getBoolean("Permissions." + permission + ".Nick-check")) {
                    if (!getPermissions().getStringList("Permissions." + permission + ".Nicknames").contains(target.getName())) {
                    	removePermission(target.getName(), permission.replaceAll("[_]", "."));
                        punishPlayer(target);
                        UnfairPermsDetectedEvent event = new UnfairPermsDetectedEvent(target, permission.replaceAll("[_]", "."));
                        Bukkit.getPluginManager().callEvent(event);
                    }
                }
                if (getPermissions().getBoolean("Permissions." + permission + ".UUID-check")) {
                    if (!getPermissions().getStringList("Permissions." + permission + ".UUIDs").contains(target.getUniqueId().toString())) {
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
    if(getConfig().getBoolean("Checks.Groups")) {
        for (String group : getPermissions().getConfigurationSection("Groups").getKeys(false)) {
        	if(getPermission().playerInGroup(target.getWorld().getName(), target, group)) {
        		if (getPermissions().getBoolean("Groups." + group + ".Nick-check")) {
        			if (!getPermissions().getStringList("Groups." + group + ".Nicknames").contains(target.getName())) {
        				removeGroup(target.getName(), group);
                        punishPlayer(target);
                        UnfairGroupsDetectedEvent event = new UnfairGroupsDetectedEvent(target, group);
                        Bukkit.getPluginManager().callEvent(event);
                    }
        		}
        		if (getPermissions().getBoolean("Groups." + group + ".UUID-check")) {
        			if (!getPermissions().getStringList("Groups." + group + ".UUIDs").contains(target.getUniqueId().toString())) {
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
    	ArrayList<String> list = new ArrayList<String>();
        if (getPermissions().getBoolean("Operators.Nick-check")) {
            for (String uuid : getPermissions().getStringList("Operators.Nicknames")) {
                list.add(uuid);
            }
        }
    	if(list.isEmpty()) {
    		list.add("null");
    	}
    	return list;
    }
    
    public ArrayList<String> getAllowedUUIDsForOp() {
    	ArrayList<String> list = new ArrayList<String>();
        if (getPermissions().getBoolean("Operators.UUID-check")) {
            for (String uuid : getPermissions().getStringList("Operators.UUIDs")) {
                list.add(uuid);
            }
        }
    	if(list.isEmpty()) {
    		list.add("null");
    	}
    	return list;
    }
    
    public ArrayList<String> getAllowedUsersForPerm(String perm) {
    	ArrayList<String> list = new ArrayList<String>();
    	if (getPermissions().getConfigurationSection("Permissions").getKeys(false).contains(perm)) {
            for (String permission : getPermissions().getConfigurationSection("Permissions").getKeys(false)) {
                if (perm.equalsIgnoreCase(permission)) {
                    if (getPermissions().getBoolean("Permissions." + permission + ".Nick-check")) {
                        for (String nick : getPermissions().getStringList("Permissions." + permission + ".Nicknames")) {
                        	list.add(nick);
                        }
                    }
                }
            }
        }
    	if(list.isEmpty()) {
    		list.add("null");
    	}
    	return list;
    }
    
    public ArrayList<String> getAllowedUUIDsForPerm(String perm) {
    	ArrayList<String> list = new ArrayList<String>();
    	if (getPermissions().getConfigurationSection("Permissions").getKeys(false).contains(perm)) {
            for (String permission : getPermissions().getConfigurationSection("Permissions").getKeys(false)) {
                if (perm.equalsIgnoreCase(permission)) {
                    if (getPermissions().getBoolean("Permissions." + permission + ".UUID-check")) {
                        for (String uuid : getPermissions().getStringList("Permissions." + permission + ".UUIDs")) {
                            list.add(uuid);
                        }
                    }
                }
            }
        }
    	if(list.isEmpty()) {
    		list.add("null");
    	}
    	return list;
    }
    
    public ArrayList<String> getAllowedUsersForGroup(String group) {
    	ArrayList<String> list = new ArrayList<String>();
    	if (getPermissions().getConfigurationSection("Groups").getKeys(false).contains(group)) {
            for (String checkgroup : getPermissions().getConfigurationSection("Groups").getKeys(false)) {
                if (group.equalsIgnoreCase(checkgroup)) {
                    if (getPermissions().getBoolean("Groups." + checkgroup + ".Nick-check")) {
                        for (String uuid : getPermissions().getStringList("Groups." + checkgroup + ".Nicknames")) {
                            list.add(uuid);
                        }
                    }
                }
            }
        }
    	if(list.isEmpty()) {
    		list.add("null");
    	}
    	return list;
    }
    
    public ArrayList<String> getAllowedUUIDsForGroup(String group) {
    	ArrayList<String> list = new ArrayList<String>();
    	if (getPermissions().getConfigurationSection("Groups").getKeys(false).contains(group)) {
            for (String checkgroup : getPermissions().getConfigurationSection("Groups").getKeys(false)) {
                if (group.equalsIgnoreCase(checkgroup)) {
                    if (getPermissions().getBoolean("Groups." + checkgroup + ".UUID-check")) {
                        for (String uuid : getPermissions().getStringList("Groups." + checkgroup + ".UUIDs")) {
                            list.add(uuid);
                        }
                    }
                }
            }
        }
    	if(list.isEmpty()) {
    		list.add("null");
    	}
    	return list;
    }
    
}

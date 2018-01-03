package it.mycraft.toomanyperms;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Charsets;

import it.mycraft.toomanyperms.events.UnfairGroupsDetectedEvent;
import it.mycraft.toomanyperms.events.UnfairOpDetectedEvent;
import it.mycraft.toomanyperms.events.UnfairPermsDetectedEvent;
import ru.tehkode.permissions.bukkit.PermissionsEx;
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

    public void onEnable() {
    	
        instance = this;
        getYAML(new File(getDataFolder(), "messages"));
        getYAML(new File(getDataFolder(), "permissions"));
        getYAML(new File(getDataFolder(), "punishments"));
        getCommand("tmp").setExecutor(new CommandTMP());
        Bukkit.getConsoleSender().sendMessage(prefix("&aEnabling..."));
        Bukkit.getConsoleSender().sendMessage(prefix("&aEnabled!"));
        
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
            	if(Bukkit.getOnlinePlayers() != null) {
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        checkPlayer(p);
                    }
            	}
            }
        }, 30L, 30L);
    }
    
    public void reloadConfig(File resourcePath) {
      
      InputStream defConfigStream = getResource(resourcePath.getName()+".yml");
      if (defConfigStream == null) {
        return;
      }
      getConfig(resourcePath.getName()).setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }
    
    public FileConfiguration getConfig(String fileName) {
        return YamlConfiguration.loadConfiguration(new File(getDataFolder()+"/"+fileName+".yml"));
    }
    
    private void getYAML(File resourcePath) {
        if(!resourcePath.exists()) {
            createYAML(resourcePath.getName()+".yml", false);
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
        if(getConfig("messages").getBoolean("Messages.Use-Prefix")) {
        	String prefix = getConfig("messages").getString("Messages.Prefix");
        	return color(prefix + message);
        }
        else return color(message);
    }
    
    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    private void punishPlayer(Player target) {
    	if (getConfig("punishments").getBoolean("Punish")) {
            for (String commandline : getConfig("punishments").getStringList("Punish Commands")) {
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
        if (getConfig("permissions").getBoolean("Operators.UUID-check")) {
            if (!getConfig("permissions").getStringList("Operators.UUIDs").contains(target.getUniqueId().toString())) {
                if (target.isOp()) {
                    target.setOp(false);
                    punishPlayer(target);
                    UnfairOpDetectedEvent event = new UnfairOpDetectedEvent(target);
                    Bukkit.getPluginManager().callEvent(event);
                }
            }
        }
        if (getConfig("permissions").getBoolean("Operators.Nick-check")) {
            if (!getConfig("permissions").getStringList("Operators.Nicknames").contains(target.getName())) {
                if (target.isOp()) {
                    target.setOp(false);
                    punishPlayer(target);
                    UnfairOpDetectedEvent event = new UnfairOpDetectedEvent(target);
                    Bukkit.getPluginManager().callEvent(event);
                }
            }
        }
        /* Check for unfair permissions, just add in config as many permissions as you want! 100% lag-free! */
        for (String permission : getConfig("permissions").getConfigurationSection("Permissions").getKeys(false)) {
            if (target.hasPermission(permission.replaceAll("_", "."))) {
                if (getConfig("permissions").getBoolean("Permissions." + permission + ".Nick-check")) {
                    if (!getConfig("permissions").getStringList("Permissions." + permission + ".Nicknames").contains(target.getName())) {
                    	PermissionsEx.getUser(target).removePermission(permission.replaceAll("_", "."));
                        punishPlayer(target);
                        UnfairPermsDetectedEvent event = new UnfairPermsDetectedEvent(target, permission.replaceAll("_", "."));
                        Bukkit.getPluginManager().callEvent(event);
                    }
                }
                if (getConfig("permissions").getBoolean("Permissions." + permission + ".UUID-check")) {
                    if (!getConfig("permissions").getStringList("Permissions." + permission + ".UUIDs").contains(target.getUniqueId().toString())) {
                    	PermissionsEx.getUser(target).removePermission(permission.replaceAll("_", "."));
                    	punishPlayer(target);
                    	UnfairPermsDetectedEvent event = new UnfairPermsDetectedEvent(target, permission.replaceAll("_", "."));
                        Bukkit.getPluginManager().callEvent(event);
                    }
                }
            }
        }
        /* Check for unfair groups, just add in config as many groups as you prefer! Works also with inheritance groups! */
        for (String group : getConfig("permissions").getConfigurationSection("Groups").getKeys(false)) {
        	if(PermissionsEx.getUser(target).inGroup(group, true)) {
        		if (getConfig("permissions").getBoolean("Groups." + group + ".Nick-check")) {
        			if (!getConfig("permissions").getStringList("Groups." + group + ".Nicknames").contains(target.getName())) {
        				PermissionsEx.getUser(target).removeGroup(group);
                        punishPlayer(target);
                        UnfairGroupsDetectedEvent event = new UnfairGroupsDetectedEvent(target, group);
                        Bukkit.getPluginManager().callEvent(event);
                    }
        		}
        		if (getConfig("permissions").getBoolean("Groups." + group + ".UUID-check")) {
        			if (!getConfig("permissions").getStringList("Groups." + group + ".UUIDs").contains(target.getName())) {
        				PermissionsEx.getUser(target).removeGroup(group);
                        punishPlayer(target);
                        UnfairGroupsDetectedEvent event = new UnfairGroupsDetectedEvent(target, group);
                        Bukkit.getPluginManager().callEvent(event);
                    }
        		}
        	}
        }
    }
    
    public ArrayList<String> getAllowedUsersForOp() {
    	ArrayList<String> list = new ArrayList<String>();
        if (getConfig("permissions").getBoolean("Operators.Nick-check")) {
            for (String uuid : getConfig("permissions").getStringList("Operators.Nicknames")) {
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
        if (getConfig("permissions").getBoolean("Operators.UUID-check")) {
            for (String uuid : getConfig("permissions").getStringList("Operators.UUIDs")) {
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
    	if (getConfig("permissions").getConfigurationSection("Permissions").getKeys(false).contains(perm)) {
            for (String permission : getConfig("permissions").getConfigurationSection("Permissions").getKeys(false)) {
                if (perm.equalsIgnoreCase(permission)) {
                    if (getConfig("permissions").getBoolean("Permissions." + permission + ".Nick-check")) {
                        for (String nick : getConfig("permissions").getStringList("Permissions." + permission + ".Nicknames")) {
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
    	if (getConfig("permissions").getConfigurationSection("Permissions").getKeys(false).contains(perm)) {
            for (String permission : getConfig("permissions").getConfigurationSection("Permissions").getKeys(false)) {
                if (perm.equalsIgnoreCase(permission)) {
                    if (getConfig("permissions").getBoolean("Permissions." + permission + ".UUID-check")) {
                        for (String uuid : getConfig("permissions").getStringList("Permissions." + permission + ".UUIDs")) {
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
    	if (getConfig("permissions").getConfigurationSection("Groups").getKeys(false).contains(group)) {
            for (String checkgroup : getConfig("permissions").getConfigurationSection("Groups").getKeys(false)) {
                if (group.equalsIgnoreCase(checkgroup)) {
                    if (getConfig("permissions").getBoolean("Groups." + checkgroup + ".Nick-check")) {
                        for (String uuid : getConfig("permissions").getStringList("Groups." + checkgroup + ".Nicknames")) {
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
    	if (getConfig("permissions").getConfigurationSection("Groups").getKeys(false).contains(group)) {
            for (String checkgroup : getConfig("permissions").getConfigurationSection("Groups").getKeys(false)) {
                if (group.equalsIgnoreCase(checkgroup)) {
                    if (getConfig("permissions").getBoolean("Groups." + checkgroup + ".UUID-check")) {
                        for (String uuid : getConfig("permissions").getStringList("Groups." + checkgroup + ".UUIDs")) {
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

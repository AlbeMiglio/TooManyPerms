package it.mycraft.toomanyperms;

import com.google.common.base.Charsets;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;

public class ConfigManager {

    @Getter
    private FileConfiguration config;

    @Getter
    private FileConfiguration messages;

    @Getter
    private FileConfiguration permissions;

    @Getter
    private FileConfiguration punishments;

    private TooManyPerms main;

    public ConfigManager(TooManyPerms main) {
        this.main = main;
        Arrays.asList("config.yml", "messages.yml", "permissions.yml", "punishments.yml").forEach((file) -> {
            this.getYAML(new File(this.main.getDataFolder(), file));
        });
    }

    private void getYAML(File resourcePath) {
        if (!resourcePath.exists()) {
            createYAML(resourcePath.getName(), false);
        }
    }

    public void reloadConfiguration() {
        Arrays.asList("config.yml", "messages.yml", "permissions.yml", "punishments.yml").forEach((file) -> {
            this.loadFile(file, YamlConfiguration.loadConfiguration(new File(this.main.getDataFolder(), file)));
            InputStream stream = this.main.getResource(file);
            if (stream != null) {
                this.setDefaults(file, stream);
            }
        });

    }

    public void loadFile(String s, FileConfiguration fileConfiguration) {
        switch (s) {
            default:
                break;
            case "config.yml":
                this.config = fileConfiguration;
                break;
            case "messages.yml":
                this.messages = fileConfiguration;
                break;
            case "permissions.yml":
                this.permissions = fileConfiguration;
                break;
            case "punishments.yml":
                this.punishments = fileConfiguration;
                break;
        }
    }

    public void setDefaults(String s, InputStream stream) {
        switch (s) {
            default:
                break;
            case "config.yml":
                this.config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(stream, Charsets.UTF_8)));
                break;
            case "messages.yml":
                this.messages.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(stream, Charsets.UTF_8)));
                break;
            case "permissions.yml":
                this.permissions.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(stream, Charsets.UTF_8)));
                break;
            case "punishments.yml":
                this.punishments.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(stream, Charsets.UTF_8)));
                break;
        }
    }

    private void createYAML(String resourcePath, boolean replace) {
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = this.main.getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " +
                        (new File(this.main.getDataFolder(), resourcePath+".yml")));
            } else {
                File outFile = new File(this.main.getDataFolder(), resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(this.main.getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));
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
}

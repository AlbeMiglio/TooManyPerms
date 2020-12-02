package it.mycraft.toomanyperms;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;

public class PermissionsManager {

    private TooManyPerms main;

    @Getter
    private FileConfiguration permissions;

    public PermissionsManager(TooManyPerms main) {
        this.main = main;
        this.permissions = this.main.getPermissions();
    }

    public ArrayList<String> getAllowedUsersForOp() {
        return (ArrayList<String>) this.getPermissions().getStringList("Operators.Nicknames");
    }

    public ArrayList<String> getAllowedUUIDsForOp() {
        return (ArrayList<String>) this.getPermissions().getStringList("Operators.UUIDs");
    }

    public ArrayList<String> getAllowedUsersForPerm(String perm) {
        return (ArrayList<String>) this.getPermissions().getStringList("Permissions." + perm + ".Nicknames");
    }

    public ArrayList<String> getAllowedUUIDsForPerm(String perm) {
        return (ArrayList<String>) this.getPermissions().getStringList("Permissions." + perm + ".UUIDs");
    }

    public ArrayList<String> getAllowedUsersForGroup(String group) {
        return (ArrayList<String>) this.getPermissions().getStringList("Groups." + group + ".Nicknames");
    }

    public ArrayList<String> getAllowedUUIDsForGroup(String group) {
        return (ArrayList<String>) this.getPermissions().getStringList("Groups." + group + ".UUIDs");
    }

    public ArrayList<String> getExcludedNicknames() {
        return (ArrayList<String>) this.getPermissions().getStringList("Excluded-Nicknames");
    }
}

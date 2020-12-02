package it.mycraft.toomanyperms;

import it.mycraft.toomanyperms.events.UnfairGroupsDetectedEvent;
import it.mycraft.toomanyperms.events.UnfairOpDetectedEvent;
import it.mycraft.toomanyperms.events.UnfairPermsDetectedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CheckTask extends BukkitRunnable {

    private TooManyPerms main;

    public CheckTask(TooManyPerms main) {
        this.main = main;
    }

    @Override
    public void run() {
        if (Bukkit.getServer().getOnlinePlayers() != null) {
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                checkPlayer(p);
            }
        }
    }

    private void checkPlayer(Player target) {
        if (this.main.getPermissionsManager().getExcludedNicknames().contains(target.getName())) {
            return;
        }
        this.checkOp(target);
        this.checkPerms(target);
        this.checkGroups(target);
    }

    private boolean checkOp(Player target) {
        if (!this.main.getConfig().getBoolean("Checks.OP")) {
            return false;
        }
        if ((this.main.getPermissions().getBoolean("Operators.Nick-check") && target.isOp()
                && !this.main.getPermissionsManager().getAllowedUsersForOp().contains(target.getName()))) {
            target.setOp(false);
            this.main.punishPlayer(target);
            UnfairOpDetectedEvent unfairOpDetectedEvent = new UnfairOpDetectedEvent(target);
            Bukkit.getPluginManager().callEvent(unfairOpDetectedEvent);
            return true;
        }
        if ((this.main.getPermissions().getBoolean("Operators.UUID-check") && target.isOp()
                && !this.main.getPermissionsManager().getAllowedUUIDsForOp().contains(target.getUniqueId().toString()))) {
            target.setOp(false);
            this.main.punishPlayer(target);
            UnfairOpDetectedEvent event = new UnfairOpDetectedEvent(target);
            Bukkit.getPluginManager().callEvent(event);
            return true;
        }
        return false;
    }

    private boolean checkPerms(Player target) {
        if (!this.main.getConfig().getBoolean("Checks.Permissions")) {
            return false;
        }
        for (String permission : this.main.getPermissions().getConfigurationSection("Permissions").getKeys(true)) {
            if (!target.hasPermission(permission.replaceAll("[_]", "."))) {
                continue;
            }
            if (this.main.getPermissions().getBoolean("Permissions." + permission + ".Nick-check")
                    && !this.main.getPermissionsManager().getAllowedUsersForPerm(permission)
                    .contains(target.getName())) {
                this.main.removePermission(target.getName(), permission.replaceAll("[_]", "."));
                this.main.punishPlayer(target);
                UnfairPermsDetectedEvent event = new UnfairPermsDetectedEvent(target, permission.replaceAll("[_]", "."));
                Bukkit.getPluginManager().callEvent(event);
                return true;
            }
            if (this.main.getPermissions().getBoolean("Permissions." + permission + ".UUID-check")
                    && !this.main.getPermissionsManager().getAllowedUUIDsForPerm(permission)
                    .contains(target.getUniqueId().toString())) {
                this.main.removePermission(target.getName(), permission.replaceAll("[_]", "."));
                this.main.punishPlayer(target);
                UnfairPermsDetectedEvent event = new UnfairPermsDetectedEvent(target, permission.replaceAll("[_]", "."));
                Bukkit.getPluginManager().callEvent(event);
                return true;
            }
        }
        return false;
    }

    private boolean checkGroups(Player target) {
        if (!this.main.getConfig().getBoolean("Checks.Groups")) {
            return false;
        }
        for (String group : this.main.getPermissions().getConfigurationSection("Groups").getKeys(false)) {
            if (!this.main.getVaultPermission().playerInGroup(target.getWorld().getName(), target, group)) {
                continue;
            }
            if (this.main.getPermissions().getBoolean("Groups." + group + ".Nick-check")
                    && !this.main.getPermissionsManager().getAllowedUsersForGroup(group)
                    .contains(target.getName())) {
                this.main.removeGroup(target.getName(), group);
                this.main.punishPlayer(target);
                UnfairGroupsDetectedEvent event = new UnfairGroupsDetectedEvent(target, group);
                Bukkit.getPluginManager().callEvent(event);
                return true;
            }
            if (this.main.getPermissions().getBoolean("Groups." + group + ".UUID-check")
                    && !this.main.getPermissionsManager().getAllowedUUIDsForGroup(group)
                    .contains(target.getUniqueId().toString())) {
                this.main.removeGroup(target.getName(), group);
                this.main.punishPlayer(target);
                UnfairGroupsDetectedEvent event = new UnfairGroupsDetectedEvent(target, group);
                Bukkit.getPluginManager().callEvent(event);
                return true;
            }
        }
        return false;
    }
}


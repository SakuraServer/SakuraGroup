/**
 * SakuraGroup - Package: net.syamn.sakuragroup.manager Created: 2012/10/16
 * 19:52:05
 */
package net.syamn.sakuragroup.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.syamn.sakuragroup.Group;
import net.syamn.sakuragroup.SakuraGroup;
import net.syamn.sakuragroup.utils.plugin.Actions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * PEXManager (PEXManager.java)
 * 
 * @author syam(syamn)
 */
public class PEXManager {
    // Logger
    private static final Logger log = SakuraGroup.log;
    private static final String logPrefix = SakuraGroup.logPrefix;
    private static final String msgPrefix = SakuraGroup.msgPrefix;

    // PEX
    private PermissionsEx pex = null;
    private PermissionManager pm = null;

    private List<String> pexgroups = new ArrayList<String>(); // exists pex
                                                              // oldGroups
    private Map<String, Group> groups = new HashMap<String, Group>();

    private final SakuraGroup plugin;

    public PEXManager(final SakuraGroup plugin) {
        this.plugin = plugin;

        // setup pex
        if (!setupPEX()) {
            return;
        }

        loadGroups();
    }

    /**
     * グループを変更する
     * 
     * @param name
     * @param groupName
     * @return
     */
    public boolean changeGroup(String name, String groupName, CommandSender sender) {
        PermissionGroup group = pm.getGroup(groupName);
        PermissionGroup[] setGroup = { group };
        PermissionsEx.getUser(name).setGroups(setGroup);

        if (sender != null) {
            Actions.message(sender, "&6" + name + " &aを&6" + group.getName() + "&aに変更しました！");
        }

        return true;
    }

    public Set<String> getPlayersByGroup(String groupName) {
        Set<String> names = new HashSet<String>();
        for (PermissionUser user : getUsersByGroup(groupName)) {
            names.add(user.getName());
        }
        return names;
    }

    public PermissionUser[] getUsersByGroup(String groupName) {
        PermissionGroup group = pm.getGroup(groupName);
        return group.getUsers();
    }

    /**
     * グループリストを再構築する
     */
    public void loadGroups() {
        // Building all exists
        pexgroups.clear();
        for (PermissionGroup group : pm.getGroups()) {
            pexgroups.add(group.getName());
        }

        // Check default group
        if (!pexgroups.contains(plugin.getConfigs().getDefGroup())) {
            log.severe(logPrefix + "Default group " + plugin.getConfigs().getDefGroup() + " is NOT found! Disabling plugin..");
            plugin.getPluginLoader().disablePlugin(plugin);
            return;
        }

        // Building all available groups
        groups.clear();
        for (String name : plugin.getConfigs().getGroups()) {
            // check exists
            if (!pexgroups.contains(name)) {
                log.warning(logPrefix + "Group NOT exist! Disabled group: " + name);
                continue;
            }

            // get configure
            Double cost = plugin.getConfigs().getGroupCost(name);
            Double keepcost = plugin.getConfigs().getGroupKeepCost(name);
            String color = plugin.getConfigs().getGroupColor(name);

            groups.put(name, new Group(name, cost, keepcost, color));
        }
    }

    public Set<String> getAvailables() {
        return this.groups.keySet();
    }

    public PermissionGroup getPEXgroup(String groupName) {
        return pm.getGroup(groupName);
    }

    public Group getGroup(String groupName) {
        for (String name : getAvailables()) {
            if (name.equalsIgnoreCase(groupName)) {
                return getGroupExact(name);
            }
        }
        return null;
    }

    public Group getGroupExact(String groupName) {
        return groups.get(groupName);
    }

    public String getCurrentGroup(Player player) {
        PermissionUser user = PermissionsEx.getUser(player);
        String[] groups = user.getGroupsNames();
        if (groups.length == 1) {
            return groups[0];
        } else {
            return null; // throw NPE
        }
    }

    /**
     * PermissionsExを返す
     * 
     * @return PermissionsEx
     */
    public PermissionsEx getPEX() {
        return pex;
    }

    /**
     * PermissionsManagerを返す
     * 
     * @return PermissionsManager
     */
    public PermissionManager getPM() {
        return pm;
    }

    /**
     * PermissionsExセットアップ
     * 
     * @return
     */
    private boolean setupPEX() {
        Plugin pexTest = plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
        if (pexTest == null) pexTest = plugin.getServer().getPluginManager().getPlugin("permissionsex");
        if (pexTest == null) return false;
        try {
            pex = (PermissionsEx) pexTest;
            pm = PermissionsEx.getPermissionManager();
        } catch (Exception ex) {
            log.warning(logPrefix + "PermissionsEx plugin NOT found. Disabling plugin..");
            ex.printStackTrace();
            plugin.getPluginLoader().disablePlugin(plugin);
            return false;
        }

        // Success
        log.info(logPrefix + "Hooked to PermissionsEx plugin!");
        return true;
    }
}

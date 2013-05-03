/**
 * SakuraGroup - Package: net.syamn.sakuragroup.manager Created: 2012/10/18
 * 20:15:32
 */
package net.syamn.sakuragroup.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.syamn.sakuragroup.Group;
import net.syamn.sakuragroup.SakuraGroup;

import org.bukkit.entity.Player;

/**
 * SignManager (SignManager.java)
 * 
 * @author syam(syamn)
 */
public class SignManager {
    // Logger
    private static final Logger log = SakuraGroup.log;
    private static final String logPrefix = SakuraGroup.logPrefix;
    private static final String msgPrefix = SakuraGroup.msgPrefix;

    private final SakuraGroup plugin;

    public SignManager(final SakuraGroup plugin) {
        this.plugin = plugin;
    }

    private static Map<String, Group> selectedGroup = new HashMap<String, Group>();

    public static void setSelectedGroup(final Player player, final Group group) {
        selectedGroup.put(player.getName(), group);
    }

    public static Group getSelectedGroup(final Player player) {
        return (player == null) ? null : selectedGroup.get(player.getName());
    }

    public static void clearSelectedGroup() {
        selectedGroup.clear();
    }
}

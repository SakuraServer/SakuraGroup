/**
 * SakuraGroup - Package: net.syamn.sakuragroup.command Created: 2012/10/16
 * 20:47:31
 */
package net.syamn.sakuragroup.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.syamn.sakuragroup.manager.PEXManager;
import net.syamn.sakuragroup.permission.Perms;
import net.syamn.sakuragroup.utils.plugin.Actions;
import net.syamn.sakuragroup.utils.plugin.Util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * ListCommand (ListCommand.java)
 * 
 * @author syam(syamn)
 */
public class ListCommand extends BaseCommand {
    public ListCommand() {
        bePlayer = false;
        name = "list";
        argLength = 0;
        usage = "<- show group/user list";
    }

    @Override
    public void execute() {
        final String senderName = (sender instanceof Player) ? sender.getName() : null;

        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                final PEXManager mgr = plugin.getPEXmgr();
                List<String> msg = new ArrayList<String>();

                msg.add(msgPrefix + "&a有効なグループリスト");
                for (String name : mgr.getAvailables()) {
                    int limit = plugin.getConfigs().getGroupLimit(name);
                    String limitStr = (limit > 0) ? " &7(Max: " + limit + "人)" : "";

                    Set<String> names = mgr.getPlayersByGroup(name);
                    msg.add("&b ** &e" + name + "&7: &6" + names.size() + "人" + limitStr);
                    if (names.size() > 0) {
                        msg.add(Util.join(names, "&7,&f "));
                    } else {
                        msg.add("&7(このグループに所属している人はいません)");
                    }
                }

                // send messages
                CommandSender s = (senderName == null) ? Bukkit.getConsoleSender() : Bukkit.getPlayer(senderName);
                if (s != null) {
                    for (String line : msg) {
                        Actions.message(s, line);
                    }
                }
            }
        }, 0L);
    }

    @Override
    public boolean permission() {
        return Perms.LIST.has(sender);
    }
}
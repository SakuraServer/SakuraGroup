/**
 * SakuraGroup - Package: syam.sakuragroup.command
 * Created: 2012/10/16 20:47:31
 */
package syam.sakuragroup.command;

import java.util.List;

import org.bukkit.entity.Player;

import syam.sakuragroup.manager.PEXManager;
import syam.sakuragroup.manager.PlayerManager;
import syam.sakuragroup.permission.Perms;
import syam.sakuragroup.util.Actions;
import syam.sakuragroup.util.Util;

/**
 * ListCommand (ListCommand.java)
 * @author syam(syamn)
 */
public class ListCommand extends BaseCommand {
	public ListCommand(){
		bePlayer = false;
		name = "list";
		argLength = 0;
		usage = "<- show group/user list";
	}

	@Override
	public void execute() {
		PEXManager mgr = plugin.getPEXmgr();
		List<String> availables = mgr.getAvailableGroups();

		Actions.message(sender, msgPrefix+ "&a有効なグループリスト");
		for (String groupName : availables){
			List<String> names = mgr.getPlayersByGroup(groupName);
			Actions.message(sender, "&b ** &e" + groupName + "&7: &6" + names.size() + "人");
			Actions.message(sender, Util.join(names, "&7,&f "));
		}
	}

	@Override
	public boolean permission() {
		return Perms.LIST.has(sender);
	}
}

/**
 * SakuraGroup - Package: syam.sakuragroup.command
 * Created: 2012/10/25 0:41:03
 */
package syam.sakuragroup.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import syam.sakuragroup.Group;
import syam.sakuragroup.SakuraGroup;
import syam.sakuragroup.database.Database;
import syam.sakuragroup.exception.CommandException;
import syam.sakuragroup.manager.PEXManager;
import syam.sakuragroup.permission.Perms;
import syam.sakuragroup.util.Actions;
import syam.sakuragroup.util.Util;

/**
 * ChangeAllCommand (ChangeAllCommand.java)
 * @author syam(syamn)
 */
public class ChangeAllCommand extends BaseCommand{
	public ChangeAllCommand(){
		bePlayer = true;
		name = "changeall";
		argLength = 2;
		usage = "[from] [to] <- change your current group";
	}
	private PEXManager mgr;

	@Override
	public void execute() throws CommandException {
		this.mgr = plugin.getPEXmgr();

		/* Build From */
		String froms = null;
		Set<String> names = new HashSet<String>();

		if (args.get(0).equalsIgnoreCase("*")){
			for (String gn : mgr.getAvailables()){
				names.addAll(mgr.getPlayersByGroup(gn));
			}
		}else{
			Group fromg = mgr.getGroup(args.get(0));
			froms = (fromg != null) ? fromg.getName() : null;
			if (froms == null){
				throw new CommandException("変更元のグループが見つかりません！");
			}

			names = mgr.getPlayersByGroup(froms); // target
		}

		/* Build To */
		String tos = null;
		if (args.get(1).equalsIgnoreCase("default")){
			tos = plugin.getConfigs().getDefGroup();
		}else{
			Group tog = mgr.getGroup(args.get(1));
			tos = (tog != null) ? tog.getName() : null;
		}

		if (tos == null){
			throw new CommandException("変更先のグループが見つかりません！");
		}


		/* Update Database at once */
		Database db = SakuraGroup.getDatabases();
		if (froms != null){
			db.write("UPDATE " + db.getTablePrefix() + "users SET `group` = ?, `lastpaid` = 0 WHERE `group` = ?", tos, froms);
		}else{
			db.write("UPDATE " + db.getTablePrefix() + "users SET `group` = ?, `lastpaid` = 0", tos);
		}

		/* Change Group */
		for (String name : names){
			// Change Group
			mgr.changeGroup(name, tos, null);

			// Messaging
			Player player = Bukkit.getPlayer(name);
			if (player != null && player.isOnline()){
				Actions.message(player, msgPrefix + "&aあなたのグループは&6 " + tos + " &aに変更されました！");
			}
		}

		if (froms != null){
			Actions.broadcastMessage(msgPrefix + "&6" + froms + "&aグループメンバーは&6" + tos + "&aに変更されました！");
		}else{
			Actions.broadcastMessage(msgPrefix + "&aすべての特別グループメンバーは&6" + tos + "&aに変更されました！");
		}

		Actions.message(sender, "&a" + names.size() + "人のグループを変更しました！");
	}

	@Override
	public boolean permission() {
		return Perms.CHANGE_ALL.has(sender);
	}
}

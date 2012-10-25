/**
 * SakuraGroup - Package: syam.sakuragroup.command
 * Created: 2012/10/25 12:05:39
 */
package syam.sakuragroup.command;

import syam.sakuragroup.Group;
import syam.sakuragroup.SakuraGroup;
import syam.sakuragroup.database.Database;
import syam.sakuragroup.exception.CommandException;
import syam.sakuragroup.manager.PEXManager;
import syam.sakuragroup.permission.Perms;
import syam.sakuragroup.util.Actions;
import syam.sakuragroup.util.Util;

/**
 * PayCommand (PayCommand.java)
 * @author syam(syamn)
 */
public class PayCommand extends BaseCommand {
	public PayCommand(){
		bePlayer = true;
		name = "pay";
		argLength = 0;
		usage = "<- pay keep cost";
	}
	private PEXManager mgr;

	@Override
	public void execute() throws CommandException {
		mgr = plugin.getPEXmgr();
		String currentGroup = plugin.getPEXmgr().getCurrentGroup(player);
		if (plugin.getConfigs().getDefGroup().equalsIgnoreCase(currentGroup)){
			throw new CommandException("&cあなたはデフォルトグループメンバーです！");
		}
		Group group = mgr.getGroup(currentGroup);
		if (group == null){
			throw new CommandException("&cあなたは特別グループに所属していません！");
		}

		// Pay cost
		boolean paid = false;
		double cost = group.getKeepCost();
		if (plugin.getConfigs().getUseVault() && cost > 0 && !Perms.FREE_PAY.has(player)){
			paid = Actions.takeMoney(player.getName(), cost);
			if (!paid){
				throw new CommandException("&cお金が足りません！ " + Actions.getCurrencyString(cost) + "必要です！");
			}
		}

		// Update!
		Database db = SakuraGroup.getDatabases();
		db.write("UPDATE " + db.getTablePrefix() + "users SET `lastpaid` = ? WHERE `player_name` = ?", Util.getCurrentUnixSec().intValue(), player.getName());

		String msg = msgPrefix + "&aグループの維持費を支払いました！";
		if (paid) msg = msg + " &c(-" + Actions.getCurrencyString(cost) + ")";
		Actions.message(player, msg);
	}

	@Override
	public boolean permission() {
		return Perms.PAY.has(sender);
	}
}

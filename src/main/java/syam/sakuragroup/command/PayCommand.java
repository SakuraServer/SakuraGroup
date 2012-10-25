/**
 * SakuraGroup - Package: syam.sakuragroup.command
 * Created: 2012/10/25 12:05:39
 */
package syam.sakuragroup.command;

import java.util.List;

import syam.sakuragroup.Group;
import syam.sakuragroup.SakuraGroup;
import syam.sakuragroup.command.queue.Queueable;
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
public class PayCommand extends BaseCommand implements Queueable{
	public PayCommand(){
		bePlayer = true;
		name = "pay";
		argLength = 0;
		usage = "<- pay keep cost";
	}
	private PEXManager mgr;
	private Group group = null;
	private double cost = 0.0D;

	@Override
	public void execute() throws CommandException {
		mgr = plugin.getPEXmgr();
		String currentGroup = plugin.getPEXmgr().getCurrentGroup(player);
		if (plugin.getConfigs().getDefGroup().equalsIgnoreCase(currentGroup)){
			throw new CommandException("&cあなたはデフォルトグループメンバーです！");
		}
		group = mgr.getGroup(currentGroup);
		if (group == null){
			throw new CommandException("&cあなたは特別グループに所属していません！");
		}

		cost = group.getKeepCost();

		// Put queue
		plugin.getQueue().addQueue(sender, this, args, 15);
		Actions.message(sender, "&dグループ  " + group.getColor() + group.getName() + " &dの更新料を支払おうとしています！");
		if (plugin.getConfigs().getUseVault() && cost > 0 && !Perms.FREE_CHANGE.has(player)){
			Actions.message(sender, "&d更新費用として &6" + Actions.getCurrencyString(cost) + " &dが必要です！");
		}
		Actions.message(sender, "&d支払った日時から起算して1週間グループを維持できます！");
		Actions.message(sender, "&d続行するには &a/group confirm &dコマンドを入力してください！");
		Actions.message(sender, "&a/group confirm &dコマンドは15秒間のみ有効です。");
	}

	@Override
	public void executeQueue(List<String> qArgs){
		// Check again
		if (group == null){
			Actions.message(sender, "&cあなたは特別グループに所属していません！");
			return;
		}

		// Pay cost
		boolean paid = false;
		if (plugin.getConfigs().getUseVault() && cost > 0 && !Perms.FREE_PAY.has(player)){
			paid = Actions.takeMoney(player.getName(), cost);
			if (!paid){
				Actions.message(sender, "&cお金が足りません！ " + Actions.getCurrencyString(cost) + "必要です！");
				return;
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

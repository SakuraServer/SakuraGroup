/**
 * SakuraGroup - Package: syam.sakuragroup.command
 * Created: 2012/10/17 7:41:13
 */
package syam.sakuragroup.command;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import syam.sakuragroup.Group;
import syam.sakuragroup.SakuraGroup;
import syam.sakuragroup.command.queue.Queueable;
import syam.sakuragroup.database.Database;
import syam.sakuragroup.exception.CommandException;
import syam.sakuragroup.manager.PEXManager;
import syam.sakuragroup.manager.SignManager;
import syam.sakuragroup.permission.Perms;
import syam.sakuragroup.util.Actions;
import syam.sakuragroup.util.Util;

/**
 * ChangeCommand (ChangeCommand.java)
 * @author syam(syamn)
 */
public class ChangeCommand extends BaseCommand implements Queueable{
	public ChangeCommand(){
		bePlayer = true;
		name = "change";
		argLength = 0;
		usage = "<- change your current group";
	}
	private PEXManager mgr;

	// グループ変更時の一時変数
	private Group group = null;
	private double cost = 0.0D;
	int status = 0;
	int changed = 0;

	@Override
	public void execute() throws CommandException {
		this.mgr = plugin.getPEXmgr();

		// 引数が1つ グループ指定
		if (args.size() >= 1){
			if (!Perms.CHANGE_CMD.has(sender)){
				throw new CommandException("コマンドから直接グループを変更する権限がありません！");
			}
			update(args.get(0));
		}
		// 引数なし 選択チェック
		else{
			if (!Perms.CHANGE_SIGN.has(sender)){
				throw new CommandException("変更するグループ名を指定してください！");
			}
			Group group = SignManager.getSelectedGroup(player);
			if (group == null){
				throw new CommandException("あなたはまだグループ看板を選択していません！");
			}
			update(group);
		}
	}

	private void update(final String newGroupName) throws CommandException{
		// 新グループ確定
		Group group = null;
		for (String name : mgr.getAvailables()){
			if (name.equalsIgnoreCase(args.get(0))){
				group = mgr.getGroup(name); break;
			}
		}
		if (group == null){
			throw new CommandException("指定したグループは存在しません！");
		}

		update(group);
	}
	private void update(final Group group) throws CommandException{
		// prepare update
		this.group = group;

		//int playerID = -1;


		// Get Database
		Database db = SakuraGroup.getDatabases();
		HashMap<Integer, ArrayList<String>> result =
				db.read("SELECT `player_id`, `group`, `status`, `changed`, `lastchange` FROM " + db.getTablePrefix() + "users WHERE `player_name` = ?", player.getName());
		if (result.size() > 0){
			// 既にDB登録済み チェック
			ArrayList<String> record = result.get(1);

			//playerID = Integer.valueOf(record.get(0));
			String currentGroup = record.get(1);
			status = Integer.valueOf(record.get(2));
			changed = Integer.valueOf(record.get(3));
			Long changedTime = Long.valueOf(record.get(4));

			// グループチェック
			if (group.getName().equalsIgnoreCase(currentGroup)){
				throw new CommandException("既に同じグループに所属しています！");
			}

			// ステータスチェック
			if (status != 0){
				throw new CommandException("あなたはグループの変更を禁止されています！");
			}

			// 時間チェック
			Calendar time = Calendar.getInstance();
			time.setTime(Util.getDateByUnixTime(changedTime));
			time.add(plugin.getConfigs().getMeasure(), plugin.getConfigs().getTime());
			if (!time.before(Calendar.getInstance())){
				throw new CommandException("あなたはまだグループの変更可能時間に達していません！");
			}
		}

		//ArrayList<String> strArgs = new ArrayList<String>() {{add (group.getName());}};

		// Put variables
		this.cost = plugin.getConfigs().getGroupCost(group.getName());
		if (plugin.getConfigs().getUseVault() && cost < 0){
			log.warning(logPrefix + "Group " + group.getName() + " cost config NOT exist or negative value! Change to 0.");
			cost = 0.0D;
		}

		// Put Queue, messaging
		plugin.getQueue().addQueue(sender, this, args, 15);
		Actions.message(sender, "&dグループ  " + group.getColor() + group.getName() + " &dに変更しようとしています！");
		if (plugin.getConfigs().getUseVault() && cost > 0 && !Perms.FREE_CHANGE.has(player)){
			Actions.message(sender, "&d変更費用として &6" + Actions.getCurrencyString(cost) + " &dが必要です！");
		}
		Actions.message(sender, "&dこの操作は取り消しすることができません。本当に続行しますか？");
		Actions.message(sender, "&d続行するには &a/group confirm &dコマンドを入力してください！");
		Actions.message(sender, "&a/group confirm &dコマンドは15秒間のみ有効です。");
	}

	@Override
	public void executeQueue(List<String> qArgs){
		// check null
		if (this.group == null){
			Actions.message(player, "&cこのグループは存在しません！");
			return;
		}

		// Pay cost
		boolean paid = false;
		if (plugin.getConfigs().getUseVault() && cost > 0 && !Perms.FREE_CHANGE.has(player)){
			paid = Actions.takeMoney(player.getName(), cost);
			if (!paid){
				Actions.message(player, "&cお金が足りません！ " + Actions.getCurrencyString(cost) + "必要です！");
				return;
			}
		}

		// Update!
		Database db = SakuraGroup.getDatabases();
		db.write("REPLACE INTO " + db.getTablePrefix() + "users (`player_name`, `group`, `status`, `changed`, `lastchange`) " +
				"VALUES (?, ?, ?, ?, ?)", player.getName(), group.getName(), status, changed + 1,  Util.getCurrentUnixSec().intValue());
		// Change group!
		mgr.changeGroup(player.getName(), group.getName(), null);

		// messaging
		Actions.broadcastMessage(msgPrefix+ "&6" + player.getName() + "&aさんが&f" + group.getColor() + group.getName() + "&aグループに所属しました！");

		String msg = msgPrefix + "&aあなたのグループを変更しました！";
		if (paid) msg = msg + " &c(-" + Actions.getCurrencyString(cost) + ")";
		Actions.message(player, msg);
	}

	@Override
	public boolean permission() {
		return (Perms.CHANGE_CMD.has(sender) || Perms.CHANGE_SIGN.has(sender));
	}
}

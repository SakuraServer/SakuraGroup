/**
 * SakuraGroup - Package: net.syamn.sakuragroup.command
 * Created: 2012/10/26 6:46:37
 */
package net.syamn.sakuragroup.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.syamn.sakuragroup.Group;
import net.syamn.sakuragroup.SakuraGroup;
import net.syamn.sakuragroup.database.Database;
import net.syamn.sakuragroup.permission.Perms;
import net.syamn.sakuragroup.utils.plugin.Actions;
import net.syamn.sakuragroup.utils.plugin.Util;
import net.syamn.utils.exception.CommandException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 * InfoCommand (InfoCommand.java)
 * @author syam(syamn)
 */
public class InfoCommand extends BaseCommand{
	public InfoCommand(){
		bePlayer = false;
		name = "info";
		argLength = 0;
		usage = "[player] <- check player information";
	}

	@Override
	public void execute() throws CommandException {
		// self
		if (args.size() == 0){
			// check console
			if (!(sender instanceof Player)){
				throw new CommandException("&c情報を表示するユーザ名を入力してください");
			}

			// check permission
			if (!Perms.INFO_SELF.has(sender)){
				throw new CommandException("&cあなたはこのコマンドを使う権限がありません");
			}
		}
		// other
		else{
			// check permission
			if (!Perms.INFO_OTHER.has(sender)){
				throw new CommandException("&cあなたは他人の情報を見る権限がありません");
			}
		}

		final String senderName = (sender instanceof Player) ? sender.getName() : null;
		final boolean other = (args.size() > 0);
		final String name = (other) ? args.get(0).trim() : player.getName();

		// running another thread
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run(){
				List<String> lines = buildStrings(name, other);

				// send messages
				CommandSender s = (senderName == null) ? Bukkit.getConsoleSender() : Bukkit.getPlayer(senderName);
				if (s != null){
					for (String line : lines){
						Actions.message(s, line);
					}
				}
			}
		}, 0L);
	}

	private List<String> buildStrings(final String name, final boolean other){
		List<String> l = new ArrayList<String>();
		l.clear();

		// get record
		Database db = SakuraGroup.getDatabases();
		HashMap<Integer, ArrayList<String>> result =
				db.read("SELECT `player_id`, `group`, `status`, `changed`, `lastchange`, `lastpaid` FROM " + db.getTablePrefix() + "users WHERE `player_name` = ?", name);

		// not found
		if (result.size() == 0){
			l.add("&cグループ情報が未登録のプレイヤーです！");
			return l;
		}else if (result.size() > 1){
			l.add("&cグループ登録情報が不正です！");
			return l;
		}

		ArrayList<String> record = result.get(1);

		//int player_id = Integer.valueOf(record.get(0));
		String player_id = record.get(0);
		String group_name = record.get(1);
		int status = Integer.valueOf(record.get(2));
		int changed = Integer.valueOf(record.get(3));
		Long lastchange = Long.valueOf(record.get(4));
		Long lastpaid = Long.valueOf(record.get(5));

		final int WEEK = 60 * 60 * 24 * 7; // 1週間: 604800秒
		Long expired = lastpaid + WEEK;

		// get group
		boolean def = group_name.equalsIgnoreCase(plugin.getConfigs().getDefGroup());
		Group group = null;
		if (!def){
			group = plugin.getPEXmgr().getGroup(group_name);
			if (group == null){
				l.add("&c所属グループ情報 " + group_name + " を読み込めませんでした！");
				return l;
			}
		}
		String joined = (def) ? "&a" + group_name : group.getColor() + group.getName();

		final String none = "&7(なし)";

		// header
		l.add(msgPrefix+ "&aプレイヤー情報");
		if (other)
			l.add("&aプレイヤー: &6" + name + " &7(ID:" + player_id + ")");

		// build information
		l.add("&e所属グループ: &a" + joined + " &7(" + changed + "回変更)");

		l.add("&e参加: &a" + ((def) ? none : Util.getDispTimeByUnixTime(lastchange)) +
				" &e更新: &a" + ((def) ? none : Util.getDispTimeByUnixTime(lastpaid)));
		l.add("&e期限: &a" + ((def) ? none : Util.getDispTimeByUnixTime(expired)) +
				" &e残り時間: &a" + ((def) ? none : Util.getDiffString(Util.getCurrentUnixSec(), expired)));

		return l;
	}

	@Override
	public boolean permission() {
		return (Perms.INFO_SELF.has(sender) || Perms.INFO_OTHER.has(sender));
	}
}

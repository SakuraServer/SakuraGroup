/**
 * SakuraGroup - Package: syam.sakuragroup.task
 * Created: 2012/10/25 13:02:14
 */
package syam.sakuragroup.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import syam.sakuragroup.Group;
import syam.sakuragroup.SakuraGroup;
import syam.sakuragroup.database.Database;
import syam.sakuragroup.manager.PEXManager;
import syam.sakuragroup.util.Actions;
import syam.sakuragroup.util.Util;

/**
 * ExpiredCheck (ExpiredCheck.java)
 * @author syam(syamn)
 */
public class ExpiredCheck implements Runnable{
	// Logger
	public static final Logger log = SakuraGroup.log;
	private static final String logPrefix = SakuraGroup.logPrefix;
	private static final String msgPrefix = SakuraGroup.msgPrefix;

	private static boolean running = false;
	private final SakuraGroup plugin;
	private final int EXPIRED_TIME = 60 * 60 * 24 * 7; // 1週間 = 604800秒
	private boolean force = false;
	private String senderName = null;

	public ExpiredCheck(final SakuraGroup plugin, final CommandSender sender){
		this.plugin = plugin;

		if (sender != null){
			force = true;
			senderName = (sender instanceof Player) ? sender.getName() : null;
		}
	}
	public ExpiredCheck(final SakuraGroup plugin){
		this(plugin, null);
	}

	@Override
	public void run(){
		if (running) return;
		running = true;

		try{
			final Long threshold = Util.getCurrentUnixSec() - EXPIRED_TIME;
			final String defGroup = plugin.getConfigs().getDefGroup();

			// Get Database
			final Database db = SakuraGroup.getDatabases();
			HashMap<Integer, ArrayList<String>> result =
					db.read("SELECT `player_name`, `group`, `status` FROM " + db.getTablePrefix() + "users WHERE `lastpaid` <= ? AND `group` != ?",
							threshold.intValue(), defGroup);

			// Record not found
			if (result.size() == 0){
				running = false;
				return;
			}

			final PEXManager mgr = plugin.getPEXmgr();

			// Loop records
			int i = 0;
			for (ArrayList<String> record : result.values()){
				String pname = record.get(0);
				String gname = record.get(1); //TODO: use this?
				int status = Integer.valueOf(record.get(2));

				// TODO: check status here?

				// Update
				db.write("UPDATE " + db.getTablePrefix() + "users SET `group` = ?, `lastpaid` = ? WHERE `player_name` = ?", defGroup, 0, pname);

				// Change group
				mgr.changeGroup(pname, defGroup, null);

				// messaging
				Player player = Bukkit.getPlayer(pname);
				if (player != null && player.isOnline()){
					Actions.broadcastMessage(msgPrefix+ "&cグループの有効期限が切れ、デフォルトグループに戻りました！");
				}

				Group group = mgr.getGroup(gname);
				String gstr = (group != null) ? "&b:" + group.getColor() + group.getName() : "";
				Actions.broadcastMessage(msgPrefix+ "&bプレイヤー " + pname + gstr + " &bのグループ有効期限が切れました！");
				i++;
			}

			// logging
			if (i > 0)
				log.info(logPrefix + i +" player(s) expired the effective group! Changed to default group!");

			// force messaging
			if (force){
				// PlayerSender
				if (senderName != null){
					Player player = Bukkit.getPlayer(senderName);
					if (player != null && player.isOnline()){
						Actions.message(player, "&aチェックが終了し、" + i + "人のグループを変更しました！");
					}
				}
				// ConsoleSender
				else{
					Actions.message(Bukkit.getConsoleSender(), "&aチェックが終了し、" + i + "人のグループを変更しました！");
				}
			}

		}catch (Exception ex){
			ex.printStackTrace();
		}finally{
			running = false;
		}
	}

	public static boolean isRunning(){
		return running;
	}
}

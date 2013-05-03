/**
 * SakuraGroup - Package: net.syamn.sakuragroup.command Created: 2012/10/18
 * 7:55:48
 */
package net.syamn.sakuragroup.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.syamn.sakuragroup.SakuraGroup;
import net.syamn.sakuragroup.command.queue.Queueable;
import net.syamn.sakuragroup.database.Database;
import net.syamn.sakuragroup.manager.PEXManager;
import net.syamn.sakuragroup.permission.Perms;
import net.syamn.sakuragroup.utils.plugin.Actions;
import net.syamn.utils.exception.CommandException;

/**
 * LeaveCommand (LeaveCommand.java)
 * 
 * @author syam(syamn)
 */
public class LeaveCommand extends BaseCommand implements Queueable {
    public LeaveCommand() {
        bePlayer = true;
        name = "leave";
        argLength = 0;
        usage = "<- leave to default group";
    }

    @Override
    public void execute() throws CommandException {
        String currentGroup = plugin.getPEXmgr().getCurrentGroup(player);
        if (plugin.getConfigs().getDefGroup().equalsIgnoreCase(currentGroup)) {
            throw new CommandException("あなたは既にデフォルトグループメンバーです！");
        }

        plugin.getQueue().addQueue(sender, this, args, 15);
        Actions.message(sender, "&dデフォルトグループに戻ろうとしています！");
        Actions.message(sender, "&dこの操作は取り消しすることができません。本当に続行しますか？");
        Actions.message(sender, "&d続行するには &a/group confirm &dコマンドを入力してください！");
        Actions.message(sender, "&a/group confirm &dコマンドは15秒間のみ有効です。");
    }

    @Override
    public void executeQueue(List<String> qArgs) {
        PEXManager mgr = plugin.getPEXmgr();
        final String defGroup = plugin.getConfigs().getDefGroup();

        if (defGroup.equalsIgnoreCase(plugin.getPEXmgr().getCurrentGroup(player))) {
            Actions.message(player, "&cあなたは既にデフォルトグループメンバーです！");
            return;
        }

        // prepare update
        int status = 0;
        int changed = 0;
        int timestamp = 0;

        // Get Database
        Database db = SakuraGroup.getDatabases();
        HashMap<Integer, ArrayList<String>> result = db.read("SELECT `status`, `changed`, `lastchange` FROM " + db.getTablePrefix() + "users WHERE `player_name` = ?", player.getName());
        if (result.size() > 0) {
            // 既にDB登録済み チェック
            ArrayList<String> record = result.get(1);

            status = Integer.valueOf(record.get(0));
            changed = Integer.valueOf(record.get(1));
            timestamp = Integer.valueOf(record.get(2));

            // ステータスチェック
            if (status != 0) {
                Actions.message(player, "&cあなたはグループの変更を禁止されています！");
                return;
            }
        }

        // Update
        db.write("REPLACE INTO " + db.getTablePrefix() + "users (`player_name`, `group`, `status`, `changed`, `lastchange`, `lastpaid`) " + "VALUES (?, ?, ?, ?, ?, ?)", player.getName(), defGroup, status, changed, timestamp, 0);

        // Change group
        mgr.changeGroup(player.getName(), defGroup, null);

        // messaging
        Actions.broadcastMessage(msgPrefix + "&6" + player.getName() + "&aさんがデフォルトグループに戻りました！");
    }

    @Override
    public boolean permission() {
        return Perms.LEAVE.has(sender);
    }
}

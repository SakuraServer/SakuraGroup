/**
 * SakuraGroup - Package: net.syamn.sakuragroup.command Created: 2012/10/17
 * 7:41:13
 */
package net.syamn.sakuragroup.command;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import net.syamn.sakuragroup.Group;
import net.syamn.sakuragroup.SakuraGroup;
import net.syamn.sakuragroup.database.Database;
import net.syamn.sakuragroup.manager.PEXManager;
import net.syamn.sakuragroup.manager.SignManager;
import net.syamn.sakuragroup.permission.Perms;
import net.syamn.utils.LogUtil;
import net.syamn.utils.TimeUtil;
import net.syamn.utils.Util;
import net.syamn.utils.economy.EconomyUtil;
import net.syamn.utils.exception.CommandException;
import net.syamn.utils.queue.ConfirmQueue;
import net.syamn.utils.queue.Queueable;
import net.syamn.utils.queue.QueuedCommand;

/**
 * ChangeCommand (ChangeCommand.java)
 * 
 * @author syam(syamn)
 */
public class ChangeCommand extends BaseCommand implements Queueable {
    public ChangeCommand() {
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
        if (args.size() >= 1) {
            if (!Perms.CHANGE_CMD.has(sender)) {
                throw new CommandException("&cコマンドから直接グループを変更する権限がありません！");
            }
            update(args.get(0));
        }
        // 引数なし 選択チェック
        else {
            if (!Perms.CHANGE_SIGN.has(sender)) {
                throw new CommandException("&c変更するグループ名を指定してください！");
            }
            Group group = SignManager.getSelectedGroup(player);
            if (group == null) {
                throw new CommandException("&cあなたはまだグループ看板を選択していません！");
            }
            update(group);
        }
    }

    private void update(final String newGroupName) throws CommandException {
        // 新グループ確定
        Group group = mgr.getGroup(args.get(0));
        if (group == null) {
            throw new CommandException("&c指定したグループは存在しません！");
        }

        update(group);
    }

    private void update(final Group group) throws CommandException {
        // prepare update
        this.group = group;

        // Get Database
        Database db = SakuraGroup.getDatabases();
        HashMap<Integer, ArrayList<String>> result = db.read("SELECT `group`, `status`, `changed`, `lastchange` FROM " + db.getTablePrefix() + "users WHERE `player_name` = ?", player.getName());
        if (result.size() > 0) {
            // 既にDB登録済み チェック
            ArrayList<String> record = result.get(1);

            String currentGroup = record.get(0);
            status = Integer.valueOf(record.get(1));
            changed = Integer.valueOf(record.get(2));
            Long changedTime = Long.valueOf(record.get(3));

            // グループチェック
            if (group.getName().equalsIgnoreCase(currentGroup)) {
                throw new CommandException("&c既に同じグループに所属しています！");
            }

            // ステータスチェック
            if (status != 0) {
                throw new CommandException("&cあなたはグループの変更を禁止されています！");
            }

            // 時間チェック
            Calendar time = Calendar.getInstance();
            time.setTime(TimeUtil.getDateByUnixSeconds(changedTime));
            time.add(plugin.getConfigs().getMeasure(), plugin.getConfigs().getTime());
            if (!time.before(Calendar.getInstance())) {
                Util.message(sender, "&cあなたはまだグループの変更可能時間に達していません！");
                throw new CommandException(" &cあと &6" + net.syamn.sakuragroup.utils.plugin.PUtil.getDiffString(TimeUtil.getCurrentUnixSec(), time.getTimeInMillis() / 1000) + " &c待つ必要があります！");
            }
        }

        // ArrayList<String> strArgs = new ArrayList<String>() {{add
        // (group.getName());}};

        // Put variables
        this.cost = plugin.getConfigs().getGroupCost(group.getName());
        if (plugin.getConfigs().getUseVault() && cost < 0) {
            LogUtil.warning("Group " + group.getName() + " cost config NOT exist or negative value! Change to 0.");
            cost = 0.0D;
        }

        // Put Queue, messaging
        ConfirmQueue.getInstance().addQueue(sender, this, null, 15);
        Util.message(sender, "&dグループ  " + group.getColor() + group.getName() + " &dに変更しようとしています！");
        if (plugin.getConfigs().getUseVault() && cost > 0 && !Perms.FREE_CHANGE.has(player)) {
            Util.message(sender, "&d変更費用として &6" + EconomyUtil.getCurrencyString(cost) + " &dが必要です！");
        }
        Util.message(sender, "&dこの操作は取り消しすることができません。本当に続行しますか？");
        Util.message(sender, "&d続行するには &a/group confirm &dコマンドを入力してください！");
        Util.message(sender, "&a/group confirm &dコマンドは15秒間のみ有効です。");
    }

    @Override
    public void executeQueue(QueuedCommand queued) {
        // check null
        if (this.group == null) {
            Util.message(player, "&cこのグループは存在しません！");
            return;
        }

        // Check player limit
        int limit = plugin.getConfigs().getGroupLimit(group.getName());
        Set<String> names = mgr.getPlayersByGroup(group.getName());
        if (limit > 0 && names.size() >= limit) {
            Util.message(player, "&cこのグループは定員(" + limit + "人)に達しています！");
            return;
        }

        // Pay cost
        boolean paid = false;
        if (plugin.getConfigs().getUseVault() && cost > 0 && !Perms.FREE_CHANGE.has(player)) {
            paid = EconomyUtil.takeMoney(player.getName(), cost);
            if (!paid) {
                Util.message(player, "&cお金が足りません！ " + EconomyUtil.getCurrencyString(cost) + "必要です！");
                return;
            }
        }

        // Update!
        Long timestamp = TimeUtil.getCurrentUnixSec();
        Database db = SakuraGroup.getDatabases();
        db.write("REPLACE INTO " + db.getTablePrefix() + "users (`player_name`, `group`, `status`, `changed`, `lastchange`, `lastpaid`) " + "VALUES (?, ?, ?, ?, ?, ?)", player.getName(), group.getName(), status, changed + 1, timestamp.intValue(), timestamp.intValue());
        // Change group!
        mgr.changeGroup(player.getName(), group.getName(), null);

        // messaging
        Util.broadcastMessage(msgPrefix + "&6" + player.getName() + "&aさんが&f" + group.getColor() + group.getName() + "&aグループに所属しました！");

        String msg = msgPrefix + "&aあなたのグループを変更しました！";
        if (paid) msg = msg + " &c(-" + EconomyUtil.getCurrencyString(cost) + ")";
        Util.message(player, msg);
    }

    @Override
    public boolean permission() {
        return (Perms.CHANGE_CMD.has(sender) || Perms.CHANGE_SIGN.has(sender));
    }
}

/**
 * SakuraGroup - Package: net.syamn.sakuragroup.command Created: 2012/10/25
 * 12:05:39
 */
package net.syamn.sakuragroup.command;

import java.util.List;

import net.syamn.sakuragroup.Group;
import net.syamn.sakuragroup.SakuraGroup;
import net.syamn.sakuragroup.database.Database;
import net.syamn.sakuragroup.manager.PEXManager;
import net.syamn.sakuragroup.permission.Perms;
import net.syamn.utils.TimeUtil;
import net.syamn.utils.Util;
import net.syamn.utils.economy.EconomyUtil;
import net.syamn.utils.exception.CommandException;
import net.syamn.utils.queue.ConfirmQueue;
import net.syamn.utils.queue.Queueable;
import net.syamn.utils.queue.QueuedCommand;

/**
 * PayCommand (PayCommand.java)
 * 
 * @author syam(syamn)
 */
public class PayCommand extends BaseCommand implements Queueable {
    public PayCommand() {
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
        if (plugin.getConfigs().getDefGroup().equalsIgnoreCase(currentGroup)) {
            throw new CommandException("&cあなたはデフォルトグループメンバーです！");
        }
        group = mgr.getGroup(currentGroup);
        if (group == null) {
            throw new CommandException("&cあなたは特別グループに所属していません！");
        }

        cost = group.getKeepCost();

        // Put queue
        ConfirmQueue.getInstance().addQueue(sender, this, null, 15);
        Util.message(sender, "&dグループ  " + group.getColor() + group.getName() + " &dの更新料を支払おうとしています！");
        if (plugin.getConfigs().getUseVault() && cost > 0 && !Perms.FREE_CHANGE.has(player)) {
            Util.message(sender, "&d更新費用として &6" + EconomyUtil.getCurrencyString(cost) + " &dが必要です！");
        }
        Util.message(sender, "&d支払った日時から起算して1週間グループを維持できます！");
        Util.message(sender, "&d続行するには &a/group confirm &dコマンドを入力してください！");
        Util.message(sender, "&a/group confirm &dコマンドは15秒間のみ有効です。");
    }

    @Override
    public void executeQueue(QueuedCommand queued) {
        // Check again
        if (group == null) {
            Util.message(sender, "&cあなたは特別グループに所属していません！");
            return;
        }

        // Pay cost
        boolean paid = false;
        if (plugin.getConfigs().getUseVault() && cost > 0 && !Perms.FREE_PAY.has(player)) {
            paid = EconomyUtil.takeMoney(player.getName(), cost);
            if (!paid) {
                Util.message(sender, "&cお金が足りません！ " + EconomyUtil.getCurrencyString(cost) + "必要です！");
                return;
            }
        }

        // Update!
        Database db = SakuraGroup.getDatabases();
        db.write("UPDATE " + db.getTablePrefix() + "users SET `lastpaid` = ? WHERE `player_name` = ?", TimeUtil.getCurrentUnixSec().intValue(), player.getName());

        String msg = msgPrefix + "&aグループの維持費を支払いました！";
        if (paid) msg = msg + " &c(-" + EconomyUtil.getCurrencyString(cost) + ")";
        Util.message(player, msg);
    }

    @Override
    public boolean permission() {
        return Perms.PAY.has(sender);
    }
}

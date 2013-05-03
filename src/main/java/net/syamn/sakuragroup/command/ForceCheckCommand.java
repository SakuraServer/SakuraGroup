/**
 * SakuraGroup - Package: net.syamn.sakuragroup.command
 * Created: 2012/10/25 13:54:47
 */
package net.syamn.sakuragroup.command;

import net.syamn.sakuragroup.permission.Perms;
import net.syamn.sakuragroup.task.ExpiredCheck;
import net.syamn.sakuragroup.utils.plugin.Actions;
import net.syamn.utils.exception.CommandException;

/**
 * ForceCheckCommand (ForceCheckCommand.java)
 * @author syam(syamn)
 */
public class ForceCheckCommand extends BaseCommand{
	public ForceCheckCommand(){
		bePlayer = false;
		name = "forcecheck";
		argLength = 0;
		usage = "<- force group expired check";
	}

	@Override
	public void execute() throws CommandException {
		if (ExpiredCheck.isRunning()){
			throw new CommandException("&c既にチェックタスクが起動しています！");
		}

		//Thread checkTask = new Thread(new ExpiredCheck(plugin, sender));
		//checkTask.start();
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new ExpiredCheck(plugin, sender), 0L);

		Actions.message(sender, "&aチェックを開始しました");
	}

	@Override
	public boolean permission() {
		return Perms.FORCE_CHECK.has(sender);
	}
}

/**
 * SakuraGroup - Package: syam.sakuragroup.command
 * Created: 2012/10/25 13:54:47
 */
package syam.sakuragroup.command;

import syam.sakuragroup.exception.CommandException;
import syam.sakuragroup.permission.Perms;
import syam.sakuragroup.task.ExpiredCheck;
import syam.sakuragroup.util.Actions;

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

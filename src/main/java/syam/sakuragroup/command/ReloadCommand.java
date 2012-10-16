/**
 * SakuraGroup - Package: syam.sakuragroup.command
 * Created: 2012/10/16 3:38:23
 */
package syam.sakuragroup.command;

import syam.sakuragroup.permission.Perms;
import syam.sakuragroup.util.Actions;

/**
 * ReloadCommand (ReloadCommand.java)
 * @author syam(syamn)
 */
public class ReloadCommand extends BaseCommand {
	public ReloadCommand(){
		bePlayer = false;
		name = "reload";
		argLength = 0;
		usage = "<- reload config.yml";
	}

	@Override
	public void execute() {
		try{
			plugin.getConfigs().loadConfig(false);
		}catch (Exception ex){
			log.warning(logPrefix+"an error occured while trying to load the config file.");
			ex.printStackTrace();
			return;
		}

		// TODO: データベース保存 新設定で接続試行

		// 権限管理プラグイン再設定
		Perms.setupPermissionHandler();

		Actions.message(sender, "&aConfiguration reloaded!");
	}

	@Override
	public boolean permission() {
		return Perms.RELOAD.has(sender);
	}

}

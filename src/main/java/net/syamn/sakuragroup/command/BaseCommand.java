/**
 * SakuraGroup - Package: net.syamn.sakuragroup.command
 * Created: 2012/10/16 3:13:30
 */
package net.syamn.sakuragroup.command;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.syamn.sakuragroup.SakuraGroup;
import net.syamn.sakuragroup.utils.plugin.Actions;
import net.syamn.utils.exception.CommandException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 * BaseCommand (BaseCommand.java)
 * @author syam(syamn)
 */
public abstract class BaseCommand {
	// Logger
	protected static final Logger log = SakuraGroup.log;
	protected static final String logPrefix = SakuraGroup.logPrefix;
	protected static final String msgPrefix = SakuraGroup.msgPrefix;
	/* コマンド関係 */
	public CommandSender sender;
	public List<String> args = new ArrayList<String>();
	public String name;
	public int argLength = 0;
	public String usage;
	public boolean bePlayer = true;
	public Player player;
	public String command;
	public SakuraGroup plugin;

	public boolean run(SakuraGroup plugin, CommandSender sender, String[] preArgs, String cmd) {
		this.plugin = plugin;
		this.sender = sender;
		this.command = cmd;

		// 引数をソート
		args.clear();
		for (String arg : preArgs)
			args.add(arg);

		// 引数からコマンドの部分を取り除く
		// (コマンド名に含まれる半角スペースをカウント、リストの先頭から順にループで取り除く)
		for (int i = 0; i < name.split(" ").length && i < args.size(); i++)
			args.remove(0);

		// 引数の長さチェック
		if (argLength > args.size()){
			sendUsage();
			return true;
		}

		// 実行にプレイヤーであることが必要かチェックする
		if (bePlayer && !(sender instanceof Player)){
			Actions.message(sender, "&cThis command cannot run from Console!");
			return true;
		}
		if (sender instanceof Player){
			player = (Player)sender;
		}

		// 権限チェック
		if (!permission()){
			Actions.message(sender, "&cYou don't have permission to use this!");
			return true;
		}

		// 実行
		try {
			execute();
		}
		catch (CommandException ex) {
			Throwable error = ex;
			while (error instanceof CommandException){
				Actions.message(sender, error.getMessage());
				error = error.getCause();
			}
		}

		return true;
	}

	/**
	 * コマンドを実際に実行する
	 * @return 成功すればtrue それ以外はfalse
	 * @throws CommandException CommandException
	 */
	public abstract void execute() throws CommandException;

	/**
	 * コマンド実行に必要な権限を持っているか検証する
	 * @return trueなら権限あり、falseなら権限なし
	 */
	public abstract boolean permission();

	/**
	 * コマンドの使い方を送信する
	 */
	public void sendUsage(){
		Actions.message(sender, "&c/"+this.command+" "+name+" "+usage);
	}
}

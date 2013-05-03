/**
 * SakuraGroup - Package: net.syamn.sakuragroup.utils.plugin
 * Created: 2012/10/16 3:13:54
 */
package net.syamn.sakuragroup.utils.plugin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.EconomyResponse;
import net.syamn.sakuragroup.SakuraGroup;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


/**
 * Actions (Actions.java)
 * @author syam(syamn)
 */
public class Actions {
	// Logger
	private static final Logger log = SakuraGroup.log;
	private static final String logPrefix = SakuraGroup.logPrefix;
	private static final String msgPrefix = SakuraGroup.msgPrefix;

	private final SakuraGroup plugin;

	public Actions(SakuraGroup plugin){
		this.plugin = plugin;
	}

	/****************************************/
	// メッセージ送信系関数
	/****************************************/
	/**
	 * メッセージをユニキャスト
	 * @param message メッセージ
	 */
	public static void message(CommandSender sender, String message){
		if (sender != null && message != null){
			sender.sendMessage(message.replaceAll("&([0-9a-fk-or])", "\u00A7$1"));
		}
	}
	public static void message(Player player, String message){
		if (player != null && message != null){
			player.sendMessage(message.replaceAll("&([0-9a-fk-or])", "\u00A7$1"));
		}
	}

	/**
	 * メッセージをブロードキャスト
	 * @param message メッセージ
	 */
	public static void broadcastMessage(String message){
		if (message != null){
			message = message.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
			//debug(message);//debug
			Bukkit.broadcastMessage(message);
		}
	}
	/**
	 * メッセージをワールドキャスト
	 * @param world
	 * @param message
	 */
	public static void worldcastMessage(World world, String message){
		if (world != null && message != null){
			message = message.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
			for(Player player: world.getPlayers()){
				player.sendMessage(message);
			}
			log.info("[Worldcast]["+world.getName()+"]: " + message);
		}
	}
	/**
	 * メッセージをパーミッションキャスト(指定した権限ユーザにのみ送信)
	 * @param permission 受信するための権限ノード
	 * @param message メッセージ
	 */
	public static void permcastMessage(String permission, String message){
		// OK
		int i = 0;
		for (Player player : Bukkit.getServer().getOnlinePlayers()){
			if (player.hasPermission(permission)){
				Actions.message(player, message);
				i++;
			}
		}

		log.info("Received "+i+"players: "+message);
	}

	/****************************************/
	// ユーティリティ
	/****************************************/
	/**
	 * 文字配列をまとめる
	 * @param s つなげるString配列
	 * @param glue 区切り文字 通常は半角スペース
	 * @return
	 */
	public static String combine(String[] s, String glue)
	{
		int k = s.length;
		if (k == 0){ return null; }
		StringBuilder out = new StringBuilder();
		out.append(s[0]);
		for (int x = 1; x < k; x++){
			out.append(glue).append(s[x]);
		}
		return out.toString();
	}
	/**
	 * コマンドをコンソールから実行する
	 * @param command
	 */
	public static void executeCommandOnConsole(String command){
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
	}
	/**
	 * 文字列の中に全角文字が含まれているか判定
	 * @param s 判定する文字列
	 * @return 1文字でも全角文字が含まれていればtrue 含まれていなければfalse
	 * @throws UnsupportedEncodingException
	 */
	public static boolean containsZen(String s)
			throws UnsupportedEncodingException {
		for (int i = 0; i < s.length(); i++) {
			String s1 = s.substring(i, i + 1);
			if (URLEncoder.encode(s1,"MS932").length() >= 4) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 現在の日時を yyyy-MM-dd HH:mm:ss 形式の文字列で返す
	 * @return
	 */
	public static String getDatetime(){

		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(date);
	}
	/**
	 * 座標データを ワールド名:x, y, z の形式の文字列にして返す
	 * @param loc
	 * @return
	 */
	public static String getLocationString(Location loc){
		return loc.getWorld().getName()+":"+loc.getX()+","+loc.getY()+","+loc.getZ();
	}
	public static String getBlockLocationString(Location loc){
		return loc.getWorld().getName()+":"+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();
	}
	/**
	 * デバッグ用 syamnがオンラインならメッセージを送る
	 * @param msg
	 */
	public static void debug(String msg){
		OfflinePlayer syamn = Bukkit.getServer().getOfflinePlayer("syamn");
		if (syamn.isOnline()){
			Actions.message((Player) syamn, msg);
		}
	}
	/**
	 * 文字列の&(char)をカラーコードに変換して返す
	 * @param string 文字列
	 * @return 変換後の文字列
	 */
	public static String coloring(String string){
		if (string == null) return null;
		return string.replaceAll("&([0-9a-fA-Fk-oK-Or])", "\u00A7$1");
	}

	/****************************************/
	// 所持金操作系関数 - Vault
	/****************************************/
	/**
	 * 指定したユーザーにお金を加える
	 * @param name ユーザー名
	 * @param amount 金額
	 * @return 成功ならtrue、失敗ならfalse
	 */
	public static boolean addMoney(String name, double amount){
		if (amount < 0) return false; // 負数は許容しない
		EconomyResponse r = SakuraGroup.getInstance().getEconomy().depositPlayer(name, amount);
		if(r.transactionSuccess()) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 指定したユーザーからお金を引く
	 * @param name ユーザー名
	 * @param amount 金額
	 * @return 成功ならtrue、失敗ならfalse
	 */
	public static boolean takeMoney(String name, double amount){
		if (amount < 0) return false; // 負数は許容しない
		EconomyResponse r = SakuraGroup.getInstance().getEconomy().withdrawPlayer(name, amount);
		if(r.transactionSuccess()) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 指定したユーザーがお金を持っているか
	 * @param name ユーザー名
	 * @param amount 金額
	 * @return 持っていればtrue、無ければfalse
	 */
	public static boolean checkMoney(String name, double amount){
		return (SakuraGroup.getInstance().getEconomy().has(name, amount));
	}
	/**
	 * 指定した金額での適切な通貨単位を返す
	 * @param amount 金額
	 * @return 通貨単位
	 */
	public static String getCurrencyName(double amount){
		if (amount <= 1.0D){
			return SakuraGroup.getInstance().getEconomy().currencyNameSingular();
		}else{
			return SakuraGroup.getInstance().getEconomy().currencyNamePlural();
		}
	}
	/**
	 * 指定した金額での適切な単位を含めた文字列を返す
	 * @param amount 金額
	 * @return 文字列
	 */
	public static String getCurrencyString(double amount){
		return SakuraGroup.getInstance().getEconomy().format(amount);
	}

	/****************************************/
	/* ログ操作系 */
	/****************************************/
	/**
	 * ログファイルに書き込み
	 * @param file ログファイル名
	 * @param line ログ内容
	 */
	public static void log(String filepath, String line){
		TextFileHandler r = new TextFileHandler(filepath);
		try{
			r.appendLine("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] " + line);
		} catch (IOException ex) {}
	}

	/****************************************/
	/* その他 */
	/****************************************/
	// プレイヤーがオンラインかチェックしてテレポートさせる
	public static void tpPlayer(Player player, Location loc){
		if (player == null || loc == null || !player.isOnline())
			return;
		player.teleport(loc);
	}

	// プレイヤーのインベントリをその場にドロップさせる
	public static void dropInventoryItems(Player player){
		if (player == null) return;

		PlayerInventory inv = player.getInventory();
		Location loc = player.getLocation();

		// インベントリアイテム
		for (ItemStack i : inv.getContents()) {
			if (i != null && i.getType() != Material.AIR) {
				inv.remove(i);
				player.getWorld().dropItemNaturally(loc, i);
			}
		}

		// 防具アイテム
		for (ItemStack i : inv.getArmorContents()){
			if (i != null && i.getType() != Material.AIR) {
				inv.remove(i);
				player.getWorld().dropItemNaturally(loc, i);
			}
		}
	}
}
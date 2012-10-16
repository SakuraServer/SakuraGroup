/**
 * SakuraGroup - Package: syam.sakuragroup.manager
 * Created: 2012/10/16 4:16:59
 */
package syam.sakuragroup.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import syam.sakuragroup.PlayerProfile;
import syam.sakuragroup.SakuraGroup;

/**
 * PlayerManager (PlayerManager.java)
 * @author syam(syamn)
 */
public class PlayerManager {
	// Logger
	private static final Logger log = SakuraGroup.log;
	private static final String logPrefix = SakuraGroup.logPrefix;
	private static final String msgPrefix = SakuraGroup.msgPrefix;

	private static Map<String, PlayerProfile> players = new HashMap<String, PlayerProfile>();

	/**
	 * プレイヤーを追加します
	 * @param player 追加するプレイヤー
	 * @return プレイヤーオブジェクト {@link LPlayer}
	 */
	public static PlayerProfile addPlayerProfile(Player player){
		PlayerProfile prof = players.get(player.getName());

		if (prof != null){
			// プレイヤーオブジェクトは接続ごとに違うものなので再設定する
			prof.setPlayer(player);
		}else{
			// 新規プレイヤー
			prof = new PlayerProfile(player.getName(), true);
			players.put(player.getName(), prof);
		}

		return prof;
	}

	/**
	 * 指定したプレイヤーをマップから削除します
	 * @param playerName 削除するプレイヤー名
	 */
	public static void removeProfile(String playerName){
		players.remove(playerName);
	}
	/**
	 * プレイヤーマップを全削除します
	 */
	public static void clearAllProfiles(){
		players.clear();
	}
	/**
	 * 全プレイヤーデータを保存する
	 */
	public static int saveAllProfiles(){
		int i = 0;
		for (PlayerProfile prof : players.values()){
			prof.save();
			i++;
		}
		return i;
	}
	/**
	 * プレイヤーを取得する
	 * @param playerName 取得対象のプレイヤー名
	 * @return プレイヤー {@link LPlayer}
	 */
	public static PlayerProfile getProfile(String playerName){
		return players.get(playerName);
	}
	/**
	 * プレイヤーのプロフィールを取得する
	 * @param player 取得対象のプレイヤー
	 * @return プレイヤープロフィール {@link PlayerProfile}
	 */
	public static PlayerProfile getProfile(OfflinePlayer player){
		return getProfile(player.getName());
	}

	/* getter / setter */
	public static Map<String, PlayerProfile> getProfiles(){
		return players;
	}
}

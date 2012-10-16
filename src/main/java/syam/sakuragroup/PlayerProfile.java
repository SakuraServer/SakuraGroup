/**
 * SakuraGroup - Package: syam.sakuragroup
 * Created: 2012/10/16 4:40:31
 */
package syam.sakuragroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import syam.sakuragroup.database.Database;

/**
 * PlayerProfile (PlayerProfile.java)
 * @author syam(syamn)
 */
public class PlayerProfile {
	// Logger
	private static final Logger log = SakuraGroup.log;
	private static final String logPrefix =SakuraGroup.logPrefix;
	private static final String msgPrefix = SakuraGroup.msgPrefix;

	private String playerName;
	private boolean loaded = false;

	private Player player;

	/* mySQL Stuff */
	private int playerID;

	/* flag */
	private boolean dirty;

	/* Data */
	private int group;
	private int status;
	private Long lastchange = 0L;


	/**
	 * コンストラクタ
	 * @param playerName プレイヤー名
	 * @param addNew 新規プレイヤーとしてデータを読み込むかどうか
	 */
	public PlayerProfile(String playerName, boolean addNew){
		this.playerName = playerName;

		if (!loadMySQL() && addNew){
			addMySQLPlayer();
			loaded = true;
		}
	}

	/**
	 * データベースからプレイヤーデータを読み込み
	 * @return 正常終了すればtrue、基本データテーブルにデータがなければfalse
	 */
	public boolean loadMySQL(){
		Database db = SakuraGroup.getDatabases();

		// プレイヤーID(DB割り当て)を読み出す
		playerID = db.getInt("SELECT player_id FROM " + db.getTablePrefix() + "users WHERE player_name = ?", playerName);

		// プレイヤー基本テーブルにデータがなければ何もしない
		if (playerID == 0){
			return false;
		}

		/* *** usersテーブルデータ読み込み *************** */
		HashMap<Integer, ArrayList<String>> profileDatas = db.read(
				"SELECT `group`, `status`, `lastchange` FROM " + db.getTablePrefix() + "users WHERE player_id = ?", playerID);
		ArrayList<String> dataValues = profileDatas.get(1);

		if (dataValues == null){
			// 新規レコード追加
			log.warning(playerName + " does not exist in the users table!");
		}else{
			// データ読み出し
			this.group = Integer.valueOf(dataValues.get(0));
			this.status = Integer.valueOf(dataValues.get(1));
			this.lastchange = Long.valueOf(dataValues.get(2));

			dataValues.clear();
		}

		// 読み込み正常終了
		loaded = true;
		dirty = false;
		return true;
	}

	/**
	 * 新規ユーザーデータをMySQLデータベースに追加
	 */
	private void addMySQLPlayer(){
		Database db = SakuraGroup.getDatabases();

		db.write("INSERT INTO " + db.getTablePrefix() + "users (player_name) VALUES (?)", playerName); // usersテーブル
		playerID = db.getInt("SELECT player_id FROM "+db.getTablePrefix() + "users WHERE player_name = ?", playerName);
	}

	/**
	 * プレイヤーデータをMySQLデータベースに保存
	 */
	public void save(boolean force){
		if (dirty || force){
			Database db = SakuraGroup.getDatabases();

			// データベースupdate

			/* usersテーブル */
			db.write("UPDATE " + db.getTablePrefix() + "users SET " +
					"`group` = ?" +
					", `status` = ?" +
					", `lastchange` = ?" +
					" WHERE player_id = ?",
					this.group, this.status, this.lastchange.intValue(),
					this.playerID);
			dirty = false;
		}
	}
	public void save(){
		save(false);
	}

	/* getter / setter */
	public void setPlayer(Player player){
		this.player = player;
	}
	public Player getPlayer(){
		return this.player;
	}

	public int getPlayerID(){
		return playerID;
	}
	public String getPlayerName(){
		return playerName;
	}
	public boolean isLoaded(){
		return loaded;
	}

	/* Data */
	// status
	public void setStatus(final int status){
		this.status = status;
		this.dirty = true;
	}
	public int getStatus(){
		return this.status;
	}

	// lastChange
	public void setLastChange(long time){
		this.lastchange = time;
		this.dirty = true;
	}
	public void updateLastChange(){
		this.lastchange = System.currentTimeMillis() / 1000;
		this.dirty = true;
	}
	public long getlastChange(){
		return this.lastchange;
	}
}

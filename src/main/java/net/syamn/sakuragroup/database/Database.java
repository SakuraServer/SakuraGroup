/**
 * SakuraGroup - Package: net.syamn.sakuragroup.database
 * Created: 2012/10/16 4:11:44
 */
package net.syamn.sakuragroup.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import net.syamn.sakuragroup.SakuraGroup;


/**
 * Database (Database.java)
 * @author syam(syamn)
 */
public class Database {
	// Logger
	public static final Logger log = SakuraGroup.log;
	private static final String logPrefix = SakuraGroup.logPrefix;
	private static final String msgPrefix = SakuraGroup.msgPrefix;

	private static SakuraGroup plugin;

	private static String connectionString = null;
	private static String tablePrefix = null;
	private static Connection connection = null;
	private static long reconnectTimestamp = 0;

	/**
	 * コンストラクタ
	 * @param plugin FlagGameプラグインインスタンス
	 */
	public Database(final SakuraGroup plugin){
		Database.plugin = plugin;

		// 接続情報読み込み
		connectionString = "jdbc:mysql://" + plugin.getConfigs().getMySQLaddress() + ":" + plugin.getConfigs().getMySQLport()
				+ "/" + plugin.getConfigs().getMySQLdbname() + "?user=" + plugin.getConfigs().getMySQLusername() + "&password=" + plugin.getConfigs().getMySQLuserpass();
		tablePrefix = plugin.getConfigs().getMySQLtablePrefix();

		connect(); // 接続

		// ドライバを読み込む
		try{
			Class.forName("com.mysql.jdbc.Driver");
			DriverManager.getConnection(connectionString);
		}catch (ClassNotFoundException ex1){
			log.severe(ex1.getLocalizedMessage());
		}catch (SQLException ex2){
			log.severe(ex2.getLocalizedMessage());
			printErrors(ex2);
		}
	}

	/**
	 * データベースに接続する
	 */
	public static void connect(){
		try{
			log.info(logPrefix+ "Attempting connection to MySQL..");

			Properties connectionProperties = new Properties();
			connectionProperties.put("autoReconnect", "false");
			connectionProperties.put("maxReconnects", "0");
			connection = DriverManager.getConnection(connectionString, connectionProperties);

			log.info(logPrefix+ "Connected MySQL database!");
		}catch (SQLException ex){
			log.severe(logPrefix+ "Could not connect MySQL database!");
			ex.printStackTrace();
			printErrors(ex);
		}
	}

	/**
	 * データベース構造を構築する
	 */
	public void createStructure(){
		// ユーザープロフィールテーブル
		write("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "users` (" +
				"`player_id` int(10) unsigned NOT NULL AUTO_INCREMENT," +	// 割り当てられたプレイヤーID
				"`player_name` varchar(32) NOT NULL," +						// プレイヤー名
				"`group` varchar(50) NOT NULL," +							// グループ名
				"`status` int(2) unsigned NOT NULL DEFAULT '0'," +			// グループ変更した回数
				"`changed` int(9) unsigned NOT NULL DEFAULT '0'," +			// グループ変更した回数
				"`lastchange` int(32) unsigned NOT NULL DEFAULT '0'," +		// 最後にグループ変更した日時
				"`lastpaid` int(32) unsigned NOT NULL DEFAULT '0'," +		// 最後に更新料を払った日時
				"PRIMARY KEY (`player_id`)," +
				"UNIQUE KEY `player_name` (`player_name`)" +
				") ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;");
	}

	/* ******************** */
	/**
	 * 書き込みのSQLクエリーを発行する
	 * @param sql 発行するSQL文
	 * @return クエリ成功ならtrue、他はfalse
	 */
	public boolean write(String sql){
		return write(sql, new Object[0]);
	}
	public boolean write(String sql, Object... obj){
		// 接続確認
		if (isConnected()){
			PreparedStatement statement = null;
			try{
				statement = connection.prepareStatement(sql);
				// バインド
				if (obj != null && obj.length > 0){
					for (int i = 0; i < obj.length; i++){
						statement.setObject(i + 1, obj[i]);
					}
				}
				statement.executeUpdate(); // 実行
			}
			catch (SQLException ex){
				printErrors(ex);
				return false;
			}
			finally{
				// 後処理
				try {
					if (statement != null)
						statement.close();
				}catch (SQLException ex) { printErrors(ex); }
			}
			return true;
		}
		// 未接続
		else{
			attemptReconnect();
		}

		return false;
	}

	@Deprecated
	public boolean write(PreparedStatement statement){
		// 接続確認
		if (isConnected()){
			try{
				statement.executeUpdate(); // 実行

				// 後処理
				statement.close();

				return true;
			}catch (SQLException ex){
				printErrors(ex);

				return false;
			}
		}
		// 未接続
		else{
			attemptReconnect();
		}

		return false;
	}
	@Deprecated
	public PreparedStatement getPreparedStatement(String sql){
		// 接続確認
		if (isConnected()){
			try{
				PreparedStatement statement = connection.prepareStatement(sql);
				return statement;
			}catch (SQLException ex){
				printErrors(ex);
				return null;
			}
		}
		// 未接続
		else{
			attemptReconnect();
		}
		return null;
	}

	/* ******************** */
	/**
	 * 読み出しのSQLクエリーを発行する
	 * @param sql 発行するSQL文
	 * @return SQLクエリで得られたデータ
	 */
	public HashMap<Integer, ArrayList<String>> read(String sql){
		return read(sql, new Object[0]);
	}
	public HashMap<Integer, ArrayList<String>> read(String sql, Object... obj){
		ResultSet resultSet = null;
		HashMap<Integer, ArrayList<String>> rows = new HashMap<Integer, ArrayList<String>>();

		// 接続確認
		if (isConnected()){
			PreparedStatement statement = null;
			try{
				statement = connection.prepareStatement(sql);
				// バインド
				if (obj != null && obj.length > 0){
					for (int i = 0; i < obj.length; i++){
						statement.setObject(i + 1, obj[i]);
					}
				}
				resultSet = statement.executeQuery(); // 実行

				// 結果のレコード数だけ繰り返す
				while (resultSet.next()){
					ArrayList<String> column = new ArrayList<String>();

					// カラム内のデータを順にリストに追加
					for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++){
						column.add(resultSet.getString(i));
					}

					// 返すマップにレコード番号とリストを追加
					rows.put(resultSet.getRow(), column);
				}
			}catch (SQLException ex){
				printErrors(ex);
			}
			finally{
				// 後処理
				try {
					if (statement != null)
						statement.close();
				}catch (SQLException ex) { printErrors(ex); }

				try {
					if (resultSet != null)
						resultSet.close();
				}catch (SQLException ex) { printErrors(ex); }
			}
		}
		// 未接続
		else{
			attemptReconnect();
		}

		return rows;
	}


	/* ******************** */
	/**
	 * int型の値を取得します
	 * @param sql 発行するSQL文
	 * @return 最初のローにある数値
	 */
	public int getInt(String sql){
		return getInt(sql, new Object[0]);
	}
	public int getInt(String sql, Object... obj){
		ResultSet resultSet = null;
		int result = 0;

		// 接続確認
		if (isConnected()){
			PreparedStatement statement = null;
			try{
				statement = connection.prepareStatement(sql);
				// バインド
				if (obj != null && obj.length > 0){
					for (int i = 0; i < obj.length; i++){
						statement.setObject(i + 1, obj[i]);
					}
				}
				resultSet = statement.executeQuery(); // 実行

				if (resultSet.next()){
					result = resultSet.getInt(1);
				}else{
					// 結果がなければ0を返す
					result = 0;
				}
			}catch (SQLException ex){
				printErrors(ex);
			}
			finally{
				// 後処理
				try {
					if (statement != null)
						statement.close();
				}catch (SQLException ex) { printErrors(ex); }

				try {
					if (resultSet != null)
						resultSet.close();
				}catch (SQLException ex) { printErrors(ex); }
			}
		}
		// 未接続
		else{
			attemptReconnect();
		}

		return result;
	}

	/**
	 * テーブル接頭語を返します
	 * @return
	 */
	public String getTablePrefix(){
		return plugin.getConfigs().getMySQLtablePrefix();
	}

	/**
	 * 接続状況を返す
	 * @return 接続中ならtrue、タイムアウトすればfalse
	 */
	public static boolean isConnected(){
		if (connection == null){
			return false;
		}

		try{
			return connection.isValid(3);
		}catch (SQLException ex){
			return false;
		}
	}

	/**
	 * MySQLデータベースへ再接続を試みる
	 */
	public static void attemptReconnect(){
		final int RECONNECT_WAIT_TICKS = 60000;
		final int RECONNECT_DELAY_TICKS = 1200;

		if (reconnectTimestamp + RECONNECT_WAIT_TICKS < System.currentTimeMillis()){
			reconnectTimestamp = System.currentTimeMillis();
			log.severe(logPrefix+ "Conection to MySQL was lost! Attempting to reconnect 60 seconds...");
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new MySQLReconnect(plugin), RECONNECT_DELAY_TICKS);
		}
	}

	/**
	 * エラーを出力する
	 * @param ex
	 */
	private static void printErrors(SQLException ex){
		log.warning("SQLException:" +ex.getMessage());
		log.warning("SQLState:" +ex.getSQLState());
		log.warning("ErrorCode:" +ex.getErrorCode());

		log.warning("Stack Trace:");
		ex.printStackTrace();
	}
}

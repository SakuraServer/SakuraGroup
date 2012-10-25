/**
 * SakuraGroup - Package: syam.sakuragroup
 * Created: 2012/10/16 3:20:21
 */
package syam.sakuragroup;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

import syam.sakuragroup.util.Util;

/**
 * ConfigurationManager (ConfigurationManager.java)
 * @author syam(syamn)
 */
public class ConfigurationManager {
	// Logger
	private static final Logger log = SakuraGroup.log;
	private static final String logPrefix = SakuraGroup.logPrefix;
	private static final String msgPrefix = SakuraGroup.msgPrefix;

	private SakuraGroup plugin;
	private FileConfiguration conf;

	private static File pluginDir = new File("plugins", "SakuraGroup");

	// デフォルトの設定定数
	//private final ArrayList<String> availableGroups = new ArrayList<String>() {{add("Citizen"); add("Builder"); add("Engineer"); add("Designer");}};

	// 設定項目
	/* Basic Configs */
	private String defaultGroup = "Citizen";
	private List<String> groups = new ArrayList<String>();
	private int time = 7;
	private int measure = 0;
	private int expiredCheckInterval = 3;

	/* Vault Config */
	private boolean useVault = false;

	/* MySQL Configs */
	private String mysqlAddress = "localhost";
	private int mysqlPort = 3306;
	private String mysqlDBName = "DatabaseName";
	private String mysqlUserName = "UserName";
	private String mysqlUserPass = "UserPassword";
	private String mysqlTablePrefix = "sg_";

	/**
	 * コンストラクタ
	 * @param plugin
	 */
	public ConfigurationManager(final SakuraGroup plugin){
		this.plugin = plugin;
		pluginDir = this.plugin.getDataFolder();
	}

	/**
	 * 設定をファイルから読み込む
	 * @param initialLoad 初回ロードかどうか
	 */
	public void loadConfig(boolean initialLoad) throws Exception{
		// ディレクトリ作成
		createDirs();

		// 設定ファイルパス取得
		File file = new File(pluginDir, "config.yml");
		// 無ければデフォルトコピー
		if (!file.exists()){
			extractResource("/config.yml", pluginDir, false, true);
			log.info(logPrefix+ "config.yml is not found! Created default config.yml!");
		}

		plugin.reloadConfig();

		// 先にバージョンチェック
		double version = plugin.getConfig().getDouble("Version", 0.1D);
		checkver(version);

		/* Basic Configs */
		defaultGroup = plugin.getConfig().getString("DefaultGroup");
		groups.clear();
		if (plugin.getConfig().get("Groups") != null){
			MemorySection ms = (MemorySection) plugin.getConfig().get("Groups");
			for (String group : ms.getKeys(false)){
				groups.add(group);
			}
		}else{
			log.severe(logPrefix + "Group values NOT found! Please change config.yml and restart server!");
			plugin.getPluginLoader().disablePlugin(plugin);
			return;
		}

		time = plugin.getConfig().getInt("Time");
		String ms = plugin.getConfig().getString("Measure");
		expiredCheckInterval = plugin.getConfig().getInt("ExpiredCheckInterval", 3);

		/* Vault Configs */
		useVault = plugin.getConfig().getBoolean("UseVault", false);

		/* MySQL Configs */
		mysqlAddress = plugin.getConfig().getString("MySQL.Server.Address", "localhost");
		mysqlPort = plugin.getConfig().getInt("MySQL.Server.Port", 3306);
		mysqlDBName = plugin.getConfig().getString("MySQL.Database.Name", "DatabaseName");
		mysqlUserName = plugin.getConfig().getString("MySQL.Database.User_Name", "UserName");
		mysqlUserPass = plugin.getConfig().getString("MySQL.Database.User_Password", "UserPassword");
		mysqlTablePrefix = plugin.getConfig().getString("MySQL.Database.TablePrefix", "sg_");

		// デフォルト拒否
		if (mysqlAddress == "localhost" && mysqlPort == 3306 && mysqlDBName == "DatabaseName" && mysqlUserName == "UserName" && mysqlUserPass == "UserPassword" && mysqlTablePrefix == "sg_"){
			log.severe(logPrefix + "MySQL values NOT configured! Please change config.yml and restart server!");
			plugin.getPluginLoader().disablePlugin(plugin);
			return;
		}

		// check Vault
		if (!initialLoad && useVault && (plugin.getVault() == null || plugin.getEconomy() == null)){
			plugin.setupVault();
		}

		// convert measure
		measure = Util.getMeasure(ms);
		if (measure == -1){
			log.warning(logPrefix+ "Time measure NOT defined properly! Use default: DAY");
			measure = Calendar.DAY_OF_MONTH;
		}
	}

	// 設定 getter ここから
	/* Basic Configs */
	public String getDefGroup(){
		return this.defaultGroup;
	}
	public List<String> getGroups(){
		return this.groups;
	}
	public Double getGroupCost(String groupName){
		return plugin.getConfig().getDouble("Groups." + groupName + ".Cost", -1.0D);
	}
	public Double getGroupKeepCost(String groupName){
		return plugin.getConfig().getDouble("Groups." + groupName + ".KeepCost", -1.0D);
	}
	public String getGroupColor(String groupName){
		return plugin.getConfig().getString("Groups." + groupName + ".ColorTag", "");
	}
	public int getGroupLimit(String groupName){
		return plugin.getConfig().getInt("Groups." + groupName + ".Limit", 0);
	}
	public int getTime(){
		return this.time;
	}
	public int getMeasure(){
		return this.measure;
	}
	public int getExpiredCheckInterval(){
		return this.expiredCheckInterval;
	}

	/* Vault Config */
	public boolean getUseVault(){
		return this.useVault;
	}
	public void setUseVault(boolean bool){
		this.useVault = bool;
	}
	/* MySQL Configs */
	public String getMySQLaddress(){
		return this.mysqlAddress;
	}
	public int getMySQLport(){
		return this.mysqlPort;
	}
	public String getMySQLdbname(){
		return this.mysqlDBName;
	}
	public String getMySQLusername(){
		return this.mysqlUserName;
	}
	public String getMySQLuserpass(){
		return this.mysqlUserPass;
	}
	public String getMySQLtablePrefix(){
		return this.mysqlTablePrefix;
	}

	// 設定 getter ここまで

	/**
	 * 設定ファイルに設定を書き込む (コメントが消えるため使わない)
	 * @throws Exception
	 */
	public void save() throws Exception{
		plugin.saveConfig();
	}

	/**
	 * 必要なディレクトリ群を作成する
	 */
	private void createDirs(){
		createDir(plugin.getDataFolder());
	}

	/**
	 * 存在しないディレクトリを作成する
	 * @param dir File 作成するディレクトリ
	 */
	private static void createDir(File dir) {
		// 既に存在すれば作らない
		if (dir.isDirectory()){
			return;
		}
		if (!dir.mkdir()){
			log.warning(logPrefix+ "Can't create directory: "+dir.getName());
		}
	}

	/**
	 * 設定ファイルのバージョンをチェックする
	 * @param ver
	 */
	private void checkver(final double ver){
		double configVersion = ver;
		double nowVersion = 0.1D;

		String versionString = plugin.getDescription().getVersion();
		try{
			// Support maven and Jenkins build number
			int index = versionString.indexOf("-");
			if (index > 0){
				versionString = versionString.substring(0, index);
			}
			nowVersion = Double.parseDouble(versionString);
		}catch (NumberFormatException ex){
			log.warning(logPrefix+ "Cannot parse version string!");
		}

		// 比較 設定ファイルのバージョンが古ければ config.yml を上書きする
		if (configVersion < nowVersion){
			// 先に古い設定ファイルをリネームする
			String destName = "oldconfig-v"+configVersion+".yml";
			String srcPath = new File(pluginDir, "config.yml").getPath();
			String destPath = new File(pluginDir, destName).getPath();
			try{
				copyTransfer(srcPath, destPath);
				log.info(logPrefix+ "Copied old config.yml to "+destName+"!");
			}catch(Exception ex){
				log.warning(logPrefix+ "Cannot copy old config.yml!");
			}

			// config.ymlを強制コピー
			extractResource("/config.yml", pluginDir, true, false);

			log.info(logPrefix+ "Deleted existing configuration file and generate a new one!");
		}
	}

	/**
	 * リソースファイルをファイルに出力する
	 * @param from 出力元のファイルパス
	 * @param to 出力先のファイルパス
	 * @param force jarファイルの更新日時より新しいファイルが既にあっても強制的に上書きするか
	 * @param checkenc 出力元のファイルを環境によって適したエンコードにするかどうか
	 * @author syam
	 */
	static void extractResource(String from, File to, boolean force, boolean checkenc){
		File of = to;

		// ファイル展開先がディレクトリならファイルに変換、ファイルでなければ返す
		if (to.isDirectory()){
			String filename = new File(from).getName();
			of = new File(to, filename);
		}else if(!of.isFile()){
			log.warning(logPrefix+ "not a file:" + of);
			return;
		}

		// ファイルが既に存在する場合は、forceフラグがtrueでない限り展開しない
		if (of.exists() && !force){
			return;
		}

		OutputStream out = null;
		InputStream in = null;
		InputStreamReader reader = null;
		OutputStreamWriter writer =null;
		DataInputStream dis = null;
		try{
			// jar内部のリソースファイルを取得
			URL res = SakuraGroup.class.getResource(from);
			if (res == null){
				log.warning(logPrefix+ "Can't find "+ from +" in plugin Jar file");
				return;
			}
			URLConnection resConn = res.openConnection();
			resConn.setUseCaches(false);
			in = resConn.getInputStream();

			if (in == null){
				log.warning(logPrefix+ "Can't get input stream from " + res);
			}else{
				// 出力処理 ファイルによって出力方法を変える
				if (checkenc){
					// 環境依存文字を含むファイルはこちら環境

					reader = new InputStreamReader(in, "UTF-8");
					writer = new OutputStreamWriter(new FileOutputStream(of)); // 出力ファイルのエンコードは未指定 = 自動で変わるようにする

					int text;
					while ((text = reader.read()) != -1){
						writer.write(text);
					}
				}else{
					// そのほか

					out = new FileOutputStream(of);
					byte[] buf = new byte[1024]; // バッファサイズ
					int len = 0;
					while((len = in.read(buf)) >= 0){
						out.write(buf, 0, len);
					}
				}
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}finally{
			// 後処理
			try{
				if (out != null)
					out.close();
				if (in != null)
					in.close();
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();
			}catch (Exception ex){}
		}
	}

	/**
	 * コピー元のパス[srcPath]から、コピー先のパス[destPath]へファイルのコピーを行います。
	 * コピー処理にはFileChannel#transferToメソッドを利用します。
	 * コピー処理終了後、入力・出力のチャネルをクローズします。
	 * @param srcPath コピー元のパス
	 * @param destPath  コピー先のパス
	 * @throws IOException 何らかの入出力処理例外が発生した場合
	 */
	public static void copyTransfer(String srcPath, String destPath) throws IOException {
		FileChannel srcChannel = new FileInputStream(srcPath).getChannel();
		FileChannel destChannel = new FileOutputStream(destPath).getChannel();
		try {
			srcChannel.transferTo(0, srcChannel.size(), destChannel);
		} finally {
			srcChannel.close();
			destChannel.close();
		}
	}

	public static File getJarFile(){
		return new File("plugins", "SakuraGroup.jar");
	}
}

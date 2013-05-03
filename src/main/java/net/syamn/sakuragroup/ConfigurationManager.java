/**
 * SakuraGroup - Package: net.syamn.sakuragroup Created: 2012/10/16 3:20:21
 */
package net.syamn.sakuragroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.syamn.sakuragroup.utils.plugin.PUtil;
import net.syamn.utils.LogUtil;
import net.syamn.utils.file.FileStructure;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * ConfigurationManager (ConfigurationManager.java)
 * 
 * @author syam(syamn)
 */
public class ConfigurationManager {
    private static final String msgPrefix = SakuraGroup.msgPrefix;

    private SakuraGroup plugin;
    private FileConfiguration conf;

    private static File pluginDir = new File("plugins", "SakuraGroup");

    // デフォルトの設定定数
    // private final ArrayList<String> availableGroups = new ArrayList<String>()
    // {{add("Citizen"); add("Builder"); add("Engineer"); add("Designer");}};

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
     * 
     * @param plugin
     */
    public ConfigurationManager(final SakuraGroup plugin) {
        this.plugin = plugin;
        pluginDir = this.plugin.getDataFolder();
    }

    /**
     * 設定をファイルから読み込む
     * 
     * @param initialLoad
     *            初回ロードかどうか
     */
    public void loadConfig(boolean initialLoad) throws Exception {
        // ディレクトリ作成
        createDirs();

        // 設定ファイルパス取得
        File file = new File(pluginDir, "config.yml");
        // 無ければデフォルトコピー
        if (!file.exists()) {
            FileStructure.extractResource("/config.yml", pluginDir, false, true, plugin);
            LogUtil.info("config.yml is not found! Created default config.yml!");
        }

        plugin.reloadConfig();

        // 先にバージョンチェック
        double version = plugin.getConfig().getDouble("Version", 0.1D);
        checkver(version);

        /* Basic Configs */
        defaultGroup = plugin.getConfig().getString("DefaultGroup");
        groups.clear();
        if (plugin.getConfig().get("Groups") != null) {
            MemorySection ms = (MemorySection) plugin.getConfig().get("Groups");
            for (String group : ms.getKeys(false)) {
                groups.add(group);
            }
        } else {
            LogUtil.severe("Group values NOT found! Please change config.yml and restart server!");
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
        if (mysqlAddress == "localhost" && mysqlPort == 3306 && mysqlDBName == "DatabaseName" && mysqlUserName == "UserName" && mysqlUserPass == "UserPassword" && mysqlTablePrefix == "sg_") {
            LogUtil.severe("MySQL values NOT configured! Please change config.yml and restart server!");
            plugin.getPluginLoader().disablePlugin(plugin);
            return;
        }

        // check Vault
        if (!initialLoad && useVault && (plugin.getVault() == null || plugin.getEconomy() == null)) {
            plugin.setupVault();
        }

        // convert measure
        measure = PUtil.getMeasure(ms);
        if (measure == -1) {
            LogUtil.warning("Time measure NOT defined properly! Use default: DAY");
            measure = Calendar.DAY_OF_MONTH;
        }
    }

    // 設定 getter ここから
    /* Basic Configs */
    public String getDefGroup() {
        return this.defaultGroup;
    }

    public List<String> getGroups() {
        return this.groups;
    }

    public Double getGroupCost(String groupName) {
        return plugin.getConfig().getDouble("Groups." + groupName + ".Cost", -1.0D);
    }

    public Double getGroupKeepCost(String groupName) {
        return plugin.getConfig().getDouble("Groups." + groupName + ".KeepCost", -1.0D);
    }

    public String getGroupColor(String groupName) {
        return plugin.getConfig().getString("Groups." + groupName + ".ColorTag", "");
    }

    public int getGroupLimit(String groupName) {
        return plugin.getConfig().getInt("Groups." + groupName + ".Limit", 0);
    }

    public int getTime() {
        return this.time;
    }

    public int getMeasure() {
        return this.measure;
    }

    public int getExpiredCheckInterval() {
        return this.expiredCheckInterval;
    }

    /* Vault Config */
    public boolean getUseVault() {
        return this.useVault;
    }

    public void setUseVault(boolean bool) {
        this.useVault = bool;
    }

    /* MySQL Configs */
    public String getMySQLaddress() {
        return this.mysqlAddress;
    }

    public int getMySQLport() {
        return this.mysqlPort;
    }

    public String getMySQLdbname() {
        return this.mysqlDBName;
    }

    public String getMySQLusername() {
        return this.mysqlUserName;
    }

    public String getMySQLuserpass() {
        return this.mysqlUserPass;
    }

    public String getMySQLtablePrefix() {
        return this.mysqlTablePrefix;
    }

    // 設定 getter ここまで

    /**
     * 設定ファイルに設定を書き込む (コメントが消えるため使わない)
     * 
     * @throws Exception
     */
    public void save() throws Exception {
        plugin.saveConfig();
    }

    /**
     * 必要なディレクトリ群を作成する
     */
    private void createDirs() {
        createDir(plugin.getDataFolder());
    }

    /**
     * 存在しないディレクトリを作成する
     * 
     * @param dir
     *            File 作成するディレクトリ
     */
    private static void createDir(File dir) {
        // 既に存在すれば作らない
        if (dir.isDirectory()) {
            return;
        }
        if (!dir.mkdir()) {
            LogUtil.warning("Can't create directory: " + dir.getName());
        }
    }

    /**
     * 設定ファイルのバージョンをチェックする
     * 
     * @param ver
     */
    private void checkver(final double ver) {
        double configVersion = ver;
        double nowVersion = 0.1D;

        String versionString = plugin.getDescription().getVersion();
        try {
            // Support maven and Jenkins build number
            int index = versionString.indexOf("-");
            if (index > 0) {
                versionString = versionString.substring(0, index);
            }
            nowVersion = Double.parseDouble(versionString);
        } catch (NumberFormatException ex) {
            LogUtil.warning("Cannot parse version string!");
        }

        // 比較 設定ファイルのバージョンが古ければ config.yml を上書きする
        if (configVersion < nowVersion) {
            // 先に古い設定ファイルをリネームする
            String destName = "oldconfig-v" + configVersion + ".yml";
            String srcPath = new File(pluginDir, "config.yml").getPath();
            String destPath = new File(pluginDir, destName).getPath();
            try {
                FileStructure.copyTransfer(srcPath, destPath);
                LogUtil.info("Copied old config.yml to " + destName + "!");
            } catch (Exception ex) {
                LogUtil.warning("Cannot copy old config.yml!");
            }

            // config.ymlを強制コピー
            FileStructure.extractResource("/config.yml", pluginDir, true, false, plugin);

            LogUtil.info("Deleted existing configuration file and generate a new one!");
        }
    }
}

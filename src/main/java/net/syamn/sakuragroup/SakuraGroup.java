/**
 * SakuraGroup - Package: net.syamn.sakuragroup Created: 2012/10/16 2:13:44
 */
package net.syamn.sakuragroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.syamn.sakuragroup.command.BaseCommand;
import net.syamn.sakuragroup.command.ChangeAllCommand;
import net.syamn.sakuragroup.command.ChangeCommand;
import net.syamn.sakuragroup.command.ConfirmCommand;
import net.syamn.sakuragroup.command.ForceCheckCommand;
import net.syamn.sakuragroup.command.HelpCommand;
import net.syamn.sakuragroup.command.InfoCommand;
import net.syamn.sakuragroup.command.LeaveCommand;
import net.syamn.sakuragroup.command.ListCommand;
import net.syamn.sakuragroup.command.PayCommand;
import net.syamn.sakuragroup.command.ReloadCommand;
import net.syamn.sakuragroup.database.Database;
import net.syamn.sakuragroup.listener.SignListener;
import net.syamn.sakuragroup.listener.SignProtectListener;
import net.syamn.sakuragroup.manager.PEXManager;
import net.syamn.sakuragroup.permission.Perms;
import net.syamn.sakuragroup.task.TaskHandler;
import net.syamn.utils.LogUtil;
import net.syamn.utils.Metrics;
import net.syamn.utils.SakuraLib;
import net.syamn.utils.economy.EconomyUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SakuraGroup (SakuraGroup.java)
 * 
 * @author syam(syamn)
 */
public class SakuraGroup extends JavaPlugin {
    // ** Log prefix **
    public final static String msgPrefix = "&c[SakuraGroup] &f";

    // ** Listener **
    SignListener signListener = new SignListener(this);
    SignProtectListener protectListener = new SignProtectListener(this);

    // ** Commands **
    private List<BaseCommand> commands = new ArrayList<BaseCommand>();

    // ** Private Classes **
    private ConfigurationManager config;

    // ** Static Variable **
    private static Database database;

    // ** Instance **
    private static SakuraGroup instance;

    // ** Hookup Plugins **
    private static Vault vault = null;
    private static Economy economy = null;

    // ** Manager **/
    private PEXManager pexm = null;
    private TaskHandler taskHandler = null;

    /**
     * プラグイン起動処理
     */
    @Override
    public void onEnable() {
        instance = this;
        LogUtil.init(this);
        
        PluginManager pm = getServer().getPluginManager();
        config = new ConfigurationManager(this);

        // loadconfig
        try {
            config.loadConfig(true);
        } catch (Exception ex) {
            LogUtil.warning("an error occured while trying to load the config file.");
            ex.printStackTrace();
        }

        // プラグインフック
        pexm = new PEXManager(this); // setup pex

        if (config.getUseVault()) {
            config.setUseVault(setupVault());
        }

        // プラグインを無効にした場合進まないようにする
        if (!pm.isPluginEnabled(this)) {
            return;
        }

        // 権限ハンドラセットアップ
        Perms.setupPermissionHandler();

        // Regist Listeners
        pm.registerEvents(signListener, this);
        pm.registerEvents(protectListener, this);

        // コマンド登録
        registerCommands();

        // データベース接続
        database = new Database(this);
        database.createStructure();

        // Timer
        taskHandler = TaskHandler.getInstance(this);
        taskHandler.scheduleStart();

        // メッセージ表示
        PluginDescriptionFile pdfFile = this.getDescription();
        LogUtil.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is enabled!");

        setupMetrics(); // mcstats
    }

    /**
     * プラグイン停止処理
     */
    @Override
    public void onDisable() {
        if (taskHandler != null) {
            taskHandler.scheduleStop();
        }
        getServer().getScheduler().cancelTasks(this);

        // メッセージ表示
        PluginDescriptionFile pdfFile = this.getDescription();
        LogUtil.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is disabled!");
    }

    /**
     * コマンドを登録
     */
    private void registerCommands() {
        // Intro Commands
        commands.add(new HelpCommand());
        commands.add(new ConfirmCommand());

        // General Commands
        commands.add(new ListCommand());
        commands.add(new ChangeCommand());
        commands.add(new LeaveCommand());
        commands.add(new InfoCommand());
        commands.add(new PayCommand());

        // Admin Commands
        commands.add(new ChangeAllCommand());
        commands.add(new ForceCheckCommand());
        commands.add(new ReloadCommand());
    }

    /**
     * Vaultプラグインにフック
     */
    public boolean setupVault() {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("Vault");
        if (plugin != null && plugin instanceof Vault) {
            RegisteredServiceProvider<Economy> econProv = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (econProv != null){
                SakuraLib.setEconomy(econProv.getProvider());
                LogUtil.info("Enabled economy hookup! Using Vault (" + EconomyUtil.getEconomyName() + ") for economy plugin!");
                return true;
            }else{
                LogUtil.warning("Could not hook to economy plugin!");
            }
        }else{
            LogUtil.warning("Vault plugin was NOT found! Disabled Vault integration.");
        }
        return false;
    }

    /**
     * Metricsセットアップ
     */
    private void setupMetrics() {
        try {
            Metrics metrics = new net.syamn.utils.Metrics(this);
            metrics.start();
        } catch (IOException ex) {
            LogUtil.warning("cant send metrics data!");
            ex.printStackTrace();
        }
    }

    /**
     * コマンドが呼ばれた
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
        if (cmd.getName().equalsIgnoreCase("sakuragroup")) {
            if (args.length == 0) {
                // 引数ゼロはヘルプ表示
                args = new String[] { "help" };
            }

            outer: for (BaseCommand command : commands.toArray(new BaseCommand[0])) {
                String[] cmds = command.name.split(" ");
                for (int i = 0; i < cmds.length; i++) {
                    if (i >= args.length || !cmds[i].equalsIgnoreCase(args[i])) {
                        continue outer;
                    }
                    // 実行
                    return command.run(this, sender, args, commandLabel);
                }
            }
            // 有効コマンドなし ヘルプ表示
            new HelpCommand().run(this, sender, args, commandLabel);
            return true;
        }
        return false;
    }

    /* getter */
    /**
     * コマンドを返す
     * 
     * @return List<BaseCommand>
     */
    public List<BaseCommand> getCommands() {
        return commands;
    }
    
    /**
     * Vaultを返す
     * 
     * @return Vault
     */
    public Vault getVault() {
        return vault;
    }

    /**
     * Economyを返す
     * 
     * @return Economy
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * データベースを返す
     * 
     * @return Database
     */
    public static Database getDatabases() {
        return database;
    }

    /**
     * PermissionsExマネージャを返す
     * 
     * @return PEXManager
     */
    public PEXManager getPEXmgr() {
        return pexm;
    }

    /**
     * TaskHandlerを返す
     * 
     * @return TaskHandler
     */
    public TaskHandler getTaskHandler() {
        return taskHandler;
    }

    /**
     * 設定マネージャを返す
     * 
     * @return ConfigurationManager
     */
    public ConfigurationManager getConfigs() {
        return config;
    }

    /**
     * インスタンスを返す
     * 
     * @return Likesインスタンス
     */
    public static SakuraGroup getInstance() {
        return instance;
    }
}

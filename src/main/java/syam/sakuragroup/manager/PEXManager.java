/**
 * SakuraGroup - Package: syam.sakuragroup.manager
 * Created: 2012/10/16 19:52:05
 */
package syam.sakuragroup.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import syam.sakuragroup.SakuraGroup;
import syam.sakuragroup.enumeration.Group;
import syam.sakuragroup.util.Actions;

/**
 * PEXManager (PEXManager.java)
 * @author syam(syamn)
 */
public class PEXManager {
	// Logger
	private static final Logger log = SakuraGroup.log;
	private static final String logPrefix = SakuraGroup.logPrefix;
	private static final String msgPrefix = SakuraGroup.msgPrefix;

	// PEX
	private PermissionsEx pex = null;
	private PermissionManager pm = null;

	private List<String> pexgroups = new ArrayList<String>(); // exists pex groups
	private List<String> groups = new ArrayList<String>();

	private final SakuraGroup plugin;
	public PEXManager(final SakuraGroup plugin){
		this.plugin = plugin;

		// setup pex
		if (!setupPEX()){
			return;
		}

		loadGroups();
	}

	/**
	 * グループを変更する
	 * @param name
	 * @param groupName
	 * @return
	 */
	public boolean changeGroup(String name, String groupName, CommandSender sender){
		PermissionGroup group = pm.getGroup(groupName);
		PermissionGroup[] setGroup = {group};
		pex.getUser(name).setGroups(setGroup);

		if (sender != null){
			Actions.message(sender, "&6" + name + " &aを&6" + group.getName() + "&aに変更しました！");
		}

		return true;
	}

	public List<String> getPlayersByGroup(String groupName){
		List<String> names = new ArrayList<String>();
		for (PermissionUser user : getUsersByGroup(groupName)){
			names.add(user.getName());
		}
		return names;
	}

	public PermissionUser[] getUsersByGroup(String groupName){
		PermissionGroup group = pm.getGroup(groupName);
		return group.getUsers();
	}

	/**
	 * グループリストを再構築する
	 */
	public void loadGroups(){
		// Building all exists
		pexgroups.clear();
		for (PermissionGroup group : pm.getGroups()){
			pexgroups.add(group.getName());
		}

		// Building all availables
		groups.clear();
		for (String name : plugin.getConfigs().getGroups()){
			if (!pexgroups.contains(name)){
				log.warning(logPrefix+ "Group NOT exist! Disabled group: "+ name);
				continue;
			}
			groups.add(name);
		}
	}

	public List<String> getAvailables(){
		return this.groups;
	}

	/**
	 * PermissionsExを返す
	 * @return PermissionsEx
	 */
	public PermissionsEx getPEX(){
		return pex;
	}
	/**
	 * PermissionsManagerを返す
	 * @return PermissionsManager
	 */
	public PermissionManager getPM(){
		return pm;
	}

	/**
	 * PermissionsExセットアップ
	 * @return
	 */
	private boolean setupPEX(){
		Plugin pexTest = plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
		if (pexTest == null) pexTest = plugin.getServer().getPluginManager().getPlugin("permissionsex");
		if (pexTest == null) return false;
		try{
			pex = (PermissionsEx) pexTest;
			pm = PermissionsEx.getPermissionManager();
		}catch (Exception ex){
			log.warning(logPrefix+ "PermissionsEx plugin NOT found. Disabling plugin..");
			ex.printStackTrace();
			plugin.getPluginLoader().disablePlugin(plugin);
			return false;
		}

		// Success
		log.info(logPrefix+ "Hooked to PermissionsEx plugin!");
		return true;
	}
}

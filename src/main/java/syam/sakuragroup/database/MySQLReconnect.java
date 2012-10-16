/**
 * SakuraGroup - Package: syam.sakuragroup.database
 * Created: 2012/10/16 4:12:59
 */
package syam.sakuragroup.database;

import org.bukkit.entity.Player;

import syam.sakuragroup.SakuraGroup;
import syam.sakuragroup.manager.PlayerManager;

/**
 * MySQLReconnect (MySQLReconnect.java)
 * @author syam(syamn)
 */
public class MySQLReconnect implements Runnable{
	private final SakuraGroup plugin;

	public MySQLReconnect(final SakuraGroup plugin){
		this.plugin = plugin;
	}

	@Override
	public void run(){
		if (!Database.isConnected()){
			Database.connect();
			if (Database.isConnected()){
				PlayerManager.saveAllProfiles();
				PlayerManager.clearAllProfiles();

				for (Player player : plugin.getServer().getOnlinePlayers()){
					PlayerManager.addPlayerProfile(player);
				}
			}
		}
	}
}

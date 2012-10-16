/**
 * SakuraGroup - Package: syam.sakuragroup.listener
 * Created: 2012/10/16 5:44:45
 */
package syam.sakuragroup.listener;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import syam.sakuragroup.SakuraGroup;
import syam.sakuragroup.manager.PlayerManager;

/**
 * PlayerListener (PlayerListener.java)
 * @author syam(syamn)
 */
public class PlayerListener implements Listener {
	public final static Logger log = SakuraGroup.log;
	private static final String logPrefix = SakuraGroup.logPrefix;
	private static final String msgPrefix = SakuraGroup.msgPrefix;

	private final SakuraGroup plugin;

	public PlayerListener(final SakuraGroup plugin){
		this.plugin = plugin;
	}

	// プレイヤーがログインしようとした
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLogin(final PlayerLoginEvent event){
		// プレイヤー追加
		PlayerManager.addPlayer(event.getPlayer());
	}

	// プレイヤーがログアウトした
	//@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerQuit(final PlayerQuitEvent event){
		Player player = event.getPlayer();
		/* TODO: Do GC here */
	}
}

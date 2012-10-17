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
}

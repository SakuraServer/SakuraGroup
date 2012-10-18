/**
 * SakuraGroup - Package: syam.sakuragroup.listener
 * Created: 2012/10/19 0:52:05
 */
package syam.sakuragroup.listener;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import syam.sakuragroup.SakuraGroup;
import syam.sakuragroup.permission.Perms;

/**
 * SignProtectListener (SignProtectListener.java)
 * @author syam(syamn)
 */
public class SignProtectListener implements Listener{
	public final static Logger log = SakuraGroup.log;
	private static final String logPrefix = SakuraGroup.logPrefix;
	private static final String msgPrefix = SakuraGroup.msgPrefix;

	private final SakuraGroup plugin;

	public SignProtectListener(final SakuraGroup plugin){
		this.plugin = plugin;
	}

	/* Block Listener */
	// ブロックを破壊した
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event){
		if (protectBlock(event.getBlock(), event.getPlayer())){
			event.setCancelled(true);
		}
	}
	// ピストンを展開した
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPistonExtend(final BlockPistonExtendEvent event){
		for (Block block : event.getBlocks()){
			if (protectBlock(block)){
				event.setCancelled(true);
			}
		}
	}
	// ピストンを格納した
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPistonRetract(final BlockPistonRetractEvent event){
		if (event.isSticky() && protectBlock(event.getBlock())){
			event.setCancelled(true);
		}
	}
	// ブロックが消失した
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockBurn(final BlockBurnEvent event){
		if (protectBlock(event.getBlock())){
			event.setCancelled(true);
		}
	}
	// ブロックに着火した
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockIgnite(final BlockIgniteEvent event){
		if (protectBlock(event.getBlock())){
			event.setCancelled(true);
		}
	}

	/* Entity Listener */
	// ブロックが爆発した
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityExplode(final EntityExplodeEvent event){
		for (Block block : event.blockList()){
			if (protectBlock(block)){
				event.setCancelled(true);
				return;
			}
		}
	}
	// エンティティがブロックを変えた
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityChangeBlock(final EntityChangeBlockEvent event){
		if (protectBlock(event.getBlock())){
			event.setCancelled(true);
		}
	}


	/* ********** */
	private boolean protectBlock(final Block block, final Player player){
		if (player != null && Perms.PLACESIGN.has(player)){
			return false;
		}

		// 看板ブロック
		if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN){
			if (((Sign) block.getState()).getLine(0).equals("§a[SakuraGroup]")){
				return true;
			}
		}

		// 一般ブロック
		// 上ブロックチェック
		Block check = block.getRelative(BlockFace.UP);
		if (check.getType() == Material.SIGN_POST){
			if (((Sign) check.getState()).getLine(0).equals("§a[SakuraGroup]")){
				return true;
			}
		}
		// 周囲ブロックチェック
		final BlockFace[] directions = new BlockFace[]{
			BlockFace.NORTH,
			BlockFace.EAST,
			BlockFace.SOUTH,
			BlockFace.WEST
		};
		for (BlockFace face : directions){
			check = block.getRelative(face);
			if (check.getType() == Material.WALL_SIGN){
				org.bukkit.material.Sign signMat = (org.bukkit.material.Sign) check.getState().getData();
				if (signMat != null && signMat.getFacing() == face){
					if (((Sign) check.getState()).getLine(0).equals("§a[SakuraGroup]")){
						return true;
					}
				}
			}
		}

		return false;
	}
	private boolean protectBlock(final Block block){
		return this.protectBlock(block, null);
	}
}

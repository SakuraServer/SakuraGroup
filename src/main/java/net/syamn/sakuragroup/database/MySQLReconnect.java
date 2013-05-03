/**
 * SakuraGroup - Package: net.syamn.sakuragroup.database
 * Created: 2012/10/16 4:12:59
 */
package net.syamn.sakuragroup.database;

import net.syamn.sakuragroup.SakuraGroup;

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
				// TODO: do stuff..
			}
		}
	}
}

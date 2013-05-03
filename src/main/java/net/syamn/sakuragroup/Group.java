/**
 * SakuraGroup - Package: net.syamn.sakuragroup
 * Created: 2012/10/18 5:33:55
 */
package net.syamn.sakuragroup;

import java.util.logging.Logger;

/**
 * Group (Group.java)
 * @author syam(syamn)
 */
public class Group {
	// Logger
	private static final Logger log = SakuraGroup.log;
	private static final String logPrefix =SakuraGroup.logPrefix;
	private static final String msgPrefix = SakuraGroup.msgPrefix;

	private String groupName; // グループ名
	private double cost; // 参加コスト
	private double keepcost; // 維持コスト
	private String colorTag; // カラータグ

	//private final SakuraGroup plugin;
	/**
	 * コンストラクタ
	 * @param plugin
	 * @param name
	 * @param cost
	 * @param color
	 */
	public Group (final String name, final double cost, final double keepcost, final String color){
		//this.plugin = plugin;
		this.groupName = name;
		this.cost = cost;
		this.keepcost = keepcost;
		this.colorTag = color;
	}

	public String getName(){
		return this.groupName;
	}

	public double getCost(){
		return this.cost;
	}

	public double getKeepCost(){
		return this.keepcost;
	}

	public String getColor(){
		return this.colorTag;
	}
}

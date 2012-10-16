/**
 * SakuraGroup - Package: syam.sakuragroup.enumeration
 * Created: 2012/10/16 7:18:40
 */
package syam.sakuragroup.enumeration;

/**
 * Group (Group.java)
 * @author syam(syamn)
 */
public enum Group {
	DEFAULT (0, "Citizen"),
	BUILDER (1, "Builder"),
	ENGINEER (2, "Engineer"),
	DESIGNER (3, "Designer"),
	;

	private int id;
	private String name;

	Group(int groupID, String name){
		this.id = groupID;
		this.name = name;
	}

	/**
	 * グループIDを返す
	 * @return
	 */
	public int getID(){
		return this.id;
	}

	/**
	 * グループ名を返す
	 * @return
	 */
	public String getName(){
		return this.name;
	}
}

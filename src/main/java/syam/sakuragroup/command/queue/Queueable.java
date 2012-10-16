/**
 * SakuraGroup - Package: syam.sakuragroup.command.queue
 * Created: 2012/10/16 3:20:59
 */
package syam.sakuragroup.command.queue;

import java.util.List;

/**
 * Queueable (Queueable.java)
 * @author syam(syamn)
 */
public interface Queueable {
	void executeQueue(List<String> args);
}

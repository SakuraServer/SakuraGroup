/**
 * SakuraGroup - Package: net.syamn.sakuragroup.exception
 * Created: 2012/10/16 3:19:00
 */
package net.syamn.sakuragroup.exception;

/**
 * CommandException (CommandException.java)
 * @author syam(syamn)
 */
public class CommandException extends Exception{
	private static final long serialVersionUID = 756337721456853035L;

	public CommandException(String message){
		super(message);
	}

	public CommandException(Throwable cause){
		super(cause);
	}

	public CommandException(String message, Throwable cause){
		super(message, cause);
	}
}

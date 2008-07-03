package endrov.basicWindow;


/**
 * Template for hook for extension to BasicWindow (one hook per extension and window)
 * @author Johan Henriksson
 */
public interface BasicWindowHook
	{
	public abstract void createMenus(BasicWindow w);
	public abstract void buildMenu(BasicWindow w);
	}

package evplugin.ev;

/**
 * Special case of GeneralObserver no argument need be passed to the listeners
 * 
 * @author Johan Henriksson
 */
public class SimpleObserver extends GeneralObserver<SimpleObserver.Listener>
	{
	public static interface Listener
		{
		public void observerEvent();
		}
	
	/**
	 * Emit signal to all observers
	 */
	public void emit()
		{
		for(Listener l:getListeners())
			l.observerEvent();
		}
	}


/*
public class SimpleObserver extends GeneralObserver<SimpleObserver.Listener>
{
public static interface Listener
	{
	public void observerEvent(Object o);
	}

public void emit(Object o)
	{
	for(Listener l:getListeners())
		l.observerEvent(o);
	}
}
*/
/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ev;

/**
 * Special case of GeneralObserver with just source (or null) sent to observers. Or anything
 * 
 * @author Johan Henriksson
 */
public class SimpleObserver extends GeneralObserver<SimpleObserver.Listener>
	{
	public static interface Listener
		{
		public void observerEvent(Object o);
		}
	
	/**
	 * Emit signal to all observers
	 */
	public void emit(Object o)
		{
		for(Listener l:getListeners())
			l.observerEvent(o);
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
/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware;

import endrov.ev.GeneralObserver;

/**
 * 
 * 
 * @author Johan Henriksson
 */
public class DeviceObserver extends GeneralObserver<DeviceObserver.Listener>
	{
	public static interface Listener
		{
		public void propertyChange(Object o);
		}
	
	/**
	 * Emit signal to all observers
	 */
	public void emit(Object o)
		{
		for(Listener l:getListeners())
			l.propertyChange(o);
		}
	}


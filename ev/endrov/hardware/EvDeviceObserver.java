/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware;

import endrov.core.observer.GeneralObserver;

/**
 * Observe state changes in device
 * 
 * @author Johan Henriksson
 */
public class EvDeviceObserver extends GeneralObserver<EvDeviceObserver.DeviceListener>
	{
	public static interface DeviceListener
		{
		public void devicePropertyChange(Object source, EvDevice dev);
		}
	
	/**
	 * Emit signal to all observers
	 */
	public void emit(Object source, EvDevice dev)
		{
		for(DeviceListener l:getListeners())
			l.devicePropertyChange(source, dev);
		}

	}


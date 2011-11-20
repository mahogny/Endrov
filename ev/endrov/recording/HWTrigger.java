/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

import endrov.hardware.*;

/**
 * Hardware with triggering capabilities
 * @author Johan Henriksson
 */
public interface HWTrigger extends EvDevice
	{
	
	public interface TriggerListener
		{
		public void triggered();
		}
	
	public void addTriggerListener(TriggerListener listener);
	public void removeTriggerListener(TriggerListener listener);
	}

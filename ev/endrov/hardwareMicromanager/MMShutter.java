/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareMicromanager;

import endrov.recording.HWShutter;


/**
 * Micro-manager Shutter
 * 
 * is it really needed? it is a 2-state filter the way I see it
 * can reserve names open and closed
 * 
 * @author Johan Henriksson
 *
 */
public class MMShutter extends MMState implements HWShutter
	{

	public MMShutter(MicroManager mm, String mmDeviceName)
		{
		super(mm,mmDeviceName);
		}

	public boolean isOpen()
		{
		try
			{
			mm.core.setShutterDevice(mmDeviceName);
			return mm.core.getShutterOpen();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return false;
			}
		}

	public void setOpen(boolean b)
		{
		try
			{
			mm.core.setShutterDevice(mmDeviceName);
			mm.core.setShutterOpen(b);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	
	
	
	}

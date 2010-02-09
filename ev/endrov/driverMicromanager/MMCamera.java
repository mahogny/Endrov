/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverMicromanager;

import endrov.recording.HWCamera;
import endrov.recording.CameraImage;

/**
 * Micro-manager camera
 * @author Johan Henriksson
 *
 */
public class MMCamera extends MMDeviceAdapter implements HWCamera
	{

	public MMCamera(MicroManager mm, String mmDeviceName)
		{
		super(mm,mmDeviceName);
		}

	public CameraImage snap()
		{
		try
			{
			return MMutil.snap(mm.core);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return null;
			}
		}

	private double getRes()
		{
		return 1;
		//TODO
		//TODO
		//TODO
		}
	
	public double getResMagX()
		{
		return getRes();
		}

	public double getResMagY()
		{
		return getRes();
		}
	
	
	
	
	}

/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverMicromanager;

import java.util.*;

import endrov.recording.*;

//could preload list of properties

//mm virtual property: state. map to setstate


/**
 * Micro manager state device
 * @author Johan Henriksson
 *
 */
public class MMState extends MMDeviceAdapter implements HWState
	{
	public MMState(MicroManager mm, String mmDeviceName)
		{
		super(mm,mmDeviceName);
		
		
		}
	
	public List<String> getStateNames()
		{
		try
			{
			//mm.core.get
	//		System.out.println("propv  "+MMutil.convVector(mm.core.getAllowedPropertyValues(mmDeviceName, "Label")));
			return MMutil.convVector(mm.core.getStateLabels(mmDeviceName));
//			System.out.println(mmDeviceName);
//			return MMutil.convVector(mm.core.getAllowedPropertyValues(mmDeviceName, "Label"));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return Collections.emptyList();
			}
		}
	
	
	
	public int getCurrentState()
		{
		try
			{
			return mm.core.getState(mmDeviceName);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return 0;
			}
		}
	
	public String getCurrentStateLabel()
		{
		try
			{
			return mm.core.getStateLabel(mmDeviceName);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		return null;
		}
	

	
	public void setCurrentState(int state)
		{
		try
			{
			mm.core.setState(mmDeviceName, state);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	public void setCurrentStateLabel(String label)
		{
		try
			{
			mm.core.setStateLabel(mmDeviceName, label);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	private double getRes()
		{
		return RecordingResource.magFromLabel(getCurrentStateLabel());
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

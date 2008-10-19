package endrov.micromanager;

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
	
	
	}

package endrov.recording.mm;

import java.util.*;

import endrov.hardware.*;

//could preload list of properties

//mm virtual property: state. map to setstate


/**
 * Micro manager device adapter, mapped to Endrov hardware
 * @author Johan Henriksson
 *
 */
public class MMDeviceAdapter implements Hardware
	{
	MicroManager mm;
	String mmDeviceName;

	public MMDeviceAdapter(MicroManager mm, String mmDeviceName)
		{
		this.mm=mm;
		this.mmDeviceName=mmDeviceName;
		}
	
	
	public String getDescName()
		{
		try
			{
			if(mmDeviceName.equals("Core"))
				return "uManager virtual device";
			else
				return mm.core.getProperty(mmDeviceName, "Description");
			}
		catch (Exception e)
			{
			return "<mm exception>";
			}
		}

	
	public SortedMap<String, String> getPropertyMap()
		{
		try
			{
			return MMutil.getPropMap(mm.core,mmDeviceName);
			}
		catch (Exception e)
			{
			return new TreeMap<String, String>();
			}
		}

	
	
	public SortedMap<String, PropertyType> getPropertyTypes()
		{
		TreeMap<String, PropertyType> map=new TreeMap<String, PropertyType>();
		try
			{
			for(String propName:MMutil.convVector(mm.core.getDevicePropertyNames(mmDeviceName)))
				{
				PropertyType p=new PropertyType();
				for(String s:MMutil.convVector(mm.core.getAllowedPropertyValues(mmDeviceName, propName)))
					p.categories.add(s);
				map.put(propName,p);
				}
			return map;
			}
		catch (Exception e)
			{
			return new TreeMap<String, PropertyType>();
			}
		}

	
	
	public String getPropertyValue(String prop)
		{
		try
			{
			return mm.core.getProperty(mmDeviceName, prop);
			}
		catch (Exception e)
			{
			return "<mm exception>";
			}
		}
	
	
	
	
	}

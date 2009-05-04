package endrov.hardware;

import java.util.*;


/**
 * One hardware device/session
 * @author Johan Henriksson
 *
 */
public interface Device
	{
	/** Descriptive name of hardware */
	public String getDescName();
		
	
	////// For devices
	
	public SortedMap<String,PropertyType> getPropertyTypes();
	public SortedMap<String,String> getPropertyMap();
	public String getPropertyValue(String prop);
	public Boolean getPropertyValueBoolean(String prop);
	
	public void setPropertyValue(String prop, String value);
	public void setPropertyValue(String prop, boolean value);
	
	}

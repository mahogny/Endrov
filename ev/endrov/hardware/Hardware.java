package endrov.hardware;

import java.util.*;

/**
 * One hardware device/session
 * @author Johan Henriksson
 *
 */
public interface Hardware
	{
	/** Descriptive name of hardware */
	public String getDescName();
		
	
	
	
	public SortedMap<String,PropertyType> getPropertyTypes();
	public SortedMap<String,String> getPropertyMap();
	public String getPropertyValue(String prop);
	public boolean getPropertyValueBoolean(String prop);
	
	public void setPropertyValue(String prop, String value);
	public void setPropertyValue(String prop, boolean value);
	
	}

package endrov.hardware;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.jdom.Element;

public class HardwareRoot extends HardwareProvider implements Hardware
	{

	public Set<Hardware> autodetect(){return null;}

	public void getConfig(Element root)
		{
		}


	public List<String> provides(){return null;}
	public Hardware newProvided(String s){return null;}

	public void setConfig(Element root)
		{
		}

	public String getDescName()
		{
		return "root";
		}

	public SortedMap<String, String> getPropertyMap()
		{
		return null;
		}

	public SortedMap<String, PropertyType> getPropertyTypes()
		{
		return null;
		}

	public String getPropertyValue(String prop)
		{
		return null;
		}

	public boolean getPropertyValueBoolean(String prop)
		{
		return false;
		}

	public void setPropertyValue(String prop, boolean value)
		{
		}

	public void setPropertyValue(String prop, String value)
		{
		}
	
	
	
	}

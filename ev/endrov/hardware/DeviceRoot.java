package endrov.hardware;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.jdom.Element;

/**
 * The special device "root". Doesn't do anything, just there to form a complete tree
 * @author Johan Henriksson
 *
 */
public class DeviceRoot extends DeviceProvider implements Device
	{

	public Set<Device> autodetect(){return null;}

	public void getConfig(Element root)
		{
		}


	public List<String> provides(){return null;}
	public Device newProvided(String s){return null;}

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

	public Boolean getPropertyValueBoolean(String prop)
		{
		return false;
		}

	public void setPropertyValue(String prop, boolean value)
		{
		}

	public void setPropertyValue(String prop, String value)
		{
		}
	
	
	public boolean hasConfigureDialog(){return false;}
	public void openConfigureDialog(){}

	}

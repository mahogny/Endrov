package endrov.hardware;

import java.util.*;
import org.jdom.Element;


/**
 * A provider of other devices
 * @author Johan Henriksson
 *
 */
public abstract class DeviceProvider
	{
	/** Return null if operation not supported 
	 * TODO maybe not return but add it right away?
	 * */
	public abstract Set<Device> autodetect();
	
	public Map<String, Device> hw=new HashMap<String, Device>();
	
	
	
	public abstract void getConfig(Element root);
	public abstract void setConfig(Element root);

	public abstract List<String> provides();
	public abstract Device newProvided(String s); 
	}

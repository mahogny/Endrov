package endrov.hardware;

import java.util.*;
import org.jdom.Element;

/**
 * 
 * @author Johan Henriksson
 *
 */
public abstract class HardwareProvider
	{
	/** Return null if operation not supported */
	public abstract List<Hardware> autodetect();
	
	public Map<String, Hardware> hw=new HashMap<String, Hardware>();
	
	
	public abstract void getConfig(Element root);
	public abstract void setConfig(Element root);

	
	
	
	}

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
	/** Return null if operation not supported 
	 * TODO maybe not return but add it right away?
	 * */
	public abstract Set<Hardware> autodetect();
	
	public Map<String, Hardware> hw=new HashMap<String, Hardware>();
	
	
	
	public abstract void getConfig(Element root);
	public abstract void setConfig(Element root);

	public abstract List<String> provides();
	public abstract Hardware newProvided(String s); 
	}

package endrov.ev;

//import java.util.Vector;
import org.jdom.*;

/**
 * Component that can load/save personalized configuration
 * 
 * @author Johan Henriksson
 *
 */
public interface PersonalConfig
	{
	public void loadPersonalConfig(Element root);
	public void savePersonalConfig(Element root);
	}

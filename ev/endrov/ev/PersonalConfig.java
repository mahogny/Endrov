package endrov.ev;

//import java.util.Vector;
import org.jdom.*;


public interface PersonalConfig
	{
	public void loadPersonalConfig(Element root);
	public void savePersonalConfig(Element root);
	}

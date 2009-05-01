package endrov.driverMicromanager;
import endrov.ev.EV;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Micro-manager";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return EV.isLinux() || (EV.isMac() && EV.isX86());
		}
	
	public String cite()
		{
		return "";
		}
	
	public String[] requires()
		{
		return new String[]{};
		}
	
	public Class<?>[] getInitClasses()
		{
		//return new Class[]{MicroManager.class};
		return new Class[]{};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

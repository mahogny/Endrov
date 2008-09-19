package endrov.recording;
import endrov.ev.EV;
import endrov.ev.PluginDef;
import endrov.recording.mm.MicroManager;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Recording hardware";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return true;
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
		if(EV.isLinux())
			return new Class[]{MicroManager.class};
		else
			return new Class[]{};
		}
	}

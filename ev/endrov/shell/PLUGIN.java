package endrov.shell;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Shell";
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
		return new Class[]{Shell.class};
		}
	}
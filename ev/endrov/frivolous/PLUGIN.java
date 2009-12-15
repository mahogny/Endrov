package endrov.frivolous;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Frivolous hardware drivers";
		}

	public String getAuthor()
		{
		return "David Johansson <jdavid@kth.se>\nArvid Johansson <arvidjo@kth.se>";
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
		return new Class[]{Frivolous.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

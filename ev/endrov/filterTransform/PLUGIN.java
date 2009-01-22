package endrov.filterTransform;
import endrov.ev.PluginDef;


public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Transform filters";
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
		return new Class[]{
				InvertFilter.class, 
				};
		}
	
	public boolean isDefaultEnabled()
		{
		return true;
		}
	}

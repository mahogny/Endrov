package endrov.examplePlugin;
import endrov.ev.PluginDef;

/**
 * Should be named PLUGIN to work
 * @author Johan Henriksson
 *
 */
public class PLUGINfoo extends PluginDef
	{
	public String getPluginName()
		{
		return "Example plugin";
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
		return new Class[]{ExampleObject.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
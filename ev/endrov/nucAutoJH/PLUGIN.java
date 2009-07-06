package endrov.nucAutoJH;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Nucleus/Lineage autolineage/JH";
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
		return new Class[]{AutolineageJH1.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

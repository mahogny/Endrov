package endrov.imagesetBasic;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Importer for basic imageset formats";
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
		return new Class[]{NamebasedImageset.class, SequenceImageset.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

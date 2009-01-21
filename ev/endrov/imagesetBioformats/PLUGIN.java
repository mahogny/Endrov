package endrov.imagesetBioformats;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "LOCI Bioformats";
		}

	public String getAuthor()
		{
		return 
		"LOCI (library)\n" +
		"Johan Henriksson (library binding)";
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
		return new Class[]{EvIODataBioformats.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

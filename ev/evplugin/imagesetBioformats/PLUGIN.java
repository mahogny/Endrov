package evplugin.imagesetBioformats;
import evplugin.ev.PluginDef;

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
		return new Class[]{BioformatsImageset.class};
		}
	}

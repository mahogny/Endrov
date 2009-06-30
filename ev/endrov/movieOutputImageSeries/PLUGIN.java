package endrov.movieOutputImageSeries;
import endrov.ev.*;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Movie output: image series";
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
		return new Class[]{EncodeImageSeries.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

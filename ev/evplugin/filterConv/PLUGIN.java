package evplugin.filterConv;
import evplugin.ev.PluginDef;
import evplugin.filterConv.Convolve2DFilter;
import evplugin.filterConv.Gaussian2DFilter;
import evplugin.filterConv.Sharpen2DFilter;
import evplugin.filterConv.WindowedMean2DFilter;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Convolving filters";
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
				Convolve2DFilter.class, 
				WindowedMean2DFilter.class, 
				Sharpen2DFilter.class,
				Gaussian2DFilter.class
				};
		}
	}

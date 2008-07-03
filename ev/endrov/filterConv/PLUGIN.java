package endrov.filterConv;
import endrov.ev.PluginDef;
import endrov.filterConv.Convolve2DFilter;
import endrov.filterConv.Gaussian2DFilter;
import endrov.filterConv.Sharpen2DFilter;
import endrov.filterConv.WindowedMean2DFilter;

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
				Gaussian2DFilter.class,
				LaplacianOfGaussian2DFilter.class
				};
		}
	}

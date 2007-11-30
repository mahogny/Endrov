package evplugin.filterBasic;
import evplugin.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Basic filters";
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
		return new Class[]{InvertFilter.class, NoisePepperAndSalt.class, NoisePoisson.class,ContrastBrightnessFilter.class, Convolve2DFilter.class, Laplace2DFilter.class,
				EqualizeHistogram.class};
		}
	}

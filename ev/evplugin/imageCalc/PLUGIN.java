package evplugin.imageCalc;
import evplugin.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Image Calculator";
		}
	
	public boolean systemSupported()
		{
		return true;
		}
	
	public String cite()
		{
		return "";
		}
	
	public String getAuthor()
		{
		return "Ricardo Figueroa, and stolen code from Johan Henriksson";
		}

	public String[] requires()
		{
		return new String[]{};
		}
	
	public Class<?>[] getInitClasses()
		{
		return new Class[]{ImageCalcWindow.class};
		}
	}

package evplugin.makeQT;
import evplugin.ev.*;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Make QuickTime Movies";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return EV.isMac() || EV.isWindows();
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
		return new Class[]{MakeQTWindow.class};
		}
	}

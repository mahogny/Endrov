package evplugin.macBinding;
import evplugin.ev.*;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Mac binding";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return EV.isMac();
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
		return new Class[]{OSXAdapter.class};
		}
	}

package endrov.recording;
import endrov.ev.EV;
import endrov.ev.PluginDef;
import endrov.recording.camWindow.CamWindow;
import endrov.recording.manualRec.ManualExtension;
import endrov.recording.mm.MicroManager;
import endrov.recording.recWindow.MicroscopeWindow;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Recording hardware";
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
		if(EV.isLinux())
			return new Class[]{MicroManager.class, MicroscopeWindow.class,ManualExtension.class,CamWindow.class};
		else
			return new Class[]{MicroscopeWindow.class,ManualExtension.class,CamWindow.class};
		}
	}

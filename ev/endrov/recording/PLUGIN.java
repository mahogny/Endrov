package endrov.recording;
import endrov.ev.PluginDef;
import endrov.recording.camWindow.CamWindow;
import endrov.recording.driver.EvNativeHardware;
import endrov.recording.recWindow.MicroscopeWindow;
import endrov.recording.recmetManual.ManualExtension;
import endrov.recording.recmetStack.StackExtension;

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
		return new Class[]{MicroscopeWindow.class,ManualExtension.class,StackExtension.class,CamWindow.class
				,EvNativeHardware.class};
		}
	}

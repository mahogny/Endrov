package endrov.recording;
import endrov.ev.PluginDef;
import endrov.recording.camWindow.CamWindow;
import endrov.recording.lightpathWindow.LightpathWindow;
import endrov.recording.recmetBurst.RecWindowBurst;
import endrov.recording.recmetManual.RecWindowManual;
import endrov.recording.recmetMultidim.RecWindowMultiDim;

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
		return new Class[]{
				RecWindowManual.class,CamWindow.class, RecordingResource.class,
				RecWindowMultiDim.class,RecWindowBurst.class,
				LightpathWindow.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

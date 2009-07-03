package endrov.flow;
import endrov.ev.PluginDef;
import endrov.flow.ui.FlowWindow;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows";
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
		return new Class[]{Flow.class,FlowWindow.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

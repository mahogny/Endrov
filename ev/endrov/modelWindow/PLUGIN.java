package endrov.modelWindow;
import endrov.ev.PluginDef;
import endrov.modelWindow.basicExt.ModelWindowClipPlane;
import endrov.modelWindow.basicExt.ModelWindowGrid;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Model Window";
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
		return new Class[]{ModelWindow.class,ModelWindowClipPlane.class,ModelWindowGrid.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

package endrov.flowGenerateImage;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows: Generate images";
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
				FlowUnitGenGaussian2D.class,
				FlowUnitGenGaussian3D.class,
		};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

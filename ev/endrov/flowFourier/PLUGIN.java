package endrov.flowFourier;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows: Fourier transform";
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
				FlowUnitFourier2D.class,
				FlowUnitInverseFourier2D.class,
				FlowUnitFourier3D.class,
				FlowUnitInverseFourier3D.class,
				
				FlowUnitRotateImage2D.class,
				FlowUnitRotateImage3D.class,
				
				FlowUnitCircConv2D.class,
				FlowUnitCircConv3D.class,
				
				FlowUnitDoG2D.class,
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

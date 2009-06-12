package endrov.flowProjection;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows: Projection";
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
				FlowUnitAverageZ.class,
				FlowUnitProjectMaxZ.class,
				FlowUnitProjectSumZ.class
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

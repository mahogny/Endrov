package endrov.flowAveraging;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows: filters";
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
				FlowUnitBilateralFilter.class,
				FlowUnitMovingAverage.class,
				FlowUnitMovingEntropy.class,
				FlowUnitMovingSum.class,
				FlowUnitMovingVariance.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

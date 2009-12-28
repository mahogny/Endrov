package endrov.flowThreshold;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows: Thresholding";
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
				FlowUnitThresholdMaxEntropy2D.class,
				FlowUnitThresholdOtsu2D.class,
				FlowUnitThresholdFukunaga2D.class,
				FlowUnitThresholdPercentile2D.class,
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

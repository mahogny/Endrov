package endrov.flowImageStats;
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
				FlowUnitConvGaussian2D.class,
				FlowUnitConvGaussian3D.class,
				FlowUnitBilateralFilter2D.class,
				FlowUnitKuwaharaFilter.class,
				FlowUnitKirschFilter2D.class,
				FlowUnitAverageRect.class,
				FlowUnitEntropyCircle.class,FlowUnitEntropyRect.class,
				FlowUnitSumRect.class,
				FlowUnitVarianceCircle.class,FlowUnitVarianceRect.class,
				FlowUnitPercentileRect.class,
				
		};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

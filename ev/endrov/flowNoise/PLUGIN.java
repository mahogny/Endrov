package endrov.flowNoise;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows: noise";
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
				FlowUnitImageNoiseExponential.class,
				FlowUnitImageNoisePepperSalt.class,
				FlowUnitImageNoisePoisson.class,
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

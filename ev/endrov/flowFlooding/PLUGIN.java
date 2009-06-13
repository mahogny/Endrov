package endrov.flowFlooding;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows: Flooding";
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
				FlowUnitFloodSelectSameColor.class,
				FlowUnitFloodSelectSigma.class,
				FlowUnitWatershed.class
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

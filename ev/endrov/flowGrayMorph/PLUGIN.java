package endrov.flowGrayMorph;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows: Gray morphology";
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
				FlowUnitGrayBlackTophat2D.class,
				FlowUnitGrayMorphClose2D.class,
				FlowUnitGrayMorphDilate2D.class,
				FlowUnitGrayMorphErode2D.class,
				FlowUnitGrayMorphOpen2D.class,
				FlowUnitGrayMorphWhiteTophat2D.class
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

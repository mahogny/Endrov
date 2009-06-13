package endrov.flowBinaryMorph;
import endrov.ev.PluginDef;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows: Binary morphology";
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
				FlowUnitBinBlackTophat2D.class,
				FlowUnitBinMorphClose2D.class,
				FlowUnitBinMorphDilate2D.class,
				FlowUnitBinMorphErode2D.class,
				FlowUnitBinMorphOpen2D.class,
				FlowUnitBinMorphWhiteTophat2D.class,
				
				FlowUnitBinMorphComplement.class,
				FlowUnitBinMorphHitmiss2D.class,
				FlowUnitBinMorphThick2D.class,
				FlowUnitBinMorphThin2D.class
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

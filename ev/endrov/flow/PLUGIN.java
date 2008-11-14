package endrov.flow;
import endrov.ev.PluginDef;
import endrov.flow.std.basic.FlowUnitGetObject;
import endrov.flow.std.basic.FlowUnitIf;
import endrov.flow.std.basic.FlowUnitInput;
import endrov.flow.std.basic.FlowUnitOutput;
import endrov.flow.std.basic.FlowUnitScript;
import endrov.flow.std.constants.FlowUnitConstBoolean;
import endrov.flow.std.constants.FlowUnitConstDouble;
import endrov.flow.std.constants.FlowUnitConstInteger;
import endrov.flow.std.constants.FlowUnitConstString;
import endrov.flow.std.math.FlowUnitAdd;
import endrov.flow.std.math.FlowUnitDiv;
import endrov.flow.std.math.FlowUnitSub;
import endrov.flow.ui.FlowWindow;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows";
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
		
		
		return new Class[]{Flow.class,FlowWindow.class,
				FlowUnitGetObject.class,FlowUnitIf.class,FlowUnitInput.class,FlowUnitOutput.class,FlowUnitScript.class,
				FlowUnitConstBoolean.class,FlowUnitConstDouble.class,FlowUnitConstInteger.class,FlowUnitConstString.class,
				FlowUnitAdd.class,FlowUnitDiv.class,FlowUnitSub.class
		};
		
		
		}
	}

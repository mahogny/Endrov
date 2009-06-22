package endrov.flow;
import endrov.ev.PluginDef;
import endrov.flow.std.basic.*;
import endrov.flow.std.collection.FlowUnitConcat;
import endrov.flow.std.collection.FlowUnitHeadTail;
import endrov.flow.std.collection.FlowUnitSize;
import endrov.flow.std.constants.*;
import endrov.flow.std.logic.FlowUnitAnd;
import endrov.flow.std.logic.FlowUnitGreaterThan;
import endrov.flow.std.logic.FlowUnitNot;
import endrov.flow.std.logic.FlowUnitOr;
import endrov.flow.std.logic.FlowUnitXor;
import endrov.flow.std.math.*;
import endrov.flow.std.objects.*;
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
				//Basic
				FlowUnitIf.class,FlowUnitInput.class,FlowUnitOutput.class,FlowUnitScript.class,FlowUnitShow.class,
				
				//Const
				FlowUnitConstBoolean.class,FlowUnitConstDouble.class,FlowUnitConstInteger.class,
				FlowUnitConstString.class,FlowUnitConstClass.class,FlowUnitConstEvDecimal.class,
				
				//Math
				FlowUnitAdd.class,FlowUnitDiv.class,FlowUnitSub.class,FlowUnitMul.class,
				FlowUnitSin.class, FlowUnitCos.class, FlowUnitExp.class, FlowUnitLog.class, FlowUnitSqrt.class,
				FlowUnitMin.class,FlowUnitMax.class, FlowUnitAbs.class,
				
				//Logic
				FlowUnitAnd.class,FlowUnitOr.class,FlowUnitXor.class,FlowUnitNot.class,
				FlowUnitGreaterThan.class,

				/*
				//Imserv
				FlowUnitImserv.class,FlowUnitImservLoad.class,FlowUnitImservQuery.class,
				*/
				//Collection
				FlowUnitConcat.class,FlowUnitHeadTail.class,FlowUnitMap.class,FlowUnitSize.class,
				
				//Objects
				FlowUnitGetObject.class,FlowUnitParent.class,FlowUnitObjectIO.class,FlowUnitThisData.class,FlowUnitThisFlow.class,
		};
		
		
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

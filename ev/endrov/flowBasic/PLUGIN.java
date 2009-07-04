package endrov.flowBasic;
import endrov.ev.PluginDef;
import endrov.flowBasic.basic.*;
import endrov.flowBasic.collection.FlowUnitConcat;
import endrov.flowBasic.collection.FlowUnitHeadTail;
import endrov.flowBasic.collection.FlowUnitSize;
import endrov.flowBasic.constants.*;
import endrov.flowBasic.convert.FlowUnitConvertFromVector2i;
import endrov.flowBasic.convert.FlowUnitConvertFromVector3i;
import endrov.flowBasic.convert.FlowUnitConvertToVector2i;
import endrov.flowBasic.convert.FlowUnitConvertToVector3i;
import endrov.flowBasic.convert.FlowUnitWrapInChannel;
import endrov.flowBasic.images.FlowUnitChannelDim2D;
import endrov.flowBasic.images.FlowUnitChannelDim3D;
import endrov.flowBasic.logic.FlowUnitAnd;
import endrov.flowBasic.logic.FlowUnitGreaterThan;
import endrov.flowBasic.logic.FlowUnitNot;
import endrov.flowBasic.logic.FlowUnitOr;
import endrov.flowBasic.logic.FlowUnitXor;
import endrov.flowBasic.math.*;
import endrov.flowBasic.objects.*;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flows: Basic units";
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
				FlowUnitWrapInChannel.class,
				
				//Convert
				FlowUnitConvertFromVector2i.class,
				FlowUnitConvertFromVector3i.class,
				FlowUnitConvertToVector2i.class,
				FlowUnitConvertToVector3i.class,
				
				//Images
				FlowUnitChannelDim2D.class,
				FlowUnitChannelDim3D.class,
		};
		
		
		}
	
	public boolean isDefaultEnabled(){return true;};
	}

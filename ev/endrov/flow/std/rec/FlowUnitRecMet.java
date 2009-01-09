package endrov.flow.std.rec;

import java.util.Map;

import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.recording.PlannedRecPrimitive;

/**
 * Flow unit for a recording method. The design is special in that additional conformity is wanted.
 * Components can be shown in the recmet window or in the flow. all additional inputs should be optional
 * or it will not work for the window.
 * 
 * @see PlannedRecPrimitive
 * 
 * @author Johan Henriksson
 *
 */
public abstract class FlowUnitRecMet extends FlowUnitBasic
	{

	
	
	
	//May want to divide into Slice, Stack, Channel, Imageset depending on the level of the output.
	//with laziness in output, arbitrary crossings can be produced but the extending unit has to do
	//more of the work
	
	
	
	//need component for recmet window. Will use the one that already exists, getGUIcomponent

	//need to know how to render connected components
	//can assume N of these are connected. maybe one single way of rendering?

	
	
	/**
	 * How many components can be connected? null if infinite
	 */
	public abstract Integer canConnectNum();
	
	
	/** 
	 * Get types of flows in. If this is overridden to add optional inputs then 
	 * this super method has to be invoked as well.
	 */
	protected void getTypesIn(Map<String, FlowType> types)
		{
		//Need to know what is currently connected!
		types.put("A", null);
		types.put("B", null);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types)
		{
		types.put("primitives", null); //Type
		}
	
	public void editDialog(){}

	
	}

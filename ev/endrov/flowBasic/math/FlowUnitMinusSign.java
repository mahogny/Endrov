/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.math;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.AnyEvImage;

/**
 * Flow unit: -x
 * @author Johan Henriksson
 *
 */
public class FlowUnitMinusSign extends FlowUnitMathUniop
	{
	private static final String metaType="minussign";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","-x",metaType,FlowUnitMinusSign.class, null,"Invert sign"));
		}
	
	public FlowUnitMinusSign()
		{
		super("-x",metaType);
		}
	
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		
		checkNotNull(a);
		if(a instanceof Number)
			lastOutput.put("B", -(((Number)a).doubleValue()));
		else if(a instanceof AnyEvImage)
			lastOutput.put("B", new EvOpImageMinusSign().exec1Untyped(exec.ph, (AnyEvImage)a));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass());
		}

	
	}

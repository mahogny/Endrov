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
 * Flow unit: Maximum
 * @author Johan Henriksson
 *
 */
public class FlowUnitMax extends FlowUnitMathBinop
	{
	private static final String metaType="max";
	
	public FlowUnitMax()
		{
		super("Max",metaType);
		}
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math",metaType,metaType,FlowUnitMax.class, null,"Maximum of numbers or images"));
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		checkNotNull(a,b);
		
		if(a instanceof Number && b instanceof Number)
			lastOutput.put("C", NumberMath.max((Number)a, (Number)b));
		else if(a instanceof AnyEvImage && b instanceof Number)
			lastOutput.put("C", new EvOpImageMaxScalar((Number)b).exec1Untyped(exec.ph, (AnyEvImage)a));
		else if(b instanceof AnyEvImage && a instanceof Number)
			lastOutput.put("C", new EvOpImageMaxScalar((Number)a).exec1Untyped(exec.ph, (AnyEvImage)b));
		else if(a instanceof AnyEvImage && b instanceof AnyEvImage)
			lastOutput.put("C", new EvOpImageMaxImage().exec1Untyped(exec.ph, (AnyEvImage)a,(AnyEvImage)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
		}

	
	}

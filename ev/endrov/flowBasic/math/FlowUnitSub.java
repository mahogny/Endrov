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
 * Flow unit: subtract
 * @author Johan Henriksson
 *
 */
public class FlowUnitSub extends FlowUnitMathBinop
	{
	private static final String metaType="sub";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","-",metaType,FlowUnitSub.class, null,"Subtract numbers"));
		}
	
	public FlowUnitSub()
		{
		super("A-B",metaType);
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		
		checkNotNull(a,b);
		if(a instanceof Number && b instanceof Number)
			lastOutput.put("C", NumberMath.minus((Number)a, (Number)b));
		else if(a instanceof AnyEvImage && b instanceof Number)
			lastOutput.put("C", new EvOpImageSubScalar((Number)b).exec1Untyped((AnyEvImage)a));
		else if(b instanceof AnyEvImage && a instanceof Number)
			lastOutput.put("C", new EvOpScalarSubImage((Number)a).exec1Untyped((AnyEvImage)b));
		else if(a instanceof AnyEvImage && b instanceof AnyEvImage)
			lastOutput.put("C", new EvOpImageSubImage().exec1Untyped((AnyEvImage)a, (AnyEvImage)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
		}

	
	}

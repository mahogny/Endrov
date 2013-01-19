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
import endrov.typeImageset.AnyEvImage;

/**
 * Flow unit: a^b
 * @author Johan Henriksson
 *
 */
public class FlowUnitPow extends FlowUnitMathBinop
	{
	private static final String metaType="add";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","PowerOf",metaType,FlowUnitPow.class, null,"One value to the power of another e.g x^3=x*x*x"));
		}
	
	public FlowUnitPow()
		{
		super("A^B",metaType);
		}
	
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		
		if(a instanceof Number && b instanceof Number)
			lastOutput.put("C", NumberMath.pow((Number)a, (Number)b));
		else if(a instanceof AnyEvImage && b instanceof Number)
			lastOutput.put("C", new EvOpImagePowScalar((Number)b).exec1Untyped(exec.ph, (AnyEvImage)a));/*
		else if(b instanceof AnyEvImage && a instanceof Number)
			lastOutput.put("C", new EvOpImagePowScalar((Number)a).exec1Untyped((AnyEvImage)b));
		else if(a instanceof AnyEvImage && b instanceof AnyEvImage)
			lastOutput.put("C", new EvOpImageAddImage().exec1Untyped((AnyEvImage)a, (AnyEvImage)b));*/
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
		}

	
	}

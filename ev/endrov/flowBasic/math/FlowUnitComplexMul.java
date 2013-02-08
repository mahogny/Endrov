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
 * Flow unit: *
 * @author Johan Henriksson
 *
 */
public class FlowUnitComplexMul extends FlowUnitMathBinComplexOp
	{
	private static final String metaType="mulComplex";
	
	public FlowUnitComplexMul()
		{
		super("A*B",metaType);
		}
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","Complex *",metaType,FlowUnitComplexMul.class, null,"Multiply complex numbers"));
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		Object aReal=flow.getInputValue(this, exec, "Areal");
		Object aImag=flow.getInputValue(this, exec, "Aimag");
		Object bReal=flow.getInputValue(this, exec, "Breal");
		Object bImag=flow.getInputValue(this, exec, "Bimag");
		
		/*
		if(aReal instanceof Number && bReal instanceof Number)
			lastOutput.put("C", NumberMath.mul((Number)aReal, (Number)bReal));
		else if(aReal instanceof AnyEvImage && bReal instanceof Number)
			lastOutput.put("C", new EvOpImageMulScalar((Number)bReal).exec1Untyped((AnyEvImage)aReal));
		else if(bReal instanceof AnyEvImage && aReal instanceof Number)
			lastOutput.put("C", new EvOpImageMulScalar((Number)aReal).exec1Untyped((AnyEvImage)bReal));
		
		else */if(aReal instanceof AnyEvImage && aImag instanceof AnyEvImage
				&& bReal instanceof AnyEvImage && bImag instanceof AnyEvImage)
			{
			AnyEvImage[] ret=new EvOpImageComplexMulImage().execUntyped(exec.ph, 
					(AnyEvImage)aReal,(AnyEvImage)aImag,(AnyEvImage)bReal,(AnyEvImage)bImag);
			lastOutput.put("Creal", ret[0]);
			lastOutput.put("Cimag", ret[1]);
			}
		else
			throw new BadTypeFlowException("Unsupported numerical types "+aReal.getClass()+" & "+bReal.getClass());

		
		
		
		}

	
	}

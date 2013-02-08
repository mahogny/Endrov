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
 * Flow unit: absolute value
 * @author Johan Henriksson
 *
 */
public class FlowUnitAbs extends FlowUnitMathUniop
	{
	private static final String metaType="abs";
	private static String showName="|A|";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitAbs.class, CategoryInfo.icon,"Absolute value"));
		}
	
	public FlowUnitAbs()
		{
		super(showName,metaType);
		}
	
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");

		checkNotNull(a);
		if(a instanceof Number)
			lastOutput.put("B", NumberMath.abs((Number)a));
		else if(a instanceof AnyEvImage)
			lastOutput.put("B", new EvOpImageAbs().exec1Untyped(exec.ph, (AnyEvImage)a));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass());
		}

	
	public String getHelpArticle()
		{
		return "flowunit abs";
		}

	}

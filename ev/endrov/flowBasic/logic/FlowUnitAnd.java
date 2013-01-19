/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.logic;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.typeImageset.AnyEvImage;

/**
 * Flow unit: and
 * @author Johan Henriksson
 *
 */
public class FlowUnitAnd extends FlowUnitLogicBinop
	{
	private static final String metaType="and";
	private static final String showName="&";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Logic",showName,metaType,FlowUnitAnd.class, null,"And"));
		}
	
	public FlowUnitAnd()
		{
		super(showName,metaType);
		}
	
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		
		checkNotNull(a,b);
		if(a instanceof Boolean && b instanceof Boolean)
			lastOutput.put("C", (Boolean)a && (Boolean)b);
		else if(a instanceof AnyEvImage && b instanceof AnyEvImage)
			lastOutput.put("C", new EvOpAndImage().exec1Untyped(exec.ph, (AnyEvImage)a, (AnyEvImage)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
		}

	
	}

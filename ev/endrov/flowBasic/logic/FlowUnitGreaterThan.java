package endrov.flowBasic.logic;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowBasic.math.NumberMath;
import endrov.imageset.AnyEvImage;

/**
 * Flow unit: Compare which is greater
 * @author Johan Henriksson
 *
 */
public class FlowUnitGreaterThan extends FlowUnitLogicBinop
	{
	private static final String metaType="greaterThan";
	
	public FlowUnitGreaterThan()
		{
		super("A>B",metaType);
		}
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Logic",">",metaType,FlowUnitGreaterThan.class, null,"Check which is greater"));
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");

		checkNotNull(a,b);
		if(a instanceof Number && b instanceof Number)
			lastOutput.put("C", NumberMath.greaterThan((Number)a, (Number)b));
		
//		else if(a instanceof Number && b instanceof Number)
//			lastOutput.put("C", new EvOpMaxImageScalar((Number)b).exec1Untyped((AnyEvImage)a));
		
		else if(a instanceof AnyEvImage && b instanceof Number)
			lastOutput.put("C", new EvOpImageGreaterThanScalar((Number)b).exec1Untyped((AnyEvImage)a));
		
		else if(a instanceof AnyEvImage && b instanceof AnyEvImage)
			lastOutput.put("C", new EvOpImageGreaterThanImage().exec1Untyped((AnyEvImage)a,(AnyEvImage)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
		}

	
	}

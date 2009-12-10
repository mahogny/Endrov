package endrov.flowBasic.math;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.AnyEvImage;

/**
 * Flow unit: Compare which is greater
 * @author Johan Henriksson
 *
 */
public class FlowUnitEquals extends FlowUnitMathBinop
	{
	private static final String metaType="equals";
	private static final String showName="A=B";
	
	public FlowUnitEquals()
		{
		super(showName,metaType);
		}
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Logic",showName,metaType,FlowUnitEquals.class, null,"Ask if A is greater (>) than B"));
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");

		if(a instanceof Number && b instanceof Number)
			lastOutput.put("C", NumberMath.greater((Number)a, (Number)b));
		else if(a instanceof Number && b instanceof AnyEvImage)
			lastOutput.put("C", new EvOpImageEqualsScalar((Number)a).exec1Untyped((AnyEvImage)b));
		else if(a instanceof AnyEvImage && b instanceof Number)
			lastOutput.put("C", new EvOpImageEqualsScalar((Number)b).exec1Untyped((AnyEvImage)a));
		else if(a instanceof AnyEvImage && b instanceof AnyEvImage)
			lastOutput.put("C", new EvOpImageGreaterThanImage().exec1Untyped((AnyEvImage)a,(AnyEvImage)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
		}

	
	}

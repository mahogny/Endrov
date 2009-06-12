package endrov.flow.std.math;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.AnyEvImage;

/**
 * Flow unit: Max
 * @author Johan Henriksson
 *
 */
public class FlowUnitMin extends FlowUnitMathBinop
	{
	private static final String metaType="min";
	
	public FlowUnitMin()
		{
		super("Min",metaType);
		}
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math",metaType,metaType,FlowUnitMin.class, null,"Minimum of numbers or images"));
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		checkNotNull(a,b);
		
		if(a instanceof Number && b instanceof Number)
			lastOutput.put("C", NumberMath.min((Number)a, (Number)b));
		else if(a instanceof AnyEvImage && b instanceof Number)
			lastOutput.put("C", new EvOpMinImageScalar((Number)b).exec1Untyped((AnyEvImage)a));
		else if(b instanceof AnyEvImage && a instanceof Number)
			lastOutput.put("C", new EvOpMinImageScalar((Number)a).exec1Untyped((AnyEvImage)b));
		else if(a instanceof AnyEvImage && b instanceof AnyEvImage)
			lastOutput.put("C", new EvOpMinImageImage().exec1Untyped((AnyEvImage)a,(AnyEvImage)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());

		
		
		
		}

	
	}

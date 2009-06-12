package endrov.flow.std.math;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.AnyEvImage;

/**
 * Flow unit: /
 * @author Johan Henriksson
 *
 */
public class FlowUnitDiv extends FlowUnitMathBinop
	{
	private static final String metaType="div";
	
	public FlowUnitDiv()
		{
		super("A/B",metaType);
		}
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","/",metaType,FlowUnitDiv.class, null,"Divide numbers"));
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		
		checkNotNull(a,b);
		if(a instanceof Number && b instanceof Number)
			lastOutput.put("C", NumberMath.div((Number)a, (Number)b));
		else if(a instanceof AnyEvImage && b instanceof Number)
			lastOutput.put("C", new EvOpImageDivScalar((Number)b).exec1Untyped((AnyEvImage)a));
		else if(b instanceof AnyEvImage && a instanceof Number)
			lastOutput.put("C", new EvOpScalarDivImage((Number)a).exec1Untyped((AnyEvImage)b));
		else if(a instanceof AnyEvImage && b instanceof AnyEvImage)
			lastOutput.put("C", new EvOpImageDivImage().exec1Untyped((AnyEvImage)a, (AnyEvImage)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
		}

	
	}

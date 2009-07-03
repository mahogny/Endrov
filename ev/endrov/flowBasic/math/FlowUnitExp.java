package endrov.flowBasic.math;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.AnyEvImage;

/**
 * Flow unit: add numbers
 * @author Johan Henriksson
 *
 */
public class FlowUnitExp extends FlowUnitMathUniop
	{
	private static final String metaType="exp";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","exp",metaType,FlowUnitExp.class, null,"Exponentiate values"));
		}
	
	public FlowUnitExp()
		{
		super("e^A",metaType);
		}
	
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		
		checkNotNull(a);
		if(a instanceof Number)
			lastOutput.put("B", Math.exp(((Number)a).doubleValue()));
		else if(a instanceof AnyEvImage)
			lastOutput.put("B", new EvOpImageExp().exec1Untyped((AnyEvImage)a));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass());
		}

	
	}

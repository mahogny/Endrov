package endrov.flow.std.math;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;

/**
 * Flow unit: add numbers
 * @author Johan Henriksson
 *
 */
public class FlowUnitAdd extends FlowUnitMathBinop
	{
	private static final String metaType="add";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","+",metaType,FlowUnitAdd.class, null,"Add values"));
		}
	
	public FlowUnitAdd()
		{
		super("A+B",metaType);
		}
	
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		if(a instanceof Double)
			lastOutput.put("C", ((Double)a)+toDouble(b));
		else if(a instanceof Integer)
			lastOutput.put("C", ((Integer)a)+((Integer)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
		}

	
	}

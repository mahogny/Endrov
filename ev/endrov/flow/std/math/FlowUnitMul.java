package endrov.flow.std.math;


import java.util.Map;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;

/**
 * Flow unit: *
 * @author Johan Henriksson
 *
 */
public class FlowUnitMul extends FlowUnitMathBinop
	{
	private static final String metaType="mul";
	
	public FlowUnitMul()
		{
		super("A*B",metaType);
		}
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","*",metaType,FlowUnitMul.class, null,"Multiply numbers"));
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		if(a instanceof Double)
			lastOutput.put("C", ((Double)a)*toDouble(b));
		else
			throw new Exception("Unsupported numerical type "+a.getClass());
		}

	
	}

package endrov.flow.std.math;


import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;

public class FlowUnitAdd extends FlowUnitMathBinop
	{
	public FlowUnitAdd()
		{
		super("A+B");
		}
	
	public void evaluate(Flow flow) throws Exception
		{
		lastOutput.clear();
		Object a=flow.getInputValue(this, "A");
		Object b=flow.getInputValue(this, "B");
		if(a instanceof Double)
			lastOutput.put("C", ((Double)a)+toDouble(b));
		else if(a instanceof Integer)
			lastOutput.put("C", ((Integer)a)+((Integer)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
		}

	
	}

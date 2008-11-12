package endrov.flow.std.math;


import endrov.flow.Flow;

public class FlowUnitDiv extends FlowUnitMathBinop
	{
	public FlowUnitDiv()
		{
		super("A/B","div");
		}
	
	public void evaluate(Flow flow) throws Exception
		{
		lastOutput.clear();
		Object a=flow.getInputValue(this, "A");
		Object b=flow.getInputValue(this, "B");
		if(a instanceof Double)
			lastOutput.put("C", ((Double)a)/toDouble(b));
		else
			throw new Exception("Unsupported numerical type "+a.getClass());
		}

	
	}

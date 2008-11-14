package endrov.flow.std.math;


import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclarationTrivial;

public class FlowUnitSub extends FlowUnitMathBinop
	{
	private static final String metaType="sub";
	
	public static void initPlugin() {}
	static
		{
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Math","-",metaType){
		public FlowUnit createInstance(){return new FlowUnitSub();}});
		}
	
	public FlowUnitSub()
		{
		super("A-B",metaType);
		}
	
	public void evaluate(Flow flow) throws Exception
		{
		lastOutput.clear();
		Object a=flow.getInputValue(this, "A");
		Object b=flow.getInputValue(this, "B");
		if(a instanceof Double)
			lastOutput.put("C", ((Double)a)-toDouble(b));
		else if(a instanceof Integer)
			lastOutput.put("C", ((Integer)a)-((Integer)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
		}

	
	}

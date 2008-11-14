package endrov.flow.std.math;


import endrov.flow.Flow;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclarationTrivial;

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
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Math","/",metaType){
		public FlowUnit createInstance(){return new FlowUnitDiv();}});
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

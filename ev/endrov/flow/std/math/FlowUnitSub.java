package endrov.flow.std.math;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.EvChannel;

/**
 * Flow unit: subtract
 * @author Johan Henriksson
 *
 */
public class FlowUnitSub extends FlowUnitMathBinop
	{
	private static final String metaType="sub";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","-",metaType,FlowUnitSub.class, null,"Subtract numbers"));
		}
	
	public FlowUnitSub()
		{
		super("A-B",metaType);
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		
		if(a==null || b==null)
			{
			throw new BadTypeFlowException("Null values "+a+" "+b);
			}
		else if(a instanceof Number && b instanceof Number)
			{
			lastOutput.put("C", NumberMath.minus((Number)a, (Number)b));
			}
		else if(a instanceof EvChannel && b instanceof Number)
			{
			EvChannel ch=new OpImageSubScalar((Number)b).exec((EvChannel)a);
			lastOutput.put("C", ch);
			}
		else if(b instanceof EvChannel && a instanceof Number)
			{
			EvChannel ch=new ScalarSubImageOp((Number)a).exec((EvChannel)b);
			lastOutput.put("C", ch);
			}
		else if(a instanceof EvChannel && b instanceof EvChannel)
			{
			EvChannel ch=new OpImageSubImage().exec((EvChannel)a, (EvChannel)a);
			lastOutput.put("C", ch);
			}
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());

		
		
		
		/*
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		if(a instanceof Double)
			lastOutput.put("C", ((Double)a)-toDouble(b));
		else if(a instanceof Integer)
			lastOutput.put("C", ((Integer)a)-((Integer)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
			*/
		}

	
	}

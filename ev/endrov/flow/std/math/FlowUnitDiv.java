package endrov.flow.std.math;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.EvChannel;

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
		
		if(a==null || b==null)
			{
			throw new BadTypeFlowException("Null values "+a+" "+b);
			}
		else if(a instanceof Number && b instanceof Number)
			{
			lastOutput.put("C", NumberMath.div((Number)a, (Number)b));
			}
		else if(a instanceof EvChannel && b instanceof Number)
			{
			EvChannel ch=new EvOpImageDivScalar((Number)b).exec1((EvChannel)a);
			lastOutput.put("C", ch);
			}
		else if(b instanceof EvChannel && a instanceof Number)
			{
			EvChannel ch=new EvOpScalarDivImage((Number)a).exec1((EvChannel)b);
			lastOutput.put("C", ch);
			}
		else if(a instanceof EvChannel && b instanceof EvChannel)
			{
			EvChannel ch=new EvOpImageDivImage().exec1((EvChannel)a, (EvChannel)b);
			lastOutput.put("C", ch);
			}
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
		
		
		
		
		
		
		/*
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		if(a instanceof Double)
			lastOutput.put("C", ((Double)a)/toDouble(b));
		else
			throw new Exception("Unsupported numerical type "+a.getClass());
			*/
		}

	
	}

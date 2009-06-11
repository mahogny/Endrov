package endrov.flow.std.math;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.EvChannel;

/**
 * Flow unit: add numbers
 * @author Johan Henriksson
 *
 */
public class FlowUnitLog extends FlowUnitMathUniop
	{
	private static final String metaType="log";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","log",metaType,FlowUnitLog.class, null,"Log"));
		}
	
	public FlowUnitLog()
		{
		super("Log",metaType);
		}
	
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		
		if(a==null)
			{
			throw new BadTypeFlowException("Null values "+a);
			}
		else if(a instanceof Number)
			{
			lastOutput.put("B", Math.log(((Number)a).doubleValue()));
			}
		else if(a instanceof EvChannel)
			{
			EvChannel ch=new EvOpImageLog().exec1((EvChannel)a);
			lastOutput.put("B", ch);
			}
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass());
		}

	
	}

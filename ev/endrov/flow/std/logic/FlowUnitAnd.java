package endrov.flow.std.logic;


import java.util.Map;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.EvChannel;

/**
 * Flow unit: and
 * @author Johan Henriksson
 *
 */
public class FlowUnitAnd extends FlowUnitLogicBinop
	{
	private static final String metaType="add";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Logic","&",metaType,FlowUnitAnd.class, null,"And"));
		}
	
	public FlowUnitAnd()
		{
		super("A & B",metaType);
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
		else if(a instanceof Boolean && b instanceof Boolean)
			{
			lastOutput.put("C", (Boolean)a && (Boolean)b);
			}
		else if(a instanceof EvChannel && b instanceof EvChannel)
			{
			EvChannel ch=new OpAndImage().exec((EvChannel)a, (EvChannel)b);
			lastOutput.put("C", ch);
			}
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());

		/*
		if(a instanceof Double)
			lastOutput.put("C", ((Double)a)+toDouble(b));
		else if(a instanceof Integer)
			lastOutput.put("C", ((Integer)a)+((Integer)b));
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass()+" & "+b.getClass());
			*/
		}

	
	}

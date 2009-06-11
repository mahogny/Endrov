package endrov.flow.std.logic;


import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.imageset.EvChannel;

/**
 * Flow unit: not
 * @author Johan Henriksson
 *
 */
public class FlowUnitNot extends FlowUnitBasic
	{
	public static final String showName="Not";
	private static final String metaType="not";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Logic","not",metaType,FlowUnitNot.class, null,"Not"));
		}

	
	public String toXML(Element e)
		{
		return metaType;
		}

	public void fromXML(Element e)
		{
		}
	
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	
	public Color getBackground()
		{
		return new Color(200,255,200);
		}

	
	
	/** Get types of flows in */
	
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("A", null);
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("C", null);
		}
	
	
	public static double toDouble(Object o) throws Exception
		{
		if(o instanceof Double)
			return (Double)o;
		else if(o instanceof Integer)
			return (Integer)o;
		else throw new BadTypeFlowException("Not a numerical type "+o.getClass());
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
			lastOutput.put("C", (Boolean)a ^ (Boolean)b);
			}
		else if(a instanceof EvChannel && b instanceof EvChannel)
			{
			EvChannel ch=new EvOpXorImage().exec1((EvChannel)a, (EvChannel)b);
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

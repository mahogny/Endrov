package endrov.flow.std.math;


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
import endrov.flow.std.logic.OpXorImage;
import endrov.imageset.EvChannel;

/**
 * Flow unit: not
 * @author Johan Henriksson
 *
 */
public class FlowUnitExp extends FlowUnitBasic
	{
	public static final String showName="Exp";
	private static final String metaType="exp";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Math","exp",metaType,FlowUnitExp.class, null,"e^a"));
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
		types.put("B", null);
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
		
		if(a==null)
			{
			throw new BadTypeFlowException("Null values "+a);
			}
		else if(a instanceof Number)
			{
			lastOutput.put("B", Math.exp(((Number)a).doubleValue()));
			}
		else if(a instanceof EvChannel)
			{
			EvChannel ch=new OpImageExp().exec((EvChannel)a);
			lastOutput.put("B", ch);
			}
		else
			throw new BadTypeFlowException("Unsupported numerical types "+a.getClass());
		}

	
	}
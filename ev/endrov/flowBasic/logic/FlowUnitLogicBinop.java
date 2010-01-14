/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.logic;

import java.awt.Color;
import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.BadTypeFlowException;
import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;

/**
 * Flow unit: binary operator
 * @author Johan Henriksson
 *
 */
public abstract class FlowUnitLogicBinop extends FlowUnitBasic
	{
	public String showName,metaType;
	public FlowUnitLogicBinop(String showName,String metaType)
		{
		this.showName=showName;
		this.metaType=metaType;
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
		types.put("A", FlowType.ANYIMAGE.or(FlowType.TBOOLEAN));
		types.put("B", FlowType.ANYIMAGE.or(FlowType.TBOOLEAN)); //TODO they have to be the same
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("C", FlowType.ANYIMAGE.or(FlowType.TBOOLEAN)); //TODO they have to be the same
		}
	
	public static double toDouble(Object o) throws Exception
		{
		if(o instanceof Double)
			return (Double)o;
		else if(o instanceof Integer)
			return (Integer)o;
		else throw new BadTypeFlowException("Not a numerical type "+o.getClass());
		}
	
	
	
	}

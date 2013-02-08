/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.db;

import java.awt.Color;
import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;

public class FlowUnitImserv extends FlowUnitBasic
	{
	private static final String metaType="imserv.imserv";

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("ImServ","ImServ",metaType,FlowUnitImserv.class, null,"Connect to ImServ?"));
		}
	
	public String getBasicShowName(){return "ImServ";}
	public ImageIcon getIcon(){return null;}

	public static Color bgColor=new Color(200,255,200);
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}

	
	public Color getBackground()
		{
		return bgColor;
		}

	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("url", null);
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("imserv", null);
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
	{
//	Map<String,Object> lastOutput=exec.getLastOutput(this);
	//TODO flowunit
	}

	public String getHelpArticle()
		{
		return "Misc flow operations";
		}

	}

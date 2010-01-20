/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.rec;

import java.awt.Color;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowBasic.collection.CategoryInfo;

/**
 * Recording method: combine channels
 * @author Johan Henriksson
 */
public class RecmetCombineChannels extends FlowUnitRecmet
	{
	private static final String metaType="recmetCombineChannels";

	
	public Integer shouldConnectNum(){return null;}

	
//	private static ImageIcon icon=new ImageIcon(FlowUnitConcat.class.getResource("jhConcat.png"));


	
	public String getBasicShowName(){return "Combine channels";}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}

	public String toXML(Element e)
		{
		return metaType;
		}
	public void fromXML(Element e)
		{
		}

	
	

	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		//TODO
		/*
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "A");
		Object b=flow.getInputValue(this, exec, "B");
		if(a instanceof String && b instanceof String)
			lastOutput.put("C", ((String)a)+((String)b));
		else
			throw new BadTypeFlowException("Unsupported collection type "+a.getClass()+" & "+b.getClass());*/
		}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"Combine Channels",metaType,RecmetCombineChannels.class, null,
				"Combine several recording channels into one stack"));
		}

	}

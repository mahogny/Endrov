/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.misc;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowWindow.FlowView;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: Get dimensions of any image object
 * @author Johan Henriksson
 *
 */
public class FlowUnitComments extends FlowUnitBasic
	{
	public static final String showName="Comment";
	private static final String metaType="comment";
	
	public static final ImageIcon icon=null;//new ImageIcon(FlowUnitComments.class.getResource("jhFlowChannelDim.png"));

	private static Color bgColor=new Color(1.0f,1.0f,0.7f);
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration("Misc",showName,metaType,FlowUnitComments.class, icon,
		"A comment on the flow, does not do anything");
		Flow.addUnitType(decl);
		}
	
	
	public String var="";

	public String toXML(Element e)
		{
		e.setAttribute("value", ""+var);
		return metaType;
		}

	public void fromXML(Element e)
		{
		var=e.getAttributeValue("value");
		}
	
	
	public String getBasicShowName(){return "© ";}
	public ImageIcon getIcon(){return icon;}
	public Color getBackground(){return bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		}

	private void setVar(String s)
		{
		var=s;
		}

	@Override
	public Component getGUIcomponent(final FlowView p)
		{
		final JTextArea field=new JTextArea(var);
		field.setBackground(bgColor);
		field.setMinimumSize(new Dimension(20,field.getPreferredSize().height));
		
		EvSwingUtil.textAreaChangeListener(field, new ChangeListener(){
		public void stateChanged(ChangeEvent e)
			{
			setVar(field.getText());
//			p.repaint();
			}
		});

		return field;
		}


	
	
	}

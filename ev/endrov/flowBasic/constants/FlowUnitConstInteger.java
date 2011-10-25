/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.constants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowWindow.FlowView;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: integer constant
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstInteger extends FlowUnitConst
	{
	
	public int var=1;
	
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstInteger.class.getResource("jhNumber.png"));

	private static final String metaType="constInteger";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Integer",metaType,FlowUnitConstInteger.class, icon,"Constant integer");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(Integer.class, decl);
		}
	
	public String toXML(Element e)
		{
		e.setAttribute("value", ""+var);
		return metaType;
		}
	
	public void fromXML(Element e)
		{
		var=Integer.parseInt(e.getAttributeValue("value"));
		}

	
	@Override
	public String getBasicShowName()
		{
		return "I";
		}

	protected FlowType getConstType()
		{
		return FlowType.TINTEGER;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("out", var);
		}
	
	private void setVar(int s)
		{
		var=s;
		}
	
	public Component getGUIcomponent(final FlowView p)
		{
		final JTextField field=new JTextField(""+var);
		field.setMinimumSize(new Dimension(20,field.getPreferredSize().height));
		final Color colorOk=field.getForeground();
		EvSwingUtil.textAreaChangeListener(field, new ChangeListener(){
		public void stateChanged(ChangeEvent e)
			{
			try
				{
				field.setForeground(colorOk);
				setVar(Integer.parseInt(field.getText()));
				}
			catch (NumberFormatException e1)
				{
				field.setForeground(colorBadValue);
				setVar(0);
				}
			p.repaint();
			}
		});
		return field;
		}
	
	}

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
import endrov.flow.ui.FlowPanel;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: EvDecimal constant
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstEvDecimal extends FlowUnitConst
	{
	
	public FlowUnitConstEvDecimal()
		{
		}
	public FlowUnitConstEvDecimal(EvDecimal var)
		{
		this.var=var;
		}
	
	private EvDecimal var=new EvDecimal(1);
	
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstEvDecimal.class.getResource("jhNumber.png"));

	private static final String metaType="constEvDecimal";

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Decimal",metaType,FlowUnitConstEvDecimal.class, icon,"Constant EvDecimal");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(EvDecimal.class, decl);
		}
	
	public String toXML(Element e)
		{
		e.setAttribute("value", ""+getVar());
		return metaType;
		}
	
	public void fromXML(Element e)
		{
		setVar(new EvDecimal(e.getAttributeValue("value")));
		}

	
	@Override
	public String getBasicShowName()
		{
		return "Dec";
		}

	protected FlowType getConstType()
		{
		return FlowType.TDOUBLE;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("out", getVar());
		}
	
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		final JTextField field=new JTextField(getVar().toString());
		field.setMinimumSize(new Dimension(20,field.getPreferredSize().height));
		final Color colorOk=field.getForeground();
		
		EvSwingUtil.textAreaChangeListener(field, new ChangeListener(){
		public void stateChanged(ChangeEvent e)
			{
			try
				{
				field.setForeground(colorOk);
				setVar(new EvDecimal(field.getText()));
				}
			catch (NumberFormatException e1)
				{
				field.setForeground(colorBadValue);
				setVar(EvDecimal.ZERO);
				}
			p.repaint();
			}
		});
		return field;
		}

	public void setVar(EvDecimal var)
		{
		this.var = var;
		}

	public EvDecimal getVar()
		{
		return var;
		}
	

	}

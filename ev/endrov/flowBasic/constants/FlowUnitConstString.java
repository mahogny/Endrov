/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.constants;

import java.awt.Component;
import java.awt.Dimension;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitDeclaration;
import endrov.gui.EvSwingUtil;
import endrov.windowFlow.FlowView;

/**
 * Flow unit: input variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstString extends FlowUnitConst
	{
	

	public String var="";
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstString.class.getResource("jhString.png"));

	
	private static final String metaType="constString";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"String",metaType,FlowUnitConstString.class, icon,"Constant string");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(String.class, decl);
		}
	
	public String toXML(Element e)
		{
		e.setAttribute("value", ""+var);
		return metaType;
		}

	public void fromXML(Element e)
		{
		var=e.getAttributeValue("value");
		}
	
	@Override
	public String getBasicShowName()
		{
		return "S";
		}

	protected FlowType getConstType()
		{
		return FlowType.TSTRING;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("out", var);
		}
	
	
	private void setVar(String s)
		{
		var=s;
		}
	
	public Component getGUIcomponent(final FlowView p)
		{
		final JTextArea field=new JTextArea(var);
		field.setMinimumSize(new Dimension(20,field.getPreferredSize().height));
		/*field.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
				{
				//Should maybe be change listener
				//should emit an update
				}});*/
		
		
		EvSwingUtil.textAreaChangeListener(field, new ChangeListener(){
		public void stateChanged(ChangeEvent e)
			{
			setVar(field.getText());
			p.repaint();
			}
		});

		return field;
		}
	
	
	public String getHelpArticle()
		{
		return "Misc flow operations";
		}
	
	
	}

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
import endrov.util.mathExpr.MathExpr;
import endrov.util.mathExpr.MathExprParser;
import endrov.windowFlow.FlowView;

/**
 * Flow unit: mathematical expression
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstExpression extends FlowUnitConst
	{
	public String var="";
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstExpression.class.getResource("jhExpression.png"));
	private static final String metaType="constExpression";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Expression",metaType,FlowUnitConstExpression.class, icon,"Mathematical expression");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(MathExpr.class, decl);  
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
		return "Expr:";
		}

	protected FlowType getConstType()
		{
		return FlowType.TMATHEXPRESSION;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("out", MathExprParser.parse(var));
		}
	
	
	private void setVar(String s)
		{
		var=s;
		}
	
	public Component getGUIcomponent(final FlowView p)
		{
		final JTextArea field=new JTextArea(var);
		field.setMinimumSize(new Dimension(20,field.getPreferredSize().height));
		
		
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

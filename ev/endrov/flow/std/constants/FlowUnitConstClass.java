package endrov.flow.std.constants;

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
import endrov.flow.ui.FlowPanel;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: input variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstClass extends FlowUnitConst
	{
	

	private String var="java.lang.String";
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstClass.class.getResource("jhClass.png"));

	
	private static final String metaType="constClass";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"Class",metaType,FlowUnitConstClass.class, icon,"Constant class"));
		}
	
	public String toXML(Element e)
		{
		e.setAttribute("value", ""+getVar());
		return metaType;
		}

	public void fromXML(Element e)
		{
		setVar(e.getAttributeValue("value"));
		}
	
	protected String getLabel()
		{
		return "C";
		}

	protected FlowType getConstType()
		{
		return FlowType.TSTRING;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		Class<?> cl=Class.forName(getVar());
		lastOutput.put("out", cl);
		}
	
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		final JTextArea field=new JTextArea(getVar());
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

	public void setVar(String var)
		{
			this.var = var;
		}

	public String getVar()
		{
			return var;
		}
	
	
	
	
	
	
	
	
	}

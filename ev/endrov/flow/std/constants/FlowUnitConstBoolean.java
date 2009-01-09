package endrov.flow.std.constants;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;

/**
 * Flow unit: boolean constant
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstBoolean extends FlowUnitConst
	{
	
	public boolean var=true;
	
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstBoolean.class.getResource("jhBoolean.png"));

	private static final String metaType="constBoolean";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"Boolean",metaType,FlowUnitConstBoolean.class, icon,"Constant boolean"));
		}
	
	public String toXML(Element e)
		{
		e.setAttribute("value", ""+var);
		return metaType;
		}

	public void fromXML(Element e)
		{
		var=Boolean.parseBoolean(e.getAttributeValue("value"));
		}

	
	
	protected String getLabel()
		{
		return "B";
		}

	protected FlowType getConstType()
		{
		return FlowType.TBOOLEAN;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("out", var);
		}
	
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		final JCheckBox comp=new JCheckBox("",var);
		comp.setOpaque(false);
		comp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
				{
				//Should maybe be change listener
				//should emit an update
				p.repaint();
				}});
		return comp;
		}
	}

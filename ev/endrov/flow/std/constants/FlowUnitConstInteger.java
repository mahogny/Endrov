package endrov.flow.std.constants;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JTextField;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;

/**
 * Flow unit: integer constant
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstInteger extends FlowUnitConst
	{
	
	public int var=123;
	
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstInteger.class.getResource("jhNumber.png"));

	private static final String metaType="constInteger";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"Integer",metaType,FlowUnitConstInteger.class, icon,"Constant integer"));
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

	
	protected String getLabel()
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
	
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		final JTextField field=new JTextField(""+var);
		field.setMinimumSize(new Dimension(20,field.getPreferredSize().height));
		field.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
				{
				//Should maybe be change listener
				var=Integer.parseInt(field.getText());
				//should emit an update
				}});
		
		field.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent arg0){}
			public void keyReleased(KeyEvent arg0){}
			public void keyTyped(KeyEvent arg0){p.repaint();}
		
		});
		return field;
		}
	
	}

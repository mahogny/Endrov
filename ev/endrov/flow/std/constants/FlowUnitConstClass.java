package endrov.flow.std.constants;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;

/**
 * Flow unit: input variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstClass extends FlowUnitConst
	{
	

	public String var="java.lang.String";
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstClass.class.getResource("jhClass.png"));

	
	private static final String metaType="constClass";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"Class",metaType,FlowUnitConstClass.class, icon,"Constant class"));
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
		Class<?> cl=Class.forName(var);
		lastOutput.put("out", cl);
		}
	
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		final JTextArea field=new JTextArea(var);
		field.setMinimumSize(new Dimension(20,field.getPreferredSize().height));
		
		field.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent arg0){}
			public void keyReleased(KeyEvent arg0){}
			public void keyTyped(KeyEvent arg0)
				{
				var=field.getText();
				p.repaint();
				}
		
		});
		return field;
		}
	
	
	
	
	
	
	
	
	}

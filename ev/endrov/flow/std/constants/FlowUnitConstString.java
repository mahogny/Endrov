package endrov.flow.std.constants;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import org.jdom.Element;

import endrov.basicWindow.FlowExec;
import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;

/**
 * Flow unit: input variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstString extends FlowUnitConst
	{
	

	public String var="foo";
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstString.class.getResource("jhString.png"));

	
	private static final String metaType="constString";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Const","String",metaType,FlowUnitConstString.class, icon));
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
	
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		final JTextArea field=new JTextArea(var);
		field.setMinimumSize(new Dimension(20,field.getPreferredSize().height));
		/*field.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
				{
				//Should maybe be change listener
				//should emit an update
				}});*/
		
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

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
	
	
	private EvDecimal var=new EvDecimal(123);
	
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstEvDecimal.class.getResource("jhNumber.png"));

	private static final String metaType="constEvDecimal";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"Decimal",metaType,FlowUnitConstEvDecimal.class, icon,"Constant EvDecimal"));
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

	
	protected String getLabel()
		{
		return "D";
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
		final JTextField field=new JTextField(""+getVar());
		field.setMinimumSize(new Dimension(20,field.getPreferredSize().height));
		/*field.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
				{
				//BUG
				//Should maybe be change listener
				setVar(new EvDecimal(field.getText()));
				//should emit an update
				}});*/
		
		EvSwingUtil.textAreaChangeListener(field, new ChangeListener(){
		public void stateChanged(ChangeEvent e)
			{
			setVar(new EvDecimal(field.getText()));
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

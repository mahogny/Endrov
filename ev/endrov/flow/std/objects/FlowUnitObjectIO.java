package endrov.flow.std.objects;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.swing.JTextArea;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvObject;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;
import endrov.util.Maybe;

/**
 * Get all objects of a type from a container
 * @author Johan Henriksson
 *
 */
public class FlowUnitObjectIO extends FlowUnit
	{
	private static final String metaType="evobjectio";
	private static final String showName="Object ref";
	
	
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitObjectIO.class, null,"Store/load object"));		
		}

	

	String nameOfObject;
	
	
	public String getLabel()
		{
		return showName;
		}
	
	
	public Dimension getBoundingBox(Component comp, Flow flow)
		{
		int w=fm.stringWidth(getLabel());
		Dimension d=new Dimension(3+w+3+comp.getWidth()+1,comp.getHeight()+2);
		return d;
		}
	
	public void paint(Graphics g, FlowPanel panel, Component comp)
		{
		Dimension d=getBoundingBox(comp, panel.getFlow());
		
		g.setColor(Color.GREEN);
		g.fillRect(x,y,d.width,d.height);
		g.setColor(getBorderColor(panel));
		g.drawRect(x,y,d.width,d.height);
		g.setColor(getTextColor());
		g.drawString(getLabel(), x+3, y+d.height/2+fonta/2);
		
		panel.drawConnPointLeft(g,this,"in",x,y+d.height/2);
		panel.drawConnPointRight(g,this,"out",x+d.width,y+d.height/2);
		}

	public boolean mouseHoverMoveRegion(int x, int y, Component comp, Flow flow)
		{
		Dimension dim=getBoundingBox(comp, flow);
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}

	
	/** Get types of flows in */
	public void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("objectIn", new FlowType(EvObject.class));
		}
	
	/** Get types of flows out */
	public void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("objectOut", new FlowType(EvObject.class));
		}

	
	
	public void editDialog()
		{
		}

	
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}

	
	
	
	
	public int getGUIcomponentOffsetX()
		{
		int w=fm.stringWidth(getLabel());
		return 3+w+3;
		}
	public int getGUIcomponentOffsetY(){return 1;}

	
	
	public void setRef(String s)
		{
		nameOfObject=s;
		}
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		final JTextArea field=new JTextArea(nameOfObject);
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
				setRef(field.getText());
				p.repaint();
				}
		
		});
		return field;
		}
	
	
	public String toXML(Element e)
		{
		e.setAttribute("ref",nameOfObject);
		return metaType;
		}
	public void fromXML(Element e)
		{
		nameOfObject=e.getAttributeValue("ref");
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		EvContainer parent=exec.getParent();

		Maybe<Object> con=flow.getInputValueMaybe(this, exec, "objectIn");
		EvObject obvalue;
		if(con.hasValue())
			{
			//Get value and store it
			obvalue=(EvObject)con.get();
			parent.metaObject.put(nameOfObject, obvalue);
			}
		else
			{
			//Read value
			obvalue=parent.metaObject.get(nameOfObject);
			}
		
		//Set output to the same value
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		lastOutput.put("objectOut", obvalue);
		}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	
	
	
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}

	String nameOfObject;
	
	public void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("objectIn", new FlowType(EvObject.class));
		}
	
	public void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("objectOut", new FlowType(EvObject.class));
		}
	
	*/
	}

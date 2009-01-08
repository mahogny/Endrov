package endrov.flow.std.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.*;

import javax.swing.JOptionPane;

import org.jdom.Element;

import endrov.basicWindow.FlowExec;
import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;

/**
 * Flow unit: input variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitInput extends FlowUnit
	{
	
	public String varName="foo";
	public FlowUnit varUnit;
	private static final String metaType="input";

	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Basic","Input",metaType,FlowUnitInput.class));
		}

	
	public String toXML(Element e)
		{
		e.setAttribute("varname", varName);
		return metaType;
		}

	public void fromXML(Element e)
		{
		varName=e.getAttributeValue("varname");
		}

	
	
	public Dimension getBoundingBox(Component comp)
		{
		int w=fm.stringWidth("In: "+varName);
		Dimension d=new Dimension(w+15,fonth);
		return d;
		}
	
	public void paint(Graphics g, FlowPanel panel, Component comp)
		{
		Dimension d=getBoundingBox(comp);

//		g.drawRect(x,y,d.width,d.height);
		
		int arcsize=8;
		
		g.setColor(Color.lightGray);
		g.fillRoundRect(x,y,d.width,d.height,arcsize,arcsize);
		g.setColor(getBorderColor(panel));
		g.drawRoundRect(x,y,d.width,d.height,arcsize,arcsize);
		g.setColor(getTextColor());
		g.drawString("In: "+varName, x+5, y+fonta);
		
		
		panel.drawConnPointRight(g,this,"out",x+d.width,y+d.height/2);
		
		}

	public boolean mouseHoverMoveRegion(int x, int y, Component comp)
		{
		Dimension dim=getBoundingBox(comp);
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}


	/** Get types of flows in */
	public Map<String, FlowType> getTypesIn()
		{
		return Collections.emptyMap();
		}
	/** Get types of flows out */
	public Map<String, FlowType> getTypesOut()
		{
		Map<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("out", null);
		return types;
		}
	
	
	public void editDialog()
		{
		String newVal=JOptionPane.showInputDialog(null,"Enter value",varName);
		if(newVal!=null)
			varName=newVal;
		}


	
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}

	/**
	 * There is an invisible connector to in. Whatever executes the flow is responsible for setting this input
	 */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Object a=flow.getInputValue(this, exec, "in");
		lastOutput.put("out", a);
		}

	public Component getGUIcomponent(FlowPanel p){return null;}
	public int getGUIcomponentOffsetX(){return 0;}
	public int getGUIcomponentOffsetY(){return 0;}

	}

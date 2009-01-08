package endrov.flow.std.constants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.*;

import org.jdom.Element;

import endrov.basicWindow.FlowExec;
import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;

/**
 * Flow unit: boolean constant
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstBoolean extends FlowUnit
	{
	
	public boolean var=true;
	
	
	private static final String metaType="constBoolean";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Const","Boolean",metaType,FlowUnitConstBoolean.class));
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

	
	
	private String getText()
		{
		return ""+var;
		}
	
	public Dimension getBoundingBox(Component comp)
		{
		int w=fm.stringWidth(getText());
		Dimension d=new Dimension(w+25,fonth);
		return d;
		}
	
	public void paint(Graphics g, FlowPanel panel, Component comp)
		{
		Dimension d=getBoundingBox(comp);

		
		g.setColor(Color.WHITE);
		g.fillRect(x,y,d.width,d.height);
		g.setColor(Color.black);
		g.drawRect(x,y,d.width,d.height);
		g.drawLine(x+2,y,x+2,y+d.height);
		g.drawLine(x+d.width-2,y,x+d.width-2,y+d.height);
		g.drawString(getText(), x+8, y+fonta);
		
		
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
	
	
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}

	public void editDialog()
		{
		var=!var;
		//TODO send out observer
		}

	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("out", var);
		}
	
	public Component getGUIcomponent(FlowPanel p){return null;}
	public int getGUIcomponentOffsetX(){return 0;}
	public int getGUIcomponentOffsetY(){return 0;}
	}

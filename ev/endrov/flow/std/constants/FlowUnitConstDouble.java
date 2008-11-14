package endrov.flow.std.constants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.*;

import javax.swing.JOptionPane;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;

/**
 * Flow unit: double constant
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstDouble extends FlowUnit
	{
	
	public double var;
	
	
	
	private static final String metaType="constDouble";
	
	public static void initPlugin() {}
	static
		{
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Const","Double",metaType)
			{
			public FlowUnit createInstance(){return new FlowUnitConstDouble(123);}
			public FlowUnit fromXML(Element e)
				{
				FlowUnitConstDouble u=new FlowUnitConstDouble(Double.parseDouble(e.getAttributeValue("value")));
				return u;
				}
			});
		}

	
	public String storeXML(Element e)
		{
		e.setAttribute("value", ""+var);
		return metaType;
		}

	
	public FlowUnitConstDouble(double var) 
		{
		this.var=var;
		}
	
	
	private String getText()
		{
		return "dbl "+var;
		}
	
	public Dimension getBoundingBox()
		{
		int w=fm.stringWidth(getText());
		Dimension d=new Dimension(w+25,fonth);
		return d;
		}
	
	public void paint(Graphics g, FlowPanel panel)
		{
		Dimension d=getBoundingBox();

		
		g.setColor(Color.WHITE);
		g.fillRect(x,y,d.width,d.height);
		g.setColor(Color.black);
		g.drawRect(x,y,d.width,d.height);
		g.drawLine(x+2,y,x+2,y+d.height);
		g.drawLine(x+d.width-2,y,x+d.width-2,y+d.height);
		g.drawString(getText(), x+8, y+fonta);
		
		
		panel.drawConnPointRight(g,this,"out",x+d.width,y+d.height/2);
		}
	
	public boolean mouseHoverMoveRegion(int x, int y)
		{
		Dimension dim=getBoundingBox();
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
		String newVal=JOptionPane.showInputDialog(null,"Enter value",""+var);
		if(newVal!=null)
			{
			try
				{
				var=Double.parseDouble(newVal);
				}
			catch (NumberFormatException e)
				{
				JOptionPane.showMessageDialog(null, "Invalid input");
				e.printStackTrace();
				}
			}
		}


	
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}

	
	public void evaluate(Flow flow) throws Exception
		{
		lastOutput.put("out", var);
		}
	
	}

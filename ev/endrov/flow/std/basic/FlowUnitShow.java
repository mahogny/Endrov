package endrov.flow.std.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

import org.jdom.Element;

import endrov.basicWindow.FlowExec;
import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;
import endrov.util.JMultilineLabel;
import endrov.util.Maybe;

public class FlowUnitShow extends FlowUnit
	{
	private static final String metaType="showValue";
	
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration("Basic","Show",metaType,FlowUnitShow.class, null));
		}
	
	public String toXML(Element e)
		{
		return metaType;
		}

	public void fromXML(Element e)
		{
		}
	

	protected String getLabel(){return ">";}
	
	public Dimension getBoundingBox(Component comp)
		{
		int w=fm.stringWidth(getLabel());
		Dimension d=new Dimension(3+w+3+comp.getWidth(),comp.getHeight());
		return d;
		}
	
	public void paint(Graphics g, FlowPanel panel, Component comp)
		{
		Dimension d=getBoundingBox(comp);
		
		g.setColor(Color.GREEN);
		g.fillRect(x,y,d.width,d.height);
		g.setColor(getBorderColor(panel));
		g.drawRect(x,y,d.width,d.height);
		g.setColor(getTextColor());
		g.drawString(getLabel(), x+3, y+d.height/2+fonta/2);
		
		panel.drawConnPointLeft(g,this,"in",x,y+d.height/2);
		}

	public boolean mouseHoverMoveRegion(int x, int y, Component comp)
		{
		Dimension dim=getBoundingBox(comp);
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}


	/** Get types of flows in */
	public Map<String, FlowType> getTypesIn()
		{
		Map<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("in", null);
		return types;
		}
	/** Get types of flows out */
	public Map<String, FlowType> getTypesOut()
		{
		return Collections.emptyMap();
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
	public int getGUIcomponentOffsetY(){return 0;}

	
	
	
	private WeakHashMap<ThisComponent, FlowExec> knowComponents=new WeakHashMap<ThisComponent, FlowExec>();

	
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Maybe<Object> in=flow.getInputValueMaybe(this, exec, "in");
		for(Map.Entry<ThisComponent, FlowExec> entry:knowComponents.entrySet())
			{
			if(entry.getValue()==exec)
				{
				ThisComponent c=entry.getKey();
				c.setText(in.toString());
				c.p.repaint();
				}
			}
		}
	
	
	private static class ThisComponent extends JMultilineLabel
		{
		static final long serialVersionUID=0;
		private FlowPanel p;
		}
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		final ThisComponent field=new ThisComponent();
		field.setText("");
		field.p=p;
		field.setMinimumSize(new Dimension(20,15));
		field.setOpaque(false);
		FlowExec exec=p.getFlowExec();

		knowComponents.put(field, exec);
		
		return field;
		}
	
	
	}

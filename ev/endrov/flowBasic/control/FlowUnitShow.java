/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.control;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.gui.component.JMultilineLabel;
import endrov.util.collection.Maybe;
import endrov.windowFlow.FlowView;

/**
 * Flow unit: show value
 * @author Johan Henriksson
 *
 */
public class FlowUnitShow extends FlowUnit
	{
	private static final String metaType="showValue";
	private static ImageIcon icon=new ImageIcon(FlowUnitShow.class.getResource("jhShow.png"));
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Show",metaType,FlowUnitShow.class, icon,"Show value");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitOutput(Object.class, decl);
		}
	
	public String toXML(Element e)
		{
		return metaType;
		}

	public void fromXML(Element e)
		{
		}
	

	protected String getLabel(){return ">";}
	
	public Dimension getBoundingBox(Component comp, Flow flow)
		{
		int w=fm.stringWidth(getLabel());
		Dimension d=new Dimension(3+w+3+comp.getWidth(),comp.getHeight());
		return d;
		}
	
	public void paint(Graphics g, FlowView panel, Component comp)
		{
		Dimension d=getBoundingBox(comp, panel.getFlow());
		
		g.setColor(Color.GREEN);
		g.fillRect(x,y,d.width,d.height);
		g.setColor(getBorderColor(panel));
		g.drawRect(x,y,d.width,d.height);
		g.setColor(getTextColor());
		g.drawString(getLabel(), x+3, y+d.height/2+fonta/2);
		
		panel.drawConnPointLeft(g,this,"in",x,y+d.height/2);
		}

	public boolean mouseHoverMoveRegion(int x, int y, Component comp, Flow flow)
		{
		Dimension dim=getBoundingBox(comp, flow);
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}


	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("in", FlowType.TANY);
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
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
		Maybe<Object> in=flow.getInputValueMaybe(this, exec, "in", Object.class);
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
		private FlowView p;
		}
	
	public Component getGUIcomponent(final FlowView p)
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

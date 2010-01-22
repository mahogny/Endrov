/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMeasure;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;
import endrov.imageset.EvChannel;

/**
 * Flow unit: measure particle
 * @author Johan Henriksson
 *
 */
public class FlowUnitMeasureParticle extends FlowUnit
	{
	public static FlowType flowTypeMeasure=new FlowType(ParticleMeasure.class);

	private static final String metaType="measureParticle3D";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Measure particle 3d",metaType,FlowUnitMeasureParticle.class, 
				CategoryInfo.icon,"Measure various properties of regions");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(Boolean.class, decl);
		}
	
	
	private HashSet<String> enabledProperty=new HashSet<String>();
	
	
	public String toXML(Element e)
		{
		for(String s:enabledProperty)
			{
			Element ne=new Element("enabled");
			ne.setText(s);
			e.addContent(ne);
			}
		return metaType;
		}

	public void fromXML(Element e)
		{
		for(Object o:e.getChildren())
			if(o instanceof Element)
				{
				Element ne=(Element)o;
				enabledProperty.add(ne.getText());
				}
		}

	

	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		
		EvOpAnalyzeParticle3D op=new EvOpAnalyzeParticle3D();
		for(String s:enabledProperty)
			op.enable(s);
		
		Object regions=flow.getInputValue(this, exec, "regions");
		Object values=flow.getInputValue(this, exec, "values");

		lastOutput.put("out", op.exec((EvChannel)regions,(EvChannel)values));
		}
	
	
	/**
	 * The special swing component for this unit
	 * @author Johan Henriksson
	 */
	private class TotalPanel extends JPanel
		{
		private static final long serialVersionUID = 1L;
		
		public TotalPanel()
			{
			int numCheck=ParticleMeasure.measures.size();
			setLayout(new GridLayout(numCheck,1));
			
			for(final String propName:ParticleMeasure.measures.keySet())
				{
				ParticleMeasure.MeasurePropertyType type=ParticleMeasure.measures.get(propName);
				
				final JCheckBox cbox=new JCheckBox(propName);
				cbox.setToolTipText(type.getDesc());
				cbox.setOpaque(false);
				cbox.setSelected(enabledProperty.contains(propName));
				add(cbox);
				
				cbox.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
							{
							if(cbox.isSelected())
								enabledProperty.add(propName);
							else
								enabledProperty.remove(propName);
							}
					});
				}
			
			setOpaque(false);
			}
				
		}


	public Component getGUIcomponent(final FlowPanel p)
		{
		return new TotalPanel();
		}

	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("regions", FlowType.ANYIMAGE);
		types.put("values", FlowType.ANYIMAGE);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", flowTypeMeasure);
		}
	
	
	public Dimension getBoundingBox(Component comp, Flow flow)
		{
		int w=fm.stringWidth(getLabel());
		Dimension d=new Dimension(3+w+3+comp.getWidth()+4,comp.getHeight()+2);
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
		
		helperDrawConnectors(g, panel, comp, getBoundingBox(comp, panel.getFlow()));
		}

	public boolean mouseHoverMoveRegion(int x, int y, Component comp, Flow flow)
		{
		Dimension dim=getBoundingBox(comp, flow);
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}

	

	
	public void editDialog()
		{
		}

	
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}

	
	private String getLabel()
		{
		return "MP";
		}
	
	
	public int getGUIcomponentOffsetX()
		{
		int w=fm.stringWidth(getLabel());
		return 3+w+3;
		}
	public int getGUIcomponentOffsetY(){return 1;}

	
	
	}

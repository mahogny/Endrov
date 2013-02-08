/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleMeasure.flow;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.typeImageset.EvChannel;
import endrov.typeParticleMeasure.ParticleMeasure;
import endrov.typeParticleMeasure.calc.MeasureProperty;
import endrov.typeParticleMeasure.calc.MeasurePropertyType;
import endrov.windowFlow.FlowView;

/**
 * Flow unit: measure particle
 * @author Johan Henriksson
 *
 */
public class FlowUnitMeasureParticle extends FlowUnitBasic
	{
	public static FlowType flowTypeMeasure=new FlowType(ParticleMeasure.class);

	private static final String metaType="measureParticle3D";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Measure particle 3D",metaType,FlowUnitMeasureParticle.class, 
				CategoryInfo.icon,"Measure various properties of regions");
		Flow.addUnitType(decl);
		}
	
	
	private HashSet<String> enabledProperty=new HashSet<String>();
	
	
	public FlowUnitMeasureParticle()
		{
		textPosition=TEXTABOVE;
		}
	
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

		lastOutput.put("out", op.exec(exec.ph, (EvChannel)regions,(EvChannel)values));
		}
	

	public Component getGUIcomponent(final FlowView p)
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
	
	
	@Override
	public Color getBackground()
		{
		return CategoryInfo.bgColor;
		}

	@Override
	public String getBasicShowName()
		{
		return "Measure particle";
		}

	@Override
	public ImageIcon getIcon()
		{
		return CategoryInfo.icon;
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
			int numCheck=MeasureProperty.measures.size();
			setLayout(new GridLayout(numCheck,1));
			
			for(final String propName:MeasureProperty.measures.keySet())
				{
				MeasurePropertyType type=MeasureProperty.measures.get(propName);
				
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


	public String getHelpArticle()
		{
		return "Flow ParticleMeasure";
		}

	
	}

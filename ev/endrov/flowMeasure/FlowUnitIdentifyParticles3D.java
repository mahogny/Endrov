/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMeasure;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowWindow.FlowView;
import endrov.imageset.AnyEvImage;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: Identify particles i.e. give them a unique ID
 * @author Johan Henriksson
 *
 */
public class FlowUnitIdentifyParticles3D extends FlowUnitBasic
	{
	public static final String showName="Identify particles 3D";
	private static final String metaType="identifyParticles3D";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitIdentifyParticles3D.class, CategoryInfo.icon,
				"Give particles unique IDs"));
		}
	
	private boolean ignore0=true;
	
	
	public FlowUnitIdentifyParticles3D()
		{
		textPosition=TEXTABOVE;
		}
	
	private void setValues(boolean ignore0)
		{
		this.ignore0=ignore0;
		}
	
	public String toXML(Element e)
		{
		Element eIgnore0=new Element("ignore0");
		eIgnore0.setText(Boolean.toString(ignore0));
		e.addContent(eIgnore0);
		return metaType;
		}
	
	public void fromXML(Element e)
		{
		ignore0=Boolean.parseBoolean(e.getChildText("ignore0"));
		}
	
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon()
		{
		return CategoryInfo.icon;
		}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("image", FlowType.ANYIMAGE);
//		types.put("ignore0", FlowType.TBOOLEAN);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", FlowType.ANYIMAGE); //TODO same type as "image"
		}
	
	public Component getGUIcomponent(FlowView p)
		{
		return new TotalPanel();
		}

	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		AnyEvImage a=(AnyEvImage)flow.getInputValue(this, exec, "image");
//		Boolean ignore0=(Boolean)flow.getInputValue(this, exec, "ignore0");

		lastOutput.put("out", new EvOpIdentifyParticles3D(ignore0).exec1Untyped(exec.ph, a));
		}

	
	
	/*********************************************************************
	 * The special swing component for this unit
	 * @author Johan Henriksson
	 */
	private class TotalPanel extends JPanel implements ActionListener
		{
		private static final long serialVersionUID = 1L;

		private JCheckBox cIgnore0=new JCheckBox();
		
		public TotalPanel()
			{
			setLayout(new BorderLayout());
			cIgnore0.setSelected(ignore0);
			cIgnore0.setOpaque(false);
			add(EvSwingUtil.withLabel("Ignore 0-value",cIgnore0));
			cIgnore0.addActionListener(this);
			setOpaque(false);
			}

		public void actionPerformed(ActionEvent e)
			{
			setValues(cIgnore0.isSelected());
			}
		
		}
	
	}

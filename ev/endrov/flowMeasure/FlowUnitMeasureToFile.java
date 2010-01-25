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
import java.io.File;
import java.io.FileWriter;
import java.util.*;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: store measure in file
 * @author Johan Henriksson
 *
 */
public class FlowUnitMeasureToFile extends FlowUnit
	{
	public static FlowType flowTypeMeasure=new FlowType(ParticleMeasure.class);

	private static final String metaType="storeMeasureParticleInFile";
	
	private String fieldDelim="\t";
	private boolean addHeaders=true;
	
	
	
	public String toXML(Element e)
		{
		Element eDelim=new Element("fieldDelim");
		Element eAddHeaders=new Element("addHeaders");
		eDelim.setText(fieldDelim);
		eAddHeaders.setText(Boolean.toString(addHeaders));
		
		e.addContent(eDelim);
		e.addContent(eAddHeaders);
		
		return metaType;
		}

	public void fromXML(Element e)
		{
		fieldDelim=e.getChildText("fieldDelim");
		addHeaders=Boolean.parseBoolean(e.getChildText("addHeaders"));
		}

	

	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		File file=(File)flow.getInputValue(this, exec, "file");
		ParticleMeasure measure=(ParticleMeasure)flow.getInputValue(this, exec, "measure");
		lastOutput.put("out",measure);
		
		FileWriter fw=new FileWriter(file); 
		measure.saveCSV(fw, addHeaders, fieldDelim);
		}
			
	
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		return new TotalPanel();
		}

	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("measure", flowTypeMeasure);
		types.put("file", FlowType.TFILE);
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
		return "  ";
		}
	
	
	public int getGUIcomponentOffsetX()
		{
		int w=fm.stringWidth(getLabel());
		return 3+w+3;
		}
	public int getGUIcomponentOffsetY(){return 1;}

	


	/*********************************************************************
	 * The special swing component for this unit
	 * @author Johan Henriksson
	 */
	private class TotalPanel extends JPanel 
		{
		private static final long serialVersionUID = 1L;

		
		public TotalPanel()
			{
			setLayout(new GridLayout(2,1));
	
			Vector<String> delimiters=new Vector<String>();
			delimiters.add("\t");
			delimiters.add(" ");
			delimiters.add(",");
			
			JCheckBox cAddHeader=new JCheckBox();
			JComboBox cFieldDelim=new JComboBox(delimiters);
			
			add(EvSwingUtil.withLabel("Add header", cAddHeader));
			add(EvSwingUtil.withLabel("Delimiter", cFieldDelim));
			
			cAddHeader.setSelected(addHeaders);
			cFieldDelim.setSelectedItem(fieldDelim); //this might be strange
			
			
			setOpaque(false);
			cAddHeader.setOpaque(false);
			cFieldDelim.setOpaque(false);
			/*
			Dimension size=new Dimension(300,200);
			setMaximumSize(size);
			setSize(size);
			setPreferredSize(size);*/
			}
		
		

		}
	
	
	
	
	
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Store measures in file",metaType,FlowUnitMeasureToFile.class, 
				CategoryInfo.icon,"Store the results of measure particle in a file");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(Boolean.class, decl);
		}

	}

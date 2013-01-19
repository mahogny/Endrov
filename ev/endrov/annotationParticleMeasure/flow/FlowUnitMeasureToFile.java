/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.annotationParticleMeasure.flow;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.jdom.Element;

import endrov.annotationParticleMeasure.ParticleMeasure;
import endrov.annotationParticleMeasure.ParticleMeasureIO;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowWindow.FlowView;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: store measure in file
 * @author Johan Henriksson
 *
 */
public class FlowUnitMeasureToFile extends FlowUnitBasic
	{
	public static FlowType flowTypeMeasure=new FlowType(ParticleMeasure.class);

	private static final String metaType="storeMeasureParticleInFile";
	
	private String fieldDelim="\t";
	private boolean addHeaders=true;
	
	private static Vector<Delim> delimiters=new Vector<Delim>();
		{
		delimiters.add(new Delim("<tab>","\t"));
		delimiters.add(new Delim("<space>"," "));
		delimiters.add(new Delim(",",","));
		}
	
	public FlowUnitMeasureToFile()
		{
		textPosition=TEXTABOVE;
		}
	
	public void setValues(String fieldDelim, boolean addHeaders)
		{
		this.fieldDelim=fieldDelim;
		this.addHeaders=addHeaders;
		}
	
	public String toXML(Element e)
		{
		Element eDelim=new Element("fieldDelim");
		Element eAddHeaders=new Element("addHeaders");
		//eDelim.setText(fieldDelim);
		eDelim.setAttribute("value", fieldDelim);
		eAddHeaders.setText(Boolean.toString(addHeaders));
		
		e.addContent(eDelim);
		e.addContent(eAddHeaders);
		
		return metaType;
		}

	public void fromXML(Element e)
		{
		fieldDelim=e.getChild("fieldDelim").getAttributeValue("value");
		//fieldDelim=e.getChildText("fieldDelim");
		addHeaders=Boolean.parseBoolean(e.getChildText("addHeaders"));
		}
	
	

	public Component getGUIcomponent(final FlowView p)
		{
		return new TotalPanel();
		}

	
	
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("measure", flowTypeMeasure);
		types.put("file", FlowType.TFILE);
		}
	
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", flowTypeMeasure);
		}

	

	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		File file=(File)flow.getInputValue(this, exec, "file");
		ParticleMeasure measure=(ParticleMeasure)flow.getInputValue(this, exec, "measure");
		lastOutput.put("out",measure);
		
		FileWriter fw=new FileWriter(file); 
		ParticleMeasureIO.saveCSV(measure, fw, addHeaders, fieldDelim);
		}
	


	
	@Override
	public Color getBackground()
		{
		return CategoryInfo.bgColor;
		}

	@Override
	public String getBasicShowName()
		{
		return "Measure to file";
		}

	@Override
	public ImageIcon getIcon()
		{
		return CategoryInfo.icon;
		}
	
	
	
	private static class Delim
		{
		String show;
		String delim;
		
		public Delim(String show, String delim)
			{
			this.show = show;
			this.delim = delim;
			}
		
		@Override
		public String toString()
			{
			return show;
			}
		}
	
	/*********************************************************************
	 * The special swing component for this unit
	 * @author Johan Henriksson
	 */
	private class TotalPanel extends JPanel implements ActionListener
		{
		private static final long serialVersionUID = 1L;

		private JCheckBox cAddHeader=new JCheckBox();
		private JComboBox cFieldDelim=new JComboBox(delimiters);

		public TotalPanel()
			{
			setLayout(new GridLayout(2,1));
	
			
			
			add(EvSwingUtil.withLabel("Add header", cAddHeader));
			add(EvSwingUtil.withLabel("Delimiter", cFieldDelim));
			
			cAddHeader.setSelected(addHeaders);
//			cFieldDelim.setSelectedItem(fieldDelim); //this might be strange
			for(Delim d:delimiters)
				if(d.delim.equals(fieldDelim))
					cFieldDelim.setSelectedItem(d);
			
			cAddHeader.addActionListener(this);
			cFieldDelim.addActionListener(this);
			
			setOpaque(false);
			cAddHeader.setOpaque(false);
			cFieldDelim.setOpaque(false);
			}

		public void actionPerformed(ActionEvent e)
			{
			setValues(((Delim)cFieldDelim.getSelectedItem()).delim, cAddHeader.isSelected());
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
		}
	

	}

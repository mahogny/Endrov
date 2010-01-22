/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.constants;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: input variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstFile extends FlowUnitConst
	{
	

	public String var="";
	
	private static ImageIcon icon=new ImageIcon(FlowUnitConstFile.class.getResource("silkFile.png"));

	
	private static final String metaType="constFile";
		
	public String toXML(Element e)
		{
		e.setAttribute("value", ""+var);
		return metaType;
		}

	public void fromXML(Element e)
		{
		var=e.getAttributeValue("value");
		}
	
	protected String getLabel()
		{
		return "F";
		}

	protected FlowType getConstType()
		{
		return FlowType.TFILE;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("out", new File(var));
		}
	
	
	private void setVar(String s)
		{
		var=s;
		}
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		final JTextArea field=new JTextArea(var);
		field.setMinimumSize(new Dimension(20,field.getPreferredSize().height));
		EvSwingUtil.textAreaChangeListener(field, new ChangeListener(){
		public void stateChanged(ChangeEvent e)
			{
			setVar(field.getText());
			p.repaint();
			}
		});

		//Browse button
		JButton bBrowse=new JButton("Browse");
		bBrowse.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
					{
					FileDialog fd=new FileDialog((Frame)null);
					fd.setTitle("Choose file");
					fd.setVisible(true);
					String fname=fd.getFile();
					if(fname!=null)
						{
						fname=new File(fd.getDirectory(),fd.getFile()).toString();
						setVar(fname);
						field.setText(fname);
						//p.repaint();
						}
					}
			});
		
		return EvSwingUtil.layoutLCR(null, field, bBrowse);//layoutCompactHorizontal(field,bBrowse);
		}
	
	
	
	
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"File",metaType,FlowUnitConstFile.class, icon,"Specify file path");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(File.class, decl);
		}
	
	
	}

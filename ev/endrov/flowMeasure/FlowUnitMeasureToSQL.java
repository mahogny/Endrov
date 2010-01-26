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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.jdom.Element;

import endrov.basicWindow.SpinnerSimpleEvFrame;
import endrov.basicWindow.icon.BasicIcon;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: Store measure result in SQL
 * @author Johan Henriksson
 *
 */
public class FlowUnitMeasureToSQL extends FlowUnitBasic
	{
	public static FlowType flowTypeMeasure=new FlowType(ParticleMeasure.class);
	
	public static final String showName="Store measures in SQL";
	private static final String metaType="particleMeasureToSQL";


	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitMeasureToSQL.class, CategoryInfo.icon,
				"Store measurements in database"));
		}
	
	public FlowUnitMeasureToSQL()
		{
		textPosition=TEXTABOVE;
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return CategoryInfo.icon;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("measure", flowTypeMeasure);
		types.put("connection", FlowType.TCONNECTION);
		types.put("dataid", FlowType.TSTRING);
		types.put("tablename", FlowType.TSTRING);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
//		types.put("out", FlowType.ANYIMAGE); //TODO same type as "image"
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
//		Map<String,Object> lastOutput=exec.getLastOutputCleared(this);
		
		Connection conn=(Connection)flow.getInputValue(this, exec, "connection");
		ParticleMeasure measure=(ParticleMeasure)flow.getInputValue(this, exec, "measure");
		String dataid=(String)flow.getInputValue(this, exec, "dataid");
		String tablename=(String)flow.getInputValue(this, exec, "tablename");
		
		
		lastConn=conn;
		lastPM=measure;
		lastDataid=dataid;
		lastTablename=tablename;
		
		measure.saveSQL(conn, dataid, tablename);
		
		
//		AnyEvImage a=(AnyEvImage)flow.getInputValue(this, exec, "image");

	
		//TODO
//		lastOutput.put("out", new EvOpIdentifyParticles3D().exec1Untyped(a));
		}

	
	private Connection lastConn;
	private ParticleMeasure lastPM;
	private String lastDataid;
	private String lastTablename;

	

	public Component getGUIcomponent(final FlowPanel p)
		{
		/*
		TotalPanel t=listPanels.get(p);
		if(t==null)
			listPanels.put(p, t=new TotalPanel());
		return t;
		*/
		return new TotalPanel();
		}
	
	/*********************************************************************
	 * The special swing component for this unit
	 * @author Johan Henriksson
	 */
	private class TotalPanel extends JPanel implements ActionListener
		{
		private static final long serialVersionUID = 1L;

		private JButton bDropTable=new JButton("Drop table");
		private JButton bCreateTable=new JButton("Create table");
		private JButton bDeleteDataid=new JButton("Delete dataid");
		
		
		
		
		public TotalPanel()
			{
			setLayout(new BorderLayout());
	
			add(EvSwingUtil.layoutEvenVertical(bDropTable, bCreateTable, bDeleteDataid),
					BorderLayout.SOUTH);
			
			bDropTable.addActionListener(this);
			bCreateTable.addActionListener(this);
			bDeleteDataid.addActionListener(this);
			}

		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bDropTable)
				{
				
				}
			else if(e.getSource()==bCreateTable)
				{
				
				}
			else if(e.getSource()==bDeleteDataid)
				{
				
				}
			
			// TODO Auto-generated method stub
			
			}
		}
	
	
	
	
	
	}

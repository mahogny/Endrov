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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.jdom.Element;

import endrov.annotationParticleMeasure.ParticleMeasure;
import endrov.annotationParticleMeasure.ParticleMeasureIO;
import endrov.core.log.EvLog;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowWindow.FlowView;
import endrov.gui.window.BasicWindow;

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
		}
	
	/** Execute algorithm */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Connection conn=(Connection)flow.getInputValue(this, exec, "connection");
		ParticleMeasure measure=(ParticleMeasure)flow.getInputValue(this, exec, "measure");
		String dataid=(String)flow.getInputValue(this, exec, "dataid");
		String tablename=(String)flow.getInputValue(this, exec, "tablename");
		
		lastConn=conn;
		lastPM=measure;
		lastDataid=dataid;
		lastTablename=tablename;
		
		ParticleMeasureIO.saveSQL(measure, conn, dataid, tablename);
		}

	
	private Connection lastConn;
	private ParticleMeasure lastPM;
	private String lastDataid;
	private String lastTablename;

	

	private Connection getLastConn()
		{
		return lastConn;
		}

	private ParticleMeasure getLastPM()
		{
		return lastPM;
		}

	private String getLastDataid()
		{
		return lastDataid;
		}

	private String getLastTablename()
		{
		return lastTablename;
		}

	public Component getGUIcomponent(final FlowView p)
		{
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
			setLayout(new GridLayout(3,1));
	
			add(bDropTable);
			add(bCreateTable);
			add(bDeleteDataid);
			
			bDropTable.addActionListener(this);
			bCreateTable.addActionListener(this);
			bDeleteDataid.addActionListener(this);
			}

		public void actionPerformed(ActionEvent e)
			{
			try
				{
				if(e.getSource()==bDropTable)
					{
					ParticleMeasure measure=getLastPM();
					if(measure==null)
						BasicWindow.showErrorDialog("Execute flow unit first");
					else
						ParticleMeasureIO.dropSQLtable(getLastConn(), getLastDataid(), getLastTablename());
					}
				else if(e.getSource()==bCreateTable)
					{
					ParticleMeasure measure=getLastPM();
					if(measure==null)
						BasicWindow.showErrorDialog("Execute flow unit first");
					else
						ParticleMeasureIO.createSQLtable(getLastPM(), getLastConn(), getLastDataid(), getLastTablename());
					}
				else if(e.getSource()==bDeleteDataid)
					{
					ParticleMeasure measure=getLastPM();
					if(measure==null)
						BasicWindow.showErrorDialog("Execute flow unit first");
					else
						ParticleMeasureIO.deleteFromSQLtable(getLastConn(), getLastDataid(), getLastTablename());
					}
				}
			catch (SQLException e1)
				{
				EvLog.printError(e1);
				}
			}
		}
	
	
	
	
	
	}

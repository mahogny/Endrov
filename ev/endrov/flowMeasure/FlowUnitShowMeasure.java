/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMeasure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.*;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.jdom.Element;

import endrov.basicWindow.SpinnerSimpleEvFrame;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;
import endrov.flowMeasure.ParticleMeasure.ParticleInfo;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: show particle measure
 * @author Johan Henriksson
 *
 */
public class FlowUnitShowMeasure extends FlowUnit
	{
	public static FlowType flowTypeMeasure=new FlowType(ParticleMeasure.class);

	private static final String metaType="showMeasureParticle";
	
	
	private WeakHashMap<FlowPanel, TotalPanel> listPanels=new WeakHashMap<FlowPanel, TotalPanel>();
	
	
	public String toXML(Element e)
		{
		return metaType;
		}

	public void fromXML(Element e)
		{
		}

	

	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		ParticleMeasure measure=(ParticleMeasure)flow.getInputValue(this, exec, "in");
		lastOutput.put("out",measure);
		
		//Update panels
		for(TotalPanel t:listPanels.values())
			t.setMeasure(measure);
		}
	
	
			
	
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		TotalPanel t=listPanels.get(p);
		if(t==null)
			listPanels.put(p, t=new TotalPanel());
		return t;
		}

	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("in", flowTypeMeasure);
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
		return " ";
		}
	
	
	public int getGUIcomponentOffsetX()
		{
		int w=fm.stringWidth(getLabel());
		return 3+w+3;
		}
	public int getGUIcomponentOffsetY(){return 1;}

	

	
	
	
	/*
			
	private class TotalPanel extends JPanel implements TableModel
		{
		private static final long serialVersionUID = 1L;
		
		//TODO Replace with something that knows which frames there are
		SpinnerSimpleEvFrame spFrame=new SpinnerSimpleEvFrame();
		//SpinnerSimpleInteger spID=new SpinnerSimpleInteger();
		
		ParticleMeasure measure;
		
		
		public TotalPanel()
			{
			setLayout(new BorderLayout());

			add(//EvSwingUtil.layoutEvenVertical(
					EvSwingUtil.withLabel("Frame ", spFrame),
					//EvSwingUtil.withLabel("ID ", spID)),
					BorderLayout.NORTH);
			
			JTable table=new JTable(this);
			add(table,BorderLayout.CENTER);
			
			setOpaque(false);
			}
		

	
				
		}
			
	}*/

	
	
	

	/*********************************************************************
	 * The special swing component for this unit
	 * @author Johan Henriksson
	 */
	private class TotalPanel extends JPanel implements TableModel
		{
		private static final long serialVersionUID = 1L;

		private LinkedList<TableModelListener> listeners=new LinkedList<TableModelListener>();

		//TODO Replace with something that knows which frames there are
		private SpinnerSimpleEvFrame spFrame=new SpinnerSimpleEvFrame();
		//SpinnerSimpleInteger spID=new SpinnerSimpleInteger();
		
		private ParticleMeasure measure=new ParticleMeasure();
		
		/**
		 * Row index to ID
		 */
		private List<Integer> mapToID=new ArrayList<Integer>();
		
		/**
		 * Column index to property. Does not include ID column
		 */
		private List<String> mapToColumn=new ArrayList<String>();

		
		private JTable table;

		
		public TotalPanel()
			{
			setLayout(new BorderLayout());
	
			add(//EvSwingUtil.layoutEvenVertical(
					EvSwingUtil.withLabel("Frame ", spFrame),
					//EvSwingUtil.withLabel("ID ", spID)),
					BorderLayout.NORTH);
			
			
			
			table=new JTable(this);
			JScrollPane sPane=new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			add(sPane,BorderLayout.CENTER);
			
/*			table=new JTable(this);
			add(table,BorderLayout.CENTER);*/
			
			spFrame.addChangeListener(new ChangeListener()
				{
				public void stateChanged(ChangeEvent e)
					{
					updateNewFrame();
					}
				});
			
			setOpaque(false);
			Dimension size=new Dimension(300,200);
			setMaximumSize(size);
			setSize(size);
			setPreferredSize(size);
			}
		
		

		public EvDecimal getCurrentFrame()
			{
			return spFrame.getDecimalValue();
			}
		
		
		public ParticleInfo getCurrentPropMap(int id)
			{
			Map<Integer,ParticleInfo> finfo=measure.getFrame(getCurrentFrame());
			if(finfo==null)
				return null;
			else
				return finfo.get(id);
			}
		
		/**
		 * Call whenever there is a new measure
		 */
		public void setMeasure(ParticleMeasure m)
			{
			measure=m;			
			mapToColumn.clear();
			mapToColumn.addAll(measure.getColumns());
			
			//TODO update frame pointer

			updateNewFrame();
			
//			doLayout();
			}

		/**
		 * Call whenever the frame is changed
		 */
		public void updateNewFrame()
			{
			mapToID.clear();
			Map<Integer, ParticleInfo> map=measure.getFrame(getCurrentFrame());
			if(map!=null)
				mapToID.addAll(map.keySet());
			
			for(TableModelListener l:listeners)
				l.tableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
			
//			System.out.println("new frame! "+mapToID.size()+"   "+mapToColumn.size());

			}
	
		public Class<?> getColumnClass(int columnIndex)
			{
			return String.class; //TODO
			}
	
		public int getColumnCount()
			{
			return mapToColumn.size()+1;
			}
	
		public String getColumnName(int columnIndex)
			{
			if(columnIndex==0)
				return "ID";
			else
				return mapToColumn.get(columnIndex-1);
			}
	
		public int getRowCount()
			{
			Map<Integer,ParticleInfo> m=measure.getFrame(getCurrentFrame());
			if(m==null)
				return 0;
			else
				return m.size();
			}
	
		public Object getValueAt(int rowIndex, int columnIndex)
			{
			int id=mapToID.get(rowIndex);
			if(columnIndex==0)
				return id;
			else
				{
				ParticleInfo info=getCurrentPropMap(id);
				String prop=mapToColumn.get(columnIndex-1);
				return info.getObject(prop).toString();
				}
			}
	
		public boolean isCellEditable(int rowIndex, int columnIndex)
			{
			return false;
			}
	
		
		public void addTableModelListener(TableModelListener l)
			{
			listeners.add(l);
			}
		
		public void removeTableModelListener(TableModelListener l)
			{
			listeners.remove(l);
			}
	
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
			{
			}
		}
	
	
	
	
	
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Show measures",metaType,FlowUnitShowMeasure.class, 
				CategoryInfo.icon,"Show the results of measure particle");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(Boolean.class, decl);
		}

	}

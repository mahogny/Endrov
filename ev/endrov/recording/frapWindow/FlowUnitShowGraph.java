/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.frapWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.jdom.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowWindow.FlowView;
import endrov.recording.CategoryInfo;

/**
 * Flow unit: show graph
 * @author Johan Henriksson
 *
 */
public class FlowUnitShowGraph extends FlowUnitBasic
	{
	private static final String metaType="showGraph";
	
	
	private WeakHashMap<FlowView, TotalPanel> listPanels=new WeakHashMap<FlowView, TotalPanel>();
	
	
	public FlowUnitShowGraph()
		{
		textPosition=TEXTABOVE;
		}
	
	
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
		
		Object ob=flow.getInputValue(this, exec, "in");
		lastOutput.put("out",ob);
		
		//Update panels
		for(TotalPanel t:listPanels.values())
			t.setObject(ob);
		}
	
	
			
	
	
	public Component getGUIcomponent(final FlowView p)
		{
		TotalPanel t=listPanels.get(p);
		if(t==null)
			listPanels.put(p, t=new TotalPanel());
		return t;
		}

	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("in", FlowType.TANY);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", FlowType.TANY); //TODO Same type as input
		}
	

	
	@Override
	public Color getBackground()
		{
		return CategoryInfo.bgColor;
		}

	@Override
	public String getBasicShowName()
		{
		return "2D Graph";
		}

	@Override
	public ImageIcon getIcon()
		{
		return CategoryInfo.icon;
		}




	/*********************************************************************
	 * The special swing component for this unit
	 * @author Johan Henriksson
	 */
	private class TotalPanel extends JPanel
		{
		private static final long serialVersionUID = 1L;


		//TODO Replace with something that knows which frames there are
		private Object object=null;
		
		private XYSeries frametimeSeries=new XYSeries("XY");

		public TotalPanel()
			{
			setLayout(new BorderLayout());
	
			XYDataset xyDataset = new XYSeriesCollection(frametimeSeries);
			
			JFreeChart chart = ChartFactory.createXYLineChart
	            ("","X","Y",xyDataset,PlotOrientation.VERTICAL,false/*legend*/, false/*tooltips*/, false/*urls*/);
			ChartPanel graphpanel = new ChartPanel(chart);

			add(graphpanel,BorderLayout.CENTER);
			
			setOpaque(false);
			Dimension size=new Dimension(300,200);
			setMaximumSize(size);
			setSize(size);
			setPreferredSize(size);
			}
		
		

		
		/**
		 * Call whenever there is a new measure
		 */
		public void setObject(Object m)
			{
			object=m;			

			frametimeSeries.clear();
			if(object==null)
				;
			else if(object instanceof double[][])
				{
				double[][] arr=(double[][])object;
				for(int i=0;i<arr[0].length;i++)
					frametimeSeries.add(arr[0][i],arr[1][i]);
				}
			repaint();
			}
	
		}
	
	
	
	
	
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Show XY graph",metaType,FlowUnitShowGraph.class, 
				CategoryInfo.icon,"Show a connected XY graph");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(Boolean.class, decl);
		}

	}

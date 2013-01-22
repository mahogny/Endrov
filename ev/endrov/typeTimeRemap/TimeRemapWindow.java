/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeTimeRemap;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;

import endrov.data.EvData;
import endrov.gui.component.EvComboObjectOne;
import endrov.gui.component.EvFrameEditor;
import endrov.gui.component.JSpinnerFrameModel;
import endrov.gui.icon.BasicIcon;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.util.collection.Tuple;
import endrov.util.math.EvDecimal;
import endrov.windowViewer2D.*;

import org.jdom.*;


/**
 * Adjust Frame-Time mapping
 * @author Johan Henriksson
 */
public class TimeRemapWindow extends EvBasicWindow implements ActionListener, ChangeListener
	{
	static final long serialVersionUID=0;

	
	/**
	 * One entry line
	 */
	private static class InputLine
		{
		JSpinner frame, time;
		JButton bDelete=BasicIcon.getButtonDelete();
		}
	
	//GUI components
	private JButton bAdd=new JButton("New point");
	private JButton bRefresh=new JButton("Refresh");
	
	private JPanel datapart=new JPanel();
	private XYSeries frametimeSeries=new XYSeries("FT");
	private ArrayList<InputLine> inputVector=new ArrayList<InputLine>();
	
	
	
	private EvComboObjectOne<TimeRemap> objectCombo=new EvComboObjectOne<TimeRemap>(new TimeRemap(),false,true);



	

	/**
	 * Make a new window
	 */
	public TimeRemapWindow()
		{				
		bAdd.addActionListener(this);
		bRefresh.addActionListener(this);
		objectCombo.addActionListener(this);
		
		XYDataset xyDataset = new XYSeriesCollection(frametimeSeries);
				
		JFreeChart chart = ChartFactory.createXYLineChart
            ("","New time","Original time",xyDataset,PlotOrientation.HORIZONTAL,false/*legend*/, false/*tooltips*/, false/*urls*/);
		ChartPanel graphpanel = new ChartPanel(chart);
		
		//Put GUI together
		JPanel datapanel=new JPanel(new BorderLayout());
		JPanel dataparto=new JPanel(new BorderLayout());
		dataparto.add(datapart,BorderLayout.NORTH);
		JScrollPane datapartscroll=new JScrollPane(dataparto, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		JPanel buttonpanel=new JPanel(new GridLayout(1,2));
		buttonpanel.add(bAdd);
		buttonpanel.add(bRefresh);
		datapanel.add(buttonpanel, BorderLayout.SOUTH);
		datapanel.add(datapartscroll, BorderLayout.CENTER);
		setLayout(new BorderLayout());
		add(datapanel, BorderLayout.EAST);		
		
		JPanel leftPanel=new JPanel(new BorderLayout());
		leftPanel.add(graphpanel,BorderLayout.CENTER);
		leftPanel.add(objectCombo, BorderLayout.SOUTH);
		add(leftPanel,BorderLayout.CENTER);
		
		
		loadData();
		
		//Window overall things
		setTitleEvWindow("Time remapper");
		packEvWindow();
		setBoundsEvWindow(new Rectangle(100,100,1000,600));
		setVisibleEvWindow(true);
		}

	private class SpinnerFrameModelFT extends JSpinnerFrameModel
		{
		public EvDecimal lastFrame(EvDecimal currentFrame){return currentFrame.subtract(1);}
		public EvDecimal nextFrame(EvDecimal currentFrame){return currentFrame.add(1);}
		}
	
	/**
	 * Add an entry. Does not update UI
	 */
	private void addEntry(EvDecimal frame, EvDecimal time)
		{
		TimeRemap meta=objectCombo.getSelectedObject();
		if(meta!=null)
			{
			InputLine inp=new InputLine();
			
			inp.frame=new JSpinner(new SpinnerFrameModelFT());
			inp.time=new JSpinner(new SpinnerFrameModelFT());
			
			EvFrameEditor frameEditor=new EvFrameEditor(inp.frame);
			EvFrameEditor frameEditor2=new EvFrameEditor(inp.time);
			
			inp.frame.setEditor(frameEditor);
			inp.time.setEditor(frameEditor2);
			
			inp.frame.setValue(frame);
			inp.time.setValue(time);
			
			inputVector.add(inp);
			
			inp.frame.addChangeListener(this);
			inp.time.addChangeListener(this);
			inp.bDelete.addActionListener(this);
			}
		else
			showErrorDialog("No object selected");
		}
	
	
	
	/**
	 * Load data from object
	 */
	private void loadData()
		{
		inputVector.clear();
		
		TimeRemap meta=objectCombo.getSelectedObject();
		if(meta!=null)
			for(Tuple<EvDecimal,EvDecimal> p:meta.list)
				addEntry(p.fst(), p.snd());
		
		fillGraphpart();
		fillDatapart();
		}
	
	
	
	private void storeData()
		{
		//Not real-time updates? this goes counter to the rest of EV
		TimeRemap meta=objectCombo.getSelectedObject();
		if(meta!=null)
			{
			meta.list.clear();
			for(int i=0;i<inputVector.size();i++)
				meta.add((EvDecimal)inputVector.get(i).frame.getValue(), (EvDecimal)inputVector.get(i).time.getValue());
			meta.updateMaps();
			meta.setMetadataModified();
			}
		}
	

	/**
	 * Regenerate all points in the graph
	 */
	private void fillGraphpart()
		{
		frametimeSeries.clear();
		for(int i=0;i<inputVector.size();i++)
			{
			EvDecimal frame=(EvDecimal)inputVector.get(i).frame.getValue();
			EvDecimal time=(EvDecimal)inputVector.get(i).time.getValue();
			frametimeSeries.add(time.doubleValue(), frame.doubleValue());
			}
		//would we want to list special times here?
		}	
	
	/**
	 * Regenerate UI
	 */
	private void fillDatapart()
		{
		datapart.removeAll();
		datapart.setLayout(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.gridy=0;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.weightx=1;
		c.gridx=0;
		datapart.add(new JLabel("Original"),c);
		c.gridx=1;
		datapart.add(new JLabel("New"),c);
		for(int i=0;i<inputVector.size();i++)
			{
			c.gridy++;
			c.fill=GridBagConstraints.HORIZONTAL;
			c.weightx=1;
			c.gridx=0;
			datapart.add(inputVector.get(i).frame,c);
			c.gridx=1;
			datapart.add(inputVector.get(i).time,c);
			c.gridx=2;
			c.fill=0;
			c.weightx=0;
			datapart.add(inputVector.get(i).bDelete,c);
			}
		setVisibleEvWindow(true);
		}
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==objectCombo)
			{
			loadData();
			}
		if(e.getSource()==bAdd)
			{
			addEntry(EvDecimal.ZERO, EvDecimal.ZERO);
			fillGraphpart();
			fillDatapart();
			storeData();
			}
		else if(e.getSource()==bRefresh)
			{
			loadData();
			}
			
		for(int i=0;i<inputVector.size();i++)
			if(inputVector.get(i).bDelete==e.getSource())
				{
				//TODO non-optimal. keep track of entry in map. then push through change right away
				storeData();
				inputVector.remove(i);
				fillGraphpart();
				fillDatapart();
				}

		}
	
	
	public void stateChanged(ChangeEvent e)
		{
		storeData();
		fillGraphpart();
		}
	
	public void dataChangedEvent()
		{
		objectCombo.updateList();
		loadData();
		}
	
	
	public void windowEventUserLoadedFile(EvData data){}
	public void windowFreeResources(){}
	public void windowSavePersonalSettings(Element root)
		{
		}
	public void windowLoadPersonalSettings(Element e)
		{
		}

	@Override
	public String windowHelpTopic()
		{
		return "The time remapper";
		}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new EvBasicWindowExtension()
					{
					public void newBasicWindow(EvBasicWindow w)
						{
						w.addHook(this.getClass(),new Hook());
						}
					class Hook implements EvBasicWindowHook, ActionListener
						{
						public void createMenus(EvBasicWindow w)
							{
							JMenuItem mi=new JMenuItem("Time remapper",new ImageIcon(getClass().getResource("iconWindow.png")));
							mi.addActionListener(this);
							w.addMenuWindow(mi);
							}
						
						public void actionPerformed(ActionEvent e) 
							{
							new TimeRemapWindow();
							}
						
						public void buildMenu(EvBasicWindow w){}
						}
					});
		
		
		
		Viewer2DWindow.addImageWindowExtension(new Viewer2DExtension()
			{
			public void newImageWindow(Viewer2DWindow w)
				{
				w.addImageWindowTool(new TimeRemapImageTool(w));
				}
			});
		}

	}

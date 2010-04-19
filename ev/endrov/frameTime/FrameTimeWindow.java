/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.frameTime;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;

import endrov.basicWindow.*;
import endrov.basicWindow.icon.BasicIcon;
import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.PersonalConfig;
import endrov.imageWindow.*;
import endrov.util.*;

import org.jdom.*;

//TODO: auto-replicate down to metadata

/**
 * Adjust Frame-Time mapping
 * @author Johan Henriksson
 */
public class FrameTimeWindow extends BasicWindow implements ActionListener, ChangeListener
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
	private JButton bAdd=new JButton("Add");
	private JButton bApply=new JButton("Apply");
	private JButton bRefresh=new JButton("Refresh");
	
	private JPanel datapart=new JPanel();
	private XYSeries frametimeSeries=new XYSeries("FT");
	private Vector<InputLine> inputVector=new Vector<InputLine>();
	
	
	
	private EvComboObjectOne<FrameTime> objectCombo=new EvComboObjectOne<FrameTime>(new FrameTime(),false,true);

	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element root)
		{
		Element e=new Element("frametimewindow");
		setXMLbounds(e);
		root.addContent(e);
		}

	

	/**
	 * Make a new window at default location
	 */
	public FrameTimeWindow()
		{
		this(new Rectangle(100,100,1000,600));
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public FrameTimeWindow(Rectangle bounds)
		{				
		bAdd.addActionListener(this);
		bApply.addActionListener(this);
		bRefresh.addActionListener(this);
		objectCombo.addActionListener(this);
		
		XYDataset xyDataset = new XYSeriesCollection(frametimeSeries);
				
		JFreeChart chart = ChartFactory.createXYLineChart
            ("","Time","Frame",xyDataset,PlotOrientation.HORIZONTAL,false/*legend*/, false/*tooltips*/, false/*urls*/);
		ChartPanel graphpanel = new ChartPanel(chart);
		
		//Put GUI together
		JPanel datapanel=new JPanel(new BorderLayout());
		JPanel dataparto=new JPanel(new BorderLayout());
		dataparto.add(datapart,BorderLayout.NORTH);
		JScrollPane datapartscroll=new JScrollPane(dataparto, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		JPanel buttonpanel=new JPanel(new GridLayout(2,2));
		buttonpanel.add(bAdd);
		buttonpanel.add(bApply);
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
		setTitleEvWindow("Frame/Time");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		}

	private class SpinnerFrameModelFT extends SpinnerFrameModel
		{
		public EvDecimal lastFrame(EvDecimal currentFrame){return currentFrame.subtract(1);}
		public EvDecimal nextFrame(EvDecimal currentFrame){return currentFrame.add(1);}
		}
	
	/**
	 * Add an entry. Does not update UI
	 */
	public void addEntry(EvDecimal frame, EvDecimal time)
		{
		FrameTime meta=objectCombo.getSelectedObject();
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
			
			
			for(int i=0;i<2;i++)
				{
				
	//			field[i]=new JSpinner(new EvDecimalSpinnerModel());
//				field[i].setEditor(new EvDecimalEditor(field[i]));
				}
			inputVector.add(inp);
			inp.frame.addChangeListener(this);
			inp.time.addChangeListener(this);
			inp.bDelete.addActionListener(this);
			}
		}
	
	/**
	 * Load data from SQL
	 */
	public void loadData()
		{
		inputVector.clear();
		
		FrameTime meta=objectCombo.getSelectedObject();
		if(meta!=null)
			for(Tuple<EvDecimal,EvDecimal> p:meta.list)
				addEntry(p.fst(), p.snd());
		
		fillGraphpart();
		fillDatapart();
		}
	
	
	
	public void applyData()
		{
		//Not real-time updates? this goes counter to the rest of EV
		FrameTime meta=objectCombo.getSelectedObject();
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
	 * Save data to text file
	 */
	/*
	public void saveTextData()
		{
		JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showSaveDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION)
			{
			String filename=chooser.getSelectedFile().getAbsolutePath();

			FrameTime frametime=objectCombo.getSelectedObject();
			if(frametime!=null)
				{
				for(int i=0;i<inputVector.size();i++)
					frametime.add((EvDecimal)inputVector.get(i)[0].getValue(), (EvDecimal)inputVector.get(i)[1].getValue());
				frametime.storeTextFile(filename);
				}
			}
		}
/*

	/**
	 * Regenerate all points in the graph
	 */
	public void fillGraphpart()
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
	public void fillDatapart()
		{
		datapart.removeAll();
		datapart.setLayout(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.gridy=0;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.weightx=1;
		c.gridx=0;
		datapart.add(new JLabel("Frame"),c);
		c.gridx=1;
		datapart.add(new JLabel("Time"),c);
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
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
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
			}
		else if(e.getSource()==bRefresh)
			{
			loadData();
			}
		else if(e.getSource()==bApply)
			{
			applyData();
			}
			
		for(int i=0;i<inputVector.size();i++)
			if(inputVector.get(i).bDelete==e.getSource())
				{
				//TODO non-optimal. keep track of entry in map. then push through change right away
				inputVector.remove(i);
				fillGraphpart();
				fillDatapart();
				}

		}
	
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
			/*
			if(inputVector.get(i).frame==e.getSource())
				{
				//Negative frame = delete
				//TODO really bad. add delete button
				EvDecimal f=(EvDecimal)inputVector.get(i).frame.getValue();
				if(f.equals(new EvDecimal("-1")))
					{
					inputVector.remove(i);
					fillGraphpart();
					fillDatapart();
					}
				}
				*/		
		
		fillGraphpart();
		}
	
	public void dataChangedEvent()
		{
		objectCombo.updateList();
		loadData();
		}
	
	
	public void loadedFile(EvData data){}
	public void freeResources(){}


	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new FrameTimeBasic());
		
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindowInterface w)
				{
				w.addImageWindowTool(new FrameTimeImageTool(w));
				}
			});
		
		EV.personalConfigLoaders.put("frametimewindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try	{new FrameTimeWindow(BasicWindow.getXMLbounds(e));}
				catch (Exception e1) {e1.printStackTrace();}
				}
			public void savePersonalConfig(Element e){}
			});
		
		}

	}

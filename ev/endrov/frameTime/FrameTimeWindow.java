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
import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.PersonalConfig;
import endrov.imageWindow.*;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

import org.jdom.*;

//TODO: auto-replicate down to metadata

/**
 * Adjust Frame-Time mapping
 * @author Johan Henriksson
 */
public class FrameTimeWindow extends BasicWindow implements ActionListener, ChangeListener
	{
	static final long serialVersionUID=0;

	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new FrameTimeBasic());
		
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				w.imageWindowTools.add(new FrameTimeImageTool(w));
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
	
	
	//GUI components
	private JButton bAdd=new JButton("Add");
	private JButton bApply=new JButton("Apply");
	private JButton bRefresh=new JButton("Refresh");
	
	private JPanel datapart=new JPanel();
	private XYSeries frametimeSeries=new XYSeries("FT");
	private Vector<JSpinner[]> inputVector=new Vector<JSpinner[]>();
	
	private EvComboObjectOne<FrameTime> objectCombo=new EvComboObjectOne<FrameTime>(new FrameTime(),false,true);

	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
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


	
	/**
	 * Add an entry. Does not update UI
	 */
	public void addEntry(EvDecimal frame, EvDecimal time)
		{
		FrameTime meta=objectCombo.getSelectedObject();
		if(meta!=null)
			{
			JSpinner field[]=new JSpinner[2];
			field[0]=new JSpinner(new EvDecimalSpinnerModel());
			field[1]=new JSpinner(new EvDecimalSpinnerModel());
			inputVector.add(field);
			field[0].addChangeListener(this);
			field[1].addChangeListener(this);
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
				meta.add((EvDecimal)inputVector.get(i)[0].getValue(), (EvDecimal)inputVector.get(i)[1].getValue());
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
			double frame=(Integer)inputVector.get(i)[0].getValue();
			double time=(Double)inputVector.get(i)[1].getValue();
			frametimeSeries.add(time, frame);
			}
		//would we want to list special times here?
		}	
	
	/**
	 * Regenerate UI
	 */
	public void fillDatapart()
		{
		datapart.removeAll();
		datapart.setLayout(new GridLayout(1+inputVector.size(),2));
		datapart.add(new JLabel("Frame"));
		datapart.add(new JLabel("Time"));
		for(int i=0;i<inputVector.size();i++)
			{
			datapart.add(inputVector.get(i)[0]);
			datapart.add(inputVector.get(i)[1]);
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
//			loadData();
			}
	/*	else if(e.getSource()==bSaveText)
			{
			
			}*/
			
		
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		for(int i=0;i<inputVector.size();i++)
			if(inputVector.get(i)[0]==e.getSource())
				{
				//Negative frame = delete
				if((Integer)inputVector.get(i)[0].getValue()==-1)
					{
					inputVector.remove(i);
					fillGraphpart();
					fillDatapart();
					}
				}		
		
		fillGraphpart();
		}
	
	public void dataChangedEvent()
		{
		objectCombo.updateList();
		loadData();
		}
	
	
	public void loadedFile(EvData data){}
	public void freeResources(){}

	
	}

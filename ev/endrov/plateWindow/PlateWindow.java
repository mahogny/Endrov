/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.plateWindow;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.event.*;
import org.jdom.*;

import endrov.basicWindow.*;
import endrov.basicWindow.icon.BasicIcon;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvPath;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.ev.PersonalConfig;
import endrov.flow.Flow;
import endrov.imageWindow.FrameControlImage;
import endrov.imageset.*;
import endrov.imagesetBD.EvIODataBD;
import endrov.particleMeasure.ParticleMeasure;
import endrov.particleMeasure.ParticleMeasure.Well;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;
import endrov.util.SnapBackSlider;

/**
 * Plate window - For high-throughput analysis
 *
 * @author Johan Henriksson
 */
public class PlateWindow extends BasicWindow implements ChangeListener, ActionListener
	{	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;
	
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element root)
		{
		}

	private static ImageIcon iconLabelBrightness=new ImageIcon(FrameControlImage.class.getResource("labelBrightness.png"));
	private static ImageIcon iconLabelContrast=new ImageIcon(FrameControlImage.class.getResource("labelContrast.png"));
	private static ImageIcon iconLabelFitRange=new ImageIcon(FrameControlImage.class.getResource("labelFitRange.png"));

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private final EvComboObject comboData=new EvComboObject(new LinkedList<EvObject>(), true, true)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return true;
			}
		};
	private final EvComboObjectOne<ParticleMeasure> comboFeature=new EvComboObjectOne<ParticleMeasure>(new ParticleMeasure(), true, true);
	private final EvComboObjectOne<Flow> comboFlow=new EvComboObjectOne<Flow>(new Flow(), true, true);
	private JComboBox comboAttribute1=new JComboBox();
	private JComboBox comboAttribute2=new JComboBox();
	private JComboBox comboChannel=new JComboBox();
	private JComboBox comboDisplay=new JComboBox(PlateWindowView.getAggrModes());
	private final FrameControlImage frameControl=new FrameControlImage(this, false, true);
	private PlateWindowView imagePanel=new PlateWindowView();	
	private ChannelWidget cw=new ChannelWidget();

	private boolean disableDataChanged=false;

	private final JMenu menuPlateWindow=new JMenu("PlateWindow");
	private final JMenuItem miZoom=new JMenuItem("Zoom to fit");
	private final JMenuItem miExportCSV=new JMenuItem("Export as CSV");
	private final JMenuItem miEvaluate=new JMenuItem("Evaluate flow");
	
	
	
	/**
	 * Make a new window at given location
	 */
	
	public PlateWindow(Rectangle bounds)
		{
		cw.updatePanelContrastBrightness();
		
		//Add listeners
		comboData.addActionListener(this);
		comboFeature.addActionListener(this);
		comboFlow.addActionListener(this);
		comboDisplay.addActionListener(this);
		comboAttribute1.addActionListener(this);
		comboAttribute2.addActionListener(this);
		comboChannel.addActionListener(this);

		
		addMenubar(menuPlateWindow);

		//TODO right-click "open in image window"

		//Do the main layout
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutLCR(
				null, 
				imagePanel, 
				EvSwingUtil.layoutACB(
						EvSwingUtil.layoutCompactVertical(
								EvSwingUtil.withTitledBorder("Data location",
										EvSwingUtil.layoutCompactVertical(
												new JLabel("Images:"),
												comboData,
												new JLabel("Flow for computation:"),
												comboFlow,
												new JLabel("Measure values:"),
												comboFeature)),
								EvSwingUtil.withTitledBorder("Display",
										EvSwingUtil.layoutCompactVertical(
												new JLabel("Channel:"),
												comboChannel,
												new JLabel("Primary attribute:"),
												comboAttribute1,
												new JLabel("Secondary attribute:"),
												comboAttribute2,
												new JLabel("Display:"),
												comboDisplay)
												)),
						null,
						null
						)),
				BorderLayout.CENTER);
		add(
				EvSwingUtil.layoutLCR(
					frameControl,
					cw,
					null),
				BorderLayout.SOUTH);

		//Misc
		attachDragAndDrop(imagePanel);
		packEvWindow();
		frameControl.setFrame(EvDecimal.ZERO);
		setBoundsEvWindow(bounds);
		setVisibleEvWindow(true);
		dataChangedEvent();
		}

	
	/**
	 * Rebuild ImageWindow menu
	 */
	private void buildMenu()
		{
		EvSwingUtil.tearDownMenu(menuPlateWindow);
		
			
		menuPlateWindow.add(miZoom);
		menuPlateWindow.add(miExportCSV);
		menuPlateWindow.add(miEvaluate);
		
		miZoom.addActionListener(this);
		miExportCSV.addActionListener(this);
		miEvaluate.addActionListener(this);

		}
	

	

	/**
	 * Get the zoom factor not including the binning
	 */
	public double getZoom()
		{
		return imagePanel.getZoom();
		}
	
	/**
	 * Set the zoom factor not including the binning 
	 */
	public void setZoom(double zoom)
		{
		imagePanel.setZoom(zoom);
		}
	
	/** Get rotation of image, in radians */
	public double getRotation()
		{
		return imagePanel.getRotation();
		}
	/** Set rotation of image, in radians */
	public void setRotation(double angle)
		{
		imagePanel.setRotation(angle);
		}

	
	
	/**
	 * One row of channel settings in the GUI
	 */
	public class ChannelWidget extends JPanel implements ActionListener, ChangeListener, SnapBackSlider.SnapChangeListener
		{
		static final long serialVersionUID=0;
		
		private final SnapBackSlider sliderContrast=new SnapBackSlider(SnapBackSlider.HORIZONTAL, -10000,10000);
		private final SnapBackSlider sliderBrightness=new SnapBackSlider(SnapBackSlider.HORIZONTAL, -200,200);
		
		private final JImageButton bFitRange=new JImageButton(iconLabelFitRange,"Fit range");
		
		
		
		public ChannelWidget()
			{
			setLayout(new GridLayout(1,4));
		
			JPanel contrastPanel=new JPanel(new BorderLayout());
			contrastPanel.setBorder(BorderFactory.createEtchedBorder());
			contrastPanel.add(new JLabel(iconLabelContrast), BorderLayout.WEST);
			contrastPanel.add(sliderContrast,BorderLayout.CENTER);

			JPanel brightnessPanel=new JPanel(new BorderLayout());
			brightnessPanel.setBorder(BorderFactory.createEtchedBorder());
			brightnessPanel.add(new JLabel(iconLabelBrightness), BorderLayout.WEST);
			brightnessPanel.add(sliderBrightness,BorderLayout.CENTER);

			add(contrastPanel);
			add(EvSwingUtil.layoutLCR(
					null,
					brightnessPanel,
					EvSwingUtil.layoutEvenHorizontal(bFitRange)
					));

			bFitRange.addActionListener(this);
			
			sliderContrast.addSnapListener(this);
			sliderBrightness.addSnapListener(this);
			}
		

		
		private double brightness=0;
		private double contrast=0.1;

		public void slideChange(SnapBackSlider source, int change)
			{
			if(source==sliderBrightness)
				{
				brightness+=change;
				}
			else if(source==sliderContrast)
				{
				contrast*=Math.pow(2,change/1000.0);
				}
			updatePanelContrastBrightness();
			}
	
		public void updatePanelContrastBrightness()
			{
			imagePanel.setContrastBrightness(contrast, brightness);
			}
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bFitRange)
				fitRange();
			else
				imagePanel.layoutImagePanel();
			}
		
		public void fitRange()
			{
			PlateWindowView.ValueRange range=imagePanel.getIntensityRange();
			if(range!=null)
				{
				contrast=255.0/(range.max-range.min);
				brightness=-range.min*contrast;
				updatePanelContrastBrightness();
				}
			}
		
		public void stateChanged(ChangeEvent e)
			{
			imagePanel.layoutImagePanel();
			}	

		public void resetSettings()
			{
			brightness=1;
			contrast=1;
			}
		}	


	
	
	
	public void updateWindowTitle()
		{
		setTitleEvWindow("Plate Analysis Window");
		}
	
	
	
	
	/**
	 * Called whenever data has been updated
	 */
	public void dataChangedEvent()
		{
		if(!disableDataChanged)
			{
			disableDataChanged=true;
			buildMenu();

			comboData.updateList();
			comboFeature.updateList();
			comboFlow.updateList();

			updateWells();
			

			List<String> alist=getAttributes();
			updateCombo(comboAttribute1,alist);
			updateCombo(comboAttribute2,alist);

			updateWindowTitle();
			
			disableDataChanged=false;
			}
		}

	
	/**
	 * Update which wells exist, and their content
	 */
	public void updateWells()
		{
		imagePanel.clearWells();
		EvContainer con=comboData.getSelectedObject();

		imagePanel.setParticleMeasure(getParticleMeasure());
		
		EvPath pathToFlow=comboFlow.getSelectedPath();
		imagePanel.setFlow(pathToFlow);
		
		imagePanel.setAggrMethod(comboDisplay.getSelectedItem(), 
				(String)comboAttribute1.getSelectedItem(),
				(String)comboAttribute2.getSelectedItem());
		
		//TODO different channel, z, time etc?
		
		if(con!=null)
			{
			Map<EvPath, EvChannel> m=con.getIdObjectsRecursive(EvChannel.class);
			

			//Update channel combo
			TreeSet<String> chans=new TreeSet<String>();
			for(EvPath p:m.keySet())
				chans.add(p.getLeafName());
			LinkedList<String> listchans=new LinkedList<String>();
			listchans.addAll(chans);
			updateCombo(comboChannel, listchans);

			//Add wells to panel
			String curChannel=(String)comboChannel.getSelectedItem();
			if(curChannel==null)
				curChannel="";
			TreeSet<EvPath> wellPaths=new TreeSet<EvPath>();
			for(EvPath p:m.keySet())
				{
				String ch=p.getLeafName();
				if(ch.equals(curChannel))
					{
					EvPath path=p.getParent();
					wellPaths.add(path);
					imagePanel.addWell(path, m.get(p));
					}
				}
			}
		

		imagePanel.layoutWells();
		imagePanel.layoutImagePanel(); //TODO not always needed
		}

	

	/**
	 * Update the options of a combobox 
	 */
	private void updateCombo(JComboBox cb, List<String> alist)
		{
		//First check if anything has changed at all. If not, leave it be. This removes a lot of potential flicker
		if(!comboNeedsUpdate(cb, alist))
			{
			String item=(String)cb.getSelectedItem();
			cb.removeAllItems();
			for(String s:alist)
				cb.addItem(s);
			if(item!=null)
				{
				int index=alist.indexOf(item);
				if(index!=-1)
					cb.setSelectedIndex(index);
				}
			}
		}
	
	/**
	 * Check if the combobox contains the given list of items
	 */
	private boolean comboNeedsUpdate(JComboBox cb, List<String> alist)
		{
		if(cb.getItemCount()!=alist.size())
			return false;
		for(int i=0;i<cb.getItemCount();i++)
			{
			if(!cb.getItemAt(i).equals(alist.get(i)))
				return false;
			}
		return true;
		}
	
	
	/**
	 * Get available particle attributes
	 */
	private List<String> getAttributes()
		{
		LinkedList<String> list=new LinkedList<String>();
		ParticleMeasure pm=getParticleMeasure();
		if(pm!=null)
			{
			list.addAll(pm.getColumns());
			list.remove("source");
			}
		return list;
		}

	
	public ParticleMeasure getParticleMeasure()
		{
		return comboFeature.getSelectedObject();
		}
	
	
	/**
	 * Upon state changes, update the window
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		imagePanel.layoutImagePanel();
		}	
	
	
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==comboData || e.getSource()==comboChannel)
			dataChangedEvent();
		else if(e.getSource()==comboFeature)
			dataChangedEvent();
		else if(e.getSource()==comboFlow)        //In some of these cases, possible to do better?
			dataChangedEvent();
		else if(e.getSource()==comboChannel)
			dataChangedEvent();
		else if(e.getSource()==comboAttribute1 || e.getSource()==comboAttribute2 || e.getSource()==comboDisplay)
			dataChangedEvent();
		else if(e.getSource()==miEvaluate)
			{
			imagePanel.execFlowAllWell();
			dataChangedEvent();  //Overkill?
			}
		else if(e.getSource()==miZoom)
			imagePanel.zoomToFit();
		}

	
	
	public void eventUserLoadedFile(EvData data)
		{
		dataChangedEvent();
		}

	public void freeResources()
		{
		imagePanel.freeResources();
		}
	
		

	
	
	
	

	public EvDecimal getFrame()
		{
		return frameControl.getFrame();
		}


	public EvDecimal getZ()
		{
		return frameControl.getModelZ();
		}


	public void setFrame(EvDecimal frame)
		{
		frameControl.setFrame(frame);
		updateImagePanelFrameZ();
		}


	public void setZ(EvDecimal z)
		{
		frameControl.setZ(z);
		updateImagePanelFrameZ();
		}

	
	public void updateImagePanelFrameZ()
		{
		imagePanel.setFrameZ(getFrame(), getZ());
		}
	
	
	
	
	
	public static void main(String[] args)
		{
		
		EvLog.addListener(new EvLogStdout());
	/*	
		EV.loadPlugins();
*/
		new PlateWindow(new Rectangle(600,600));
		
//		EvData d=EvData.loadFile(new File("/media/753C-F3A6/20121001_plate1"));
		EvData d=new EvData();
		try
			{
			new EvIODataBD(d, new File("/win/images/20121001_plate1"));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		ParticleMeasure pm=new ParticleMeasure();
		d.metaObject.put("pm",pm);

		for(String letter:new String[]{"B","C","D","E","F"})
			for(String num:new String[]{"02","03","04","05","06","07","08","09","10"})
				{
				String wellName="#<unnamed>/"+letter+num;
//				m.put("source", );

				ParticleMeasure.Well pmw=new Well();
				pm.setWell(wellName, pmw);
				
				ParticleMeasure.Frame fi=new ParticleMeasure.Frame();
				pmw.setFrame(EvDecimal.ZERO, fi);
//				pm.addColumn("source");
				pm.addColumn("a");
				pm.addColumn("b");

				int id=0;
						for(int i=0;i<100;i++)
							{
							ParticleMeasure.Particle m=fi.getCreateParticle(id++);
							double r=Math.random();
							m.put("a", r);
							m.put("b", r+Math.random());
							}

				}
		
		EvData.registerOpenedData(d);
		
		
		}
	
	public void finalize()
		{
		System.out.println("removing image window");
		}

	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
				{
				public void newBasicWindow(BasicWindow w)
					{
					w.basicWindowExtensionHook.put(this.getClass(),new Hook());
					}
				class Hook implements BasicWindowHook, ActionListener
					{
					public void createMenus(BasicWindow w)
						{
						JMenuItem mi=new JMenuItem("Plate analysis",BasicIcon.iconImage);
						mi.addActionListener(this);
						w.addMenuWindow(mi);
						}
					
					public void actionPerformed(ActionEvent e) 
						{
						new PlateWindow(null);
						}
					
					public void buildMenu(BasicWindow w){}
					}
				});
		
		EV.personalConfigLoaders.put("platewindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try
					{
					PlateWindow win=new PlateWindow(BasicWindow.getXMLbounds(e));
					win.frameControl.setGroup(e.getAttribute("group").getIntValue());
					}
				catch (Exception e1){e1.printStackTrace();}
				}
			public void savePersonalConfig(Element e){}
			});
		
		}
	
	
	}


/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.plateWindow;

import java.util.HashMap;
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
import endrov.ev.PersonalConfig;
import endrov.flow.Flow;
import endrov.flowMeasure.ParticleMeasure;
import endrov.flowMeasure.ParticleMeasure.FrameInfo;
import endrov.imageWindow.FrameControlImage;
import endrov.imageset.*;
import endrov.imagesetBD.EvIODataBD;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;
import endrov.util.SnapBackSlider;

/**
 * Plate window - For high-throughput analysis
 *
 * @author Johan Henriksson
 */
public class PlateWindow extends BasicWindow 
implements ChangeListener, ActionListener
			
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

	EvComboObject comboData=new EvComboObject(new LinkedList<EvObject>(), true, true)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return true;
			}
		};
	EvComboObjectOne<ParticleMeasure> comboFeature=new EvComboObjectOne<ParticleMeasure>(new ParticleMeasure(), true, true);
	EvComboObjectOne<Flow> comboFlow=new EvComboObjectOne<Flow>(new Flow(), true, true);
	JComboBox comboAttribute1=new JComboBox();
	JComboBox comboAttribute2=new JComboBox();
	JComboBox comboChannel=new JComboBox();
	
	
	JComboBox comboAggregation=new JComboBox(PlateWindowView.getAggrModes());

	JButton bExport=new JButton("Export as CSV");

	
	private final FrameControlImage frameControl=new FrameControlImage(this, false, false);

	private final JMenu menuPlateWindow=new JMenu("PlateWindow");

	private PlateWindowView imagePanel=new PlateWindowView();
	
	private ChannelWidget cw=new ChannelWidget();

	
	
	/**
	 * Make a new window at given location
	 */
	
	public PlateWindow(Rectangle bounds)
		{


		comboData.addActionListener(this);
		comboFeature.addActionListener(this);
		comboFlow.addActionListener(this);
		comboAggregation.addActionListener(this);
		comboAttribute1.addActionListener(this);
		comboAttribute2.addActionListener(this);
		comboChannel.addActionListener(this);
		
		
		/*
		sliderZoom2.addSnapListener(new SnapChangeListener(){
			public void slideChange(SnapBackSlider source, int change){zoom(change/50.0);}
		});
*/		
		
				
//		attachDragAndDrop(imagePanel);
		
		//Window overall things
		
		addMenubar(menuPlateWindow);

		//TODO right-click "open in image window"

		
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
												new JLabel("Attribute display:"),
												comboAggregation)
												)),
						null,
						bExport
						)),
				BorderLayout.CENTER);
		
		add(
				EvSwingUtil.layoutCompactHorizontal(
					frameControl,
					cw),
				BorderLayout.SOUTH);
				
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
		
			
		
		}
	

	

	/**
	 * Get the zoom factor not including the binning
	 */
	public double getZoom()
		{
		return imagePanel.zoom;
		}
	
	/**
	 * Set the zoom factor not including the binning 
	 */
	public void setZoom(double zoom)
		{
		imagePanel.zoom=zoom;
		repaint();
		}
	
	/** Get rotation of image, in radians */
	public double getRotation()
		{
		return imagePanel.rotation;
		}
	/** Set rotation of image, in radians */
	public void setRotation(double angle)
		{
		imagePanel.rotation=angle;
		}

	
	
	/**
	 * One row of channel settings in the GUI
	 */
	public class ChannelWidget extends JPanel implements ActionListener, ChangeListener, SnapBackSlider.SnapChangeListener
		{
		static final long serialVersionUID=0;
		
//		private final EvComboChannel comboChannel=new EvComboChannel(false,false);
		
		private final SnapBackSlider sliderContrast=new SnapBackSlider(SnapBackSlider.HORIZONTAL, -10000,10000);
		private final SnapBackSlider sliderBrightness=new SnapBackSlider(SnapBackSlider.HORIZONTAL, -200,200);
		
//		private final EvComboColor comboColor=new EvComboColor(false, channelColorList, EvColor.white);
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

			/*
			add(EvSwingUtil.layoutLCR(
					null, 
					EvSwingUtil.layoutLCR(
							null,
							comboChannel,
							null),
					null));
			*/
			add(contrastPanel);
			add(EvSwingUtil.layoutLCR(
					null,
					brightnessPanel,
					EvSwingUtil.layoutEvenHorizontal(bFitRange)
					));

			
			
//			comboColor.addActionListener(this);
//			comboChannel.addActionListener(this);
	//		bRemoveChannel.addActionListener(this);
			bFitRange.addActionListener(this);
			
			sliderContrast.addSnapListener(this);
			sliderBrightness.addSnapListener(this);

			}
		

		
		double brightness=0;
		double contrast=1;

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
			imagePanel.layoutImagePanel();
			}
	
		
		public void actionPerformed(ActionEvent e)
			{
			/*
			if(e.getSource()==comboChannel)
				{
				frameControl.setChannel(getChannel()); //has been moved here
				frameControl.setAll(frameControl.getFrame(), frameControl.getZ());
				layoutImagePanel();
				}
			*/
/*			else if(e.getSource()==comboColor)
				updateImagePanel();
			else if(e.getSource()==bRemoveChannel)
				removeChannel(this);*/
			
			
			if(e.getSource()==bFitRange)
				fitRange();
			else
				imagePanel.layoutImagePanel();
			}
		
		public void fitRange()
			{
			//TODO
			}
		
		public void stateChanged(ChangeEvent e)
			{
			imagePanel.layoutImagePanel();
			}	
		
		public double getContrast()
			{
			return contrast;
			}
		
		public double getBrightness()
			{
			return brightness;
			}
		/*
		public EvColor getColor()
			{
			return comboColor.getEvColor();
			}
		*/
		
		/**
		 * Get channel, or null in case it fails (data outdated, or similar)
		 */
		/*
		public EvChannel getChannel()
			{
			return comboChannel.getSelectedObject();
			}
*/

		public void resetSettings()
			{
			brightness=1;
			contrast=1;
			}
		
		}	


	
	
	
	public void updateWindowTitle()
		{
		System.out.println("title");
		setTitleEvWindow("Plate Window");
		}
	
	
	private boolean disableDataChanged=false;
	
	
	/**
	 * Called whenever data has been updated
	 */
	public void dataChangedEvent()
		{
		if(!disableDataChanged)
			{
			disableDataChanged=true;
			System.out.println("data cahnge");
			buildMenu();

			comboData.updateList();
			comboFeature.updateList();
			comboFlow.updateList();

			updateAvailableWells();
			

			List<String> alist=getAttributes();
			updateAttrCombo(comboAttribute1,alist);
			updateAttrCombo(comboAttribute2,alist);

//			List<String> clist=getChannels();
//			updateAttrCombo(comboChannel, clist);
				

			updateWindowTitle();
//			imagePanel.zoomToFit(); //TODO
			
			
			disableDataChanged=false;
			}
		}

	
	public void updateAvailableWells()
		{
		imagePanel.wellMap.clear();
		EvContainer con=comboData.getSelectedObject();

		imagePanel.setPM(getParticleMeasure());
		imagePanel.setAggrMethod(comboAggregation.getSelectedItem(), 
				(String)comboAttribute1.getSelectedItem(),
				(String)comboAttribute2.getSelectedItem());
		
		if(con!=null)
			{
			Map<EvPath, EvChannel> m=con.getIdObjectsRecursive(EvChannel.class);
			
			TreeSet<String> chans=new TreeSet<String>();
			TreeSet<EvPath> wellPaths=new TreeSet<EvPath>();

			for(EvPath p:m.keySet())
				{
				EvPath path=p.getParent();
				wellPaths.add(path);
				chans.add(p.getLeafName());
				imagePanel.addWell(path, m.get(p));
				}

			//Update channel combo
			LinkedList<String> listchans=new LinkedList<String>();
			listchans.add("");
			listchans.addAll(chans);
			updateAttrCombo(comboChannel, listchans);

			
			}
		

		imagePanel.layoutWells();
		imagePanel.layoutImagePanel(); //TODO not always needed
		}

	

	private void updateAttrCombo(JComboBox cb, List<String> alist)
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
	
	/*
	private List<String> getChannels()
		{
		LinkedList<String> list=new LinkedList<String>();
		list.add("");
		list.add("foo chan");
		
		return list;
		}
*/
	
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
		else if(e.getSource()==comboAttribute1 || e.getSource()==comboAttribute2 || e.getSource()==comboAggregation)
			dataChangedEvent();
		}

	
	
	public void eventUserLoadedFile(EvData data)
		{
		List<Imageset> ims=data.getObjects(Imageset.class);
		if(!ims.isEmpty())
			{
			//EvComboChannel chw=getCurrentChannelWidget().comboChannel;
			
			//TODO try and set the last selected channel!
			//TODO store state of window
			
			//setSelectedObject(ims.get(0), getCurrentChannelWidget().comboChannel.lastSelectChannel);
			}
		}

	public void freeResources(){}
	
		

	
	
	
	

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
		}


	public void setZ(EvDecimal z)
		{
		frameControl.setZ(z);
		}

	

	
	
	
	
	
	public static void main(String[] args)
		{
		
	/*	
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();
*/
		new PlateWindow(new Rectangle(600,600));
		
//		EvData d=EvData.loadFile(new File("/media/753C-F3A6/20121001_plate1"));
		EvData d=new EvData();
		try
			{
			new EvIODataBD(d, new File("/media/753C-F3A6/20121001_plate1"));
			}
		catch (Exception e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
		
		ParticleMeasure pm=new ParticleMeasure();
		d.metaObject.put("pm",pm);
		
		FrameInfo fi=new FrameInfo();
		pm.setFrame(EvDecimal.ZERO, fi);
		pm.addColumn("source");
		pm.addColumn("a");
		pm.addColumn("b");

		int id=0;
		for(String letter:new String[]{"B","C","D","E","F"})
			for(String num:new String[]{"02","03","04","05","06","07","08","09","10"})
				for(int i=0;i<100;i++)
					{
					HashMap<String, Object> m=fi.getCreateParticle(id++);
					double r=Math.random();
					m.put("source", "#<unnamed>/"+letter+num);
					m.put("a", r);
					m.put("b", r+Math.random());
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
						JMenuItem mi=new JMenuItem("Plate",BasicIcon.iconImage);
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
		
		EV.personalConfigLoaders.put("imagewindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try
					{
					PlateWindow win=new PlateWindow(BasicWindow.getXMLbounds(e));
					win.frameControl.setGroup(e.getAttribute("group").getIntValue());
					//win.channelWidget.get(0).comboChannel.lastSelectChannel=e.getAttributeValue("lastSelectChannel");
					}
				catch (Exception e1){e1.printStackTrace();}
				}
			public void savePersonalConfig(Element e){}
			});
		
		}
	
	
	}


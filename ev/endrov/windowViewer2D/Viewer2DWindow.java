/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowViewer2D;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.*;
import javax.vecmath.*;

import org.jdom.*;

import endrov.core.log.EvLog;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.gui.EvDataGUI;
import endrov.gui.*;
import endrov.gui.component.EvComboColor;
import endrov.gui.component.EvHidableSidePaneRight;
import endrov.gui.component.JImageButton;
import endrov.gui.component.JImageToggleButton;
import endrov.gui.component.JSnapBackSlider;
import endrov.gui.component.JSnapBackSlider.SnapChangeListener;
import endrov.gui.icon.BasicIcon;
import endrov.gui.keybinding.*;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.typeImageset.*;
import endrov.typeImageset.gui.EvComboChannel;
import endrov.util.ProgressHandle;
import endrov.util.math.EvDecimal;
import endrov.windowConsole.*;
import endrov.windowViewer2D.Viewer2DView.ImagePanelImage;
import endrov.windowViewer2D.basicExt2.Viewer2DChannelHook;
import endrov.windowViewer2D.basicExtensions.ImageWindowToolChannelDisp;
import endrov.windowViewer2D.basicExtensions.ImageWindowToolEditImage;
import endrov.windowViewer2D.basicExtensions.ImageWindowToolPixelInfo;
import endrov.windowViewer2D.basicExtensions.ImageWindowToolScreenshot;

/**
 * Image window - Displays imageset with overlays. Data can be edited with tools, filters can be applied.
 *
 * @author Johan Henriksson
 */
public class Viewer2DWindow extends EvBasicWindow 
			implements ActionListener, MouseListener, MouseMotionListener, KeyListener, ChangeListener, MouseWheelListener, TimedDataWindowInterface, 
			Viewer2DInterface
	{	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;
	/** Registered extensions */
	public static final Vector<Viewer2DRendererExtension> imageWindowRendererExtensions=new Vector<Viewer2DRendererExtension>();
	private static Vector<Viewer2DWindowExtension> imageWindowExtensions=new Vector<Viewer2DWindowExtension>();


	
	private static final int KEY_STEP_BACK    =KeyBinding.register(new KeyBinding("2D viewer","Step back",'a'));
	private static final int KEY_STEP_FORWARD =KeyBinding.register(new KeyBinding("2D viewer","Step forward",'d'));
	private static final int KEY_STEP_UP      =KeyBinding.register(new KeyBinding("2D viewer","Step up",'w'));
	private static final int KEY_STEP_DOWN    =KeyBinding.register(new KeyBinding("2D viewer","Step down",'s'));
	private static final int KEY_HIDE_MARKINGS=KeyBinding.register(new KeyBinding("2D viewer","Hide markings",' '));

	private static ImageIcon iconLabelBrightness=new ImageIcon(BasicIcon.class.getResource("labelBrightness.png"));
	private static ImageIcon iconLabelContrast=new ImageIcon(BasicIcon.class.getResource("labelContrast.png"));
	private static ImageIcon iconLabelFitRange=new ImageIcon(BasicIcon.class.getResource("labelFitRange.png"));
	private static ImageIcon iconLabelIDColor=new ImageIcon(BasicIcon.class.getResource("labelIDcolor.png"));
	
	public static int snapDistance=10;
	
	
	private static EvColor[] channelColorList=new EvColor[]{
			EvColor.red,
			EvColor.magenta,
			EvColor.yellow,
			EvColor.white,
			EvColor.green,
			EvColor.cyan,
			EvColor.blue
	};

	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element root)
		{
		frameControl.storeSettings(root);
		}
	@Override
	public void windowLoadPersonalSettings(Element root)
		{
		frameControl.getSettings(root);
		}



	/** Register an extension to image window */
	public static void addImageWindowExtension(Viewer2DWindowExtension e)
		{
		imageWindowExtensions.add(e);
		}

	/** Register an extension to image window */
	public static void addImageWindowRendererExtension(Viewer2DRendererExtension e)
		{
		imageWindowRendererExtensions.add(e);
		}

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private final Vector<Viewer2DWindowHook> imageWindowHooks=new Vector<Viewer2DWindowHook>();
	

	
	/** Hide all markings for now; to quickly show without overlay */
	private boolean temporarilyHideMarkings=false;

	/** Keep track of last imageset for re-centering purposes */
	private EvContainer lastContainerRecenter=null;
	
	/** Last coordinate of the mouse pointer. Used to detect dragging distance. */
	private int mouseLastDragX=0, mouseLastDragY=0;
	/** Last coordinate of the mouse pointer. Used to detect moving distance. For event technical reasons,
	 * this requires a separate set of variables than dragging (or so it seems) */
	private int mouseLastX=0, mouseLastY=0;
	/** Current mouse coordinate. Used for repainting. */
	public int mouseCurX=0, mouseCurY=0;
	/** Flag if the mouse cursor currently is in the window */
	public boolean mouseInWindow=false;

	//GUI components
	private final JSnapBackSlider sliderZoom2=new JSnapBackSlider(JScrollBar.VERTICAL,0,1000);	
	private JSnapBackSlider sliderRotate=new JSnapBackSlider(JScrollBar.VERTICAL,0,1000);
	
	private final JImageButton bAddChannel=new JImageButton(BasicIcon.iconAdd, "Add a channel");
	
	private final ButtonGroup rChannelGroup=new ButtonGroup();
	private final Vector<ChannelWidget> channelWidget=new Vector<ChannelWidget>();
	private final FrameControl2D frameControl=new FrameControl2D(this, true, true);
	
	private final JPanel bottomPanel=new JPanel();

	private final JMenu menuImageWindow=new JMenu("2D Viewer");
	private final JCheckBoxMenuItem miToolNone=new JCheckBoxMenuItem("No tool");
	private final JMenuItem miZoomToFit=new JMenuItem("Zoom to fit");
	private final JMenuItem miReset=new JMenuItem("Reset view");
	private final JMenuItem miMiddleSlice=new JMenuItem("Go to middle slice");
	private final JMenuItem miSliceInfo=new JMenuItem("Get slice info");
	private final JCheckBoxMenuItem miShowOverlay=new JCheckBoxMenuItem("Show overlay",true);

	private final JPanel bottomPanelFirstRow=new JPanel(new BorderLayout());

	
	public final Vector<JComponent> sidePanelItems=new Vector<JComponent>();
	private final JPanel sidePanel=new JPanel(new GridBagLayout());

	private final EvHidableSidePaneRight sidePanelSplitPane;
	
	
	
	public List<ChannelWidget> getChannels()
		{
		return channelWidget;
		}
	
	
	/**
	 * The image panel extended with more graphics
	 */
	private Viewer2DView imagePanel=new Viewer2DView()
		{
		static final long serialVersionUID=0;
		public void paintComponent(Graphics g)
			{
			super.paintComponent(g);
			if(!isOverlayHidden() && miShowOverlay.isSelected())
				{
				if(checkIfTransformOk())
					{
					for(Viewer2DRenderer r:imageWindowRenderers)
						r.draw(g);
					if(currentTool!=null)
						currentTool.paintComponent(g);
					}
				else
					EvLog.printError("Bad transformation of image", null);
				}
			}
		};
		
	/**
	 * Acquire a screenshot
	 */	
	public BufferedImage getScreenshotOverlay()
		{
		BufferedImage bi = new BufferedImage(imagePanel.getWidth(),imagePanel.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		imagePanel.paintComponent(g);
		return bi;
		}
	
	public Map<String,BufferedImage> getScreenshotOriginal(ProgressHandle progh)
		{
		HashMap<String,BufferedImage> bims=new HashMap<String, BufferedImage>(); 
		if(!imagePanel.images.isEmpty())
			{
			for(int i=0;i<channelWidget.size();i++)
				{
				String chname=channelWidget.get(i).getChannelName();
				BufferedImage im=imagePanel.images.get(i).getImage().getPixels(progh).quickReadOnlyAWT();
				bims.put(chname, im);
				}
			
			return bims;
			}
		else
			return null;
		}

	
	/**
	 * One row of channel settings in the GUI
	 */
	public class ChannelWidget extends JPanel implements ActionListener, ChangeListener, JSnapBackSlider.SnapChangeListener
		{
		static final long serialVersionUID=0;
		
		private final JRadioButton rSelect=new JRadioButton();
		private final EvComboChannel comboChannel=new EvComboChannel(false,false);
		
		private final JSnapBackSlider sliderContrast=new JSnapBackSlider(JSnapBackSlider.HORIZONTAL, -10000,10000);
		private final JSnapBackSlider sliderBrightness=new JSnapBackSlider(JSnapBackSlider.HORIZONTAL, -200,200);
		
		private final EvComboColor comboColor=new EvComboColor(false, channelColorList, EvColor.white);
		private final JImageButton bRemoveChannel=new JImageButton(BasicIcon.iconRemove,"Remove channel");
		private final JImageButton bFitRange=new JImageButton(iconLabelFitRange,"Fit range");
		private final JImageToggleButton bIDcolor=new JImageToggleButton(iconLabelIDColor,"Assume colors are IDs - improves contrast");
		
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

			add(EvSwingUtil.layoutLCR(
					rSelect, 
					EvSwingUtil.layoutLCR(
							comboColor,
							comboChannel,
							null),
					null));
			add(contrastPanel);
			add(EvSwingUtil.layoutLCR(
					null,
					brightnessPanel,
					EvSwingUtil.layoutEvenHorizontal(bIDcolor, bFitRange, bRemoveChannel)
					));

			
			
			comboColor.addActionListener(this);
			comboChannel.addActionListener(this);
			bRemoveChannel.addActionListener(this);
			bFitRange.addActionListener(this);
			bIDcolor.addActionListener(this);
			
			sliderContrast.addSnapListener(this);
			sliderBrightness.addSnapListener(this);

			}
		

		
		double brightness=0;
		double contrast=1;

		public void slideChange(JSnapBackSlider source, int change)
			{
			if(source==sliderBrightness)
				{
				brightness+=change;
				}
			else if(source==sliderContrast)
				{
				contrast*=Math.pow(2,change/1000.0);
				}
			updateImagePanel();
			}

		public boolean colorAsID()
			{
			return bIDcolor.isSelected();
			}
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==comboChannel)
				{
				frameControl.setChannel(getCurrentChannel()); //has been moved here
				frameControl.setAll(frameControl.getFrame(), frameControl.getZ());
				updateImagePanel();
				}
			else if(e.getSource()==comboColor)
				updateImagePanel();
			else if(e.getSource()==bRemoveChannel)
				removeChannel(this);
			else if(e.getSource()==bFitRange)
				fitRange();
			else if(e.getSource()==bIDcolor)
				updateImagePanel();
			else
				updateImagePanel();
			}
		
		public void fitRange()
			{
			int index=channelWidget.indexOf(this);
			
			EvImagePlane evim=imagePanel.images.get(index).getImage();
			if(evim!=null)
				{
				//Find min and max intensity
				EvPixels p=evim.getPixels(null);
				double[] arr=p.convertToDouble(true).getArrayDouble();
				int w=p.getWidth();
				int h=p.getHeight();
				double lowest=Double.MAX_VALUE;
				double highest=Double.MIN_VALUE;
				for(int i=0;i<w*h;i++)
					{
					if(arr[i]<lowest)
						lowest=arr[i];
					if(arr[i]>highest)
						highest=arr[i];
//					System.out.println("pix "+arr[i]);
					}
				
				System.out.println("highest "+highest+" lowest "+lowest);
				
				//Calculate optimal settings
				contrast=255.0/(highest-lowest);
				brightness=-lowest*contrast;
				
				updateImagePanel();
				}
			}
		
		public void stateChanged(ChangeEvent e)
			{
			updateImagePanel();
			}	
		
		public double getContrast()
			{
			return contrast;
			}
		
		public double getBrightness()
			{
			return brightness;
			}
		
		public EvColor getColor()
			{
			return comboColor.getEvColor();
			}
		
		
		public String getChannelName()
			{
			return comboChannel.getChannelName();
			}
		
		
		
		/**
		 * Get channel, or null in case it fails (data outdated, or similar)
		 */
		public EvChannel getChannel()
			{
			return comboChannel.getSelectedObject();
			}

		public void resetSettings()
			{
			brightness=1;
			contrast=1;
			}
		
		}	

		
	/** 
	 * Extension: Tool 
	 */
	private final Vector<Viewer2DTool> imageWindowTools=new Vector<Viewer2DTool>();
	/** 
	 * Currently selected tool 
	 */
	private Viewer2DTool currentTool;

	
	
	public void setTool(Viewer2DTool tool)
		{
		if(this.currentTool!=null)
			this.currentTool.deselected();
		this.currentTool=tool;  
		buildMenu();
		}
	
	public void unsetTool()
		{
		currentTool=null;
		}
	
	public Viewer2DTool getTool()
		{
		return currentTool;
		}
	
	
	public void addImageWindowTool(Viewer2DTool tool)
		{
		imageWindowTools.add(tool);
		}
	
		
	/**
	 * Make new window at default location
	 */
	public Viewer2DWindow()
		{
		this(new Rectangle(0,25,800,650));
		}
	/**
	 * Make a new window at given location
	 */
	
	public Viewer2DWindow(Rectangle bounds)
		{
		for(Viewer2DRendererExtension e:imageWindowRendererExtensions)
			e.newImageWindow(this);
		for(Viewer2DWindowExtension e:imageWindowExtensions)
			e.newImageWindow(this);
				
		imagePanel.setFocusable(true);
		
		ChangeListener chListenerNoInvalidate=new ChangeListener()
			{public void stateChanged(ChangeEvent e) {updateImagePanelNoInvalidate();}};
		
		//Attach listeners
		imagePanel.addKeyListener(this);  //TODO
		imagePanel.addMouseListener(this);
		imagePanel.addMouseMotionListener(this);
		imagePanel.addMouseWheelListener(this);
		sliderZoom2.addSnapListener(new SnapChangeListener(){
			public void slideChange(JSnapBackSlider source, int change){zoom(change/50.0);}
		});
		
		miShowOverlay.addChangeListener(chListenerNoInvalidate);
		bAddChannel.addActionListener(this);
		
		
		sliderRotate.addSnapListener(new JSnapBackSlider.SnapChangeListener(){
		public void slideChange(JSnapBackSlider source, int change)
			{
			imagePanel.rotateCamera(change/200.0);
			}
		});

		
		//Piece GUI together
		JPanel bottomRight=new JPanel(new GridLayout(1,2));
		bottomRight.add(bAddChannel);
		bottomPanelFirstRow.add(frameControl,BorderLayout.CENTER);
		bottomPanelFirstRow.add(bottomRight,BorderLayout.EAST);

		//Build list of channel widgets
		ChannelWidget chWidget=new ChannelWidget();
		rChannelGroup.add(chWidget.rSelect);
		channelWidget.add(chWidget);
		chWidget.rSelect.setSelected(true);
		
		JPanel zoomPanel=new JPanel(new BorderLayout());
		zoomPanel.add(new JLabel(BasicIcon.iconLabelZoom), BorderLayout.NORTH);
		zoomPanel.add(sliderZoom2,BorderLayout.CENTER);
		
		JPanel rotatePanel=new JPanel(new BorderLayout());
		rotatePanel.add(new JLabel(BasicIcon.iconLabelRotate), BorderLayout.NORTH);
		rotatePanel.add(sliderRotate,BorderLayout.CENTER);
		
		JPanel rightPanel=new JPanel(new GridLayout(2,1));
		rightPanel.add(zoomPanel);
		rightPanel.add(rotatePanel);
		
		sidePanelSplitPane=new EvHidableSidePaneRight(imagePanel, sidePanel, true);

		
		setLayout(new BorderLayout());
		add(sidePanelSplitPane,BorderLayout.CENTER);
		add(bottomPanel,BorderLayout.SOUTH);
		add(rightPanel,BorderLayout.EAST);

		
		updateToolPanels();
		
		addMainMenubarWindowSpecific(menuImageWindow);
		buildMenu();
		
		attachDragAndDrop(imagePanel);
		
		//Window overall things
		for(ChannelWidget w:channelWidget)
			w.comboChannel.updateList();
		packEvWindow();
		setBoundsEvWindow(bounds);
		updateWindowTitle();
		setVisibleEvWindow(true);
		
		frameControl.setChannel(getCurrentChannel());
		frameControl.setFrame(EvDecimal.ZERO);
		updateImagePanel();
		}

	/**
	 * Add channel
	 */
	public void addChannel()
		{
		ChannelWidget chWidget=new ChannelWidget();
		rChannelGroup.add(chWidget.rSelect);
		channelWidget.add(chWidget);
		updateToolPanels();
		}

	/**
	 * Remove channel
	 */
	public void removeChannel(ChannelWidget chan)
		{
		if(channelWidget.size()>1)
			{
			channelWidget.remove(chan);
			rChannelGroup.remove(chan.rSelect);
			if(chan.rSelect.isSelected())
				channelWidget.get(0).rSelect.setSelected(true);
			updateToolPanels();
			updateImagePanel();
			}
		}

	/**
	 * Build list of channel widgets
	 */
	private void updateToolPanels()
		{
		//Assemble bottom panel
		bottomPanel.removeAll();
		bottomPanel.setLayout(new GridLayout(1+channelWidget.size(),1,0,0));
		bottomPanel.add(bottomPanelFirstRow);
		
		for(ChannelWidget w:channelWidget)
			bottomPanel.add(w);
		bottomPanel.setVisible(false);
		bottomPanel.setVisible(true);  //TODO invalidate or something
		
		//Update side panel
		sidePanelItems.clear();
		for(Viewer2DWindowHook h:imageWindowHooks)
			h.fillMenus(this);
		
		//Assemble side panel
		int counta=0;
		sidePanel.removeAll();
		for(JComponent c:sidePanelItems)
			{
			GridBagConstraints cr=new GridBagConstraints();	cr.gridy=counta;	cr.fill=GridBagConstraints.HORIZONTAL;
			cr.weightx=1;
			sidePanel.add(c,cr);
			counta++;
			}
		GridBagConstraints cr=new GridBagConstraints();	cr.gridy=counta;	cr.fill=GridBagConstraints.VERTICAL; cr.weighty=1;
		sidePanel.add(new JLabel(""),cr);
		sidePanel.revalidate();
		
		
		}
	
	
	/**
	 * Rebuild ImageWindow menu
	 */
	private void buildMenu()
		{
		EvSwingUtil.tearDownMenu(menuImageWindow);
		miReset.addActionListener(this);
		miMiddleSlice.addActionListener(this);
		miZoomToFit.addActionListener(this);
		miSliceInfo.addActionListener(this);
		miToolNone.addActionListener(this);
		
		//Window specific menu items
		menuImageWindow.add(miReset);
		menuImageWindow.add(miMiddleSlice);
		menuImageWindow.add(miZoomToFit);
		menuImageWindow.add(miShowOverlay);
		menuImageWindow.add(miSliceInfo);
		
		menuImageWindow.addSeparator();
		
		//List all tools. None first, then the other tools in alphabetical order
		menuImageWindow.add(miToolNone);
		miToolNone.setSelected(currentTool==null);
		List<JMenuItem> menuItems=new LinkedList<JMenuItem>();
		for(final Viewer2DTool t:imageWindowTools)
			{
			JMenuItem mi=t.getMenuItem();
			if(mi!=null)
				menuItems.add(mi);
			}
		Collections.sort(menuItems, new Comparator<JMenuItem>(){
			public int compare(JMenuItem arg0, JMenuItem arg1)
				{
				return arg0.getText().compareTo(arg1.getText());
				}
		});
		for(JMenuItem mi:menuItems)
			menuImageWindow.add(mi);
			
		//List custom tool as well
		if(!imageWindowTools.contains(currentTool) && !miToolNone.isSelected())
			menuImageWindow.add(currentTool.getMenuItem());
		
		}
	
	/**
	 * Get currently selected channel
	 */
	private ChannelWidget getCurrentChannelWidget()
		{
		for(ChannelWidget w:channelWidget)
			if(w.rSelect.isSelected())
				return w;
		return channelWidget.firstElement();
		}
	
	/**
	 * Get name of currently viewed channel
	 */
	public String getCurrentChannelName()
		{
		return getCurrentChannelWidget().comboChannel.getChannelName();
		}
	
	public EvChannel getCurrentChannel()
		{
		return getCurrentChannelWidget().comboChannel.getSelectedObject();
		}
	
	/**
	 * Get current metadata 
	 */
	public EvData getSelectedData()
		{
		return getCurrentChannelWidget().comboChannel.getData();
		}
	
	/** 
	 * Get current imageset or an empty one 
	 */
	public EvContainer getImageset()
		{
		EvContainer im=getCurrentChannelWidget().comboChannel.getImageset();
		if(im==null)
			return new EvContainer();
		else
			return im;
		}
	
	/**
	 * Get root object - were all objects to be displayed are located
	 */
	public EvContainer getRootObject()
		{
		EvContainer im=getCurrentChannelWidget().comboChannel.getImageset();
		if(im==null)
			return new EvContainer();
		else
			return im;
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
	/** Check if overlay should be hidden */
	public boolean isOverlayHidden()
		{
		return temporarilyHideMarkings;
		}

	



	
	
	/**
	 * Take current settings of sliders and apply it to image
	 */
	public void updateImagePanel()
		{
		//Set images
		imagePanel.images.clear();
		for(int i=0;i<channelWidget.size();i++)
			{
			ChannelWidget cw=channelWidget.get(i);

			EvChannel ch=cw.getChannel();
			
			
			//Imageset rec2=cw.comboChannel.getImageset();
			//String chname=cw.comboChannel.getChannelName();
			if(ch!=null)
			//if(rec2!=null && chname!=null)
				{
				//EvChannel ch=rec2.getChannel(chname);
				Viewer2DView.ImagePanelImage pi=new Viewer2DView.ImagePanelImage();
				pi.brightness=cw.getBrightness();//cw.sliderBrightness.getValue();
				pi.contrast=cw.getContrast();//Math.pow(2,cw.sliderContrast.getValue()/1000.0);
				pi.color=cw.getColor();
				pi.idcolor=cw.colorAsID();
				
				EvDecimal frame=frameControl.getFrame();
				EvDecimal z=frameControl.getZ();
				frame=ch.closestFrame(frame);
				
				EvStack stack=ch.getStack(new ProgressHandle(), frame);
				
				//System.out.println("---- got stack "+stack);
				
				if(stack==null)
					pi.setImage(null,null);
				else
					{
					int closestZ=stack.getClosestPlaneIndex(z.doubleValue());
					//System.out.println("----closest z: "+closestZ+"   depth:"+stack.getDepth());
					if(closestZ!=-1)
						{
						EvImagePlane evim=stack.getPlane(closestZ);
						//System.out.println("--- got stack 2: "+evim+"   "+evim.getPixels(null));
						
						if(evim!=null)
							pi.setImage(stack,evim);
						else
							{
							System.out.println("Image was null. ch:"+cw.getChannelName());
							}
						}
					else
						System.out.println("--z=-1 for ch:"+cw.getChannelName());
					}
				imagePanel.images.add(pi);
				}
			}
		
		imagePanel.invalidateImages();
		updateImagePanelNoInvalidate();
		}


	
	/**
	 * Update, but assume images are still ok
	 */
	public void updateImagePanelNoInvalidate()
		{				
		//Check if recenter needed
		boolean zoomToFit=false;
		EvContainer rec=getImageset();
		if(rec!=lastContainerRecenter)
			{
			zoomToFit=true;
			lastContainerRecenter=rec;
			frameControl.stepForward();
			}
		
		//Show new image
		imagePanel.repaint();

		if(zoomToFit)
			{
			imagePanel.zoomToFit();
			}

		updateWindowTitle();
		}
	
	public void updateWindowTitle()
		{
		setTitleEvWindow("2D viewer - "+channelWidget.get(0).comboChannel.getSelectedPath());
		}
	
	
	/**
	 * Called whenever data has been updated
	 */
	public void dataChangedEvent()
		{
		imagePanel.dataChangedEvent();
		for(ChannelWidget w:channelWidget)
			w.comboChannel.updateList();
		frameControl.setChannel(getCurrentChannel());//hm. does not cause cascade?
		buildMenu();
		updateImagePanel();
		}
	
	/**
	 * Upon state changes, update the window
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		updateImagePanel();
		}	
	
	
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		frameControl.setChannel(getCurrentChannel());
		
		if(e.getSource()==miReset)
			{
			for(ChannelWidget w:channelWidget)
				{
				w.resetSettings();
				}
			setRotation(0);
			updateImagePanel();
			}
		else if(e.getSource()==miMiddleSlice)
			{
			EvChannel ch=getCurrentChannel();//getImageset().getChannel(getCurrentChannelName());
			if(ch!=null)
				{
				EvDecimal curFrame=ch.closestFrame(frameControl.getFrame());
				if(ch.getStack(new ProgressHandle(), curFrame).getDepth()>0)
					{
					EvStack stack=ch.getStack(curFrame);
					frameControl.setZ(new EvDecimal(stack.transformImageWorldZ(stack.getDepth()/2)));
					updateImagePanel();
					}
				}
			}
		else if(e.getSource()==miZoomToFit)
			{
			imagePanel.zoomToFit();
			}
		else if(e.getSource()==miSliceInfo)
			{
			StringBuffer sb=new StringBuffer();
			for(ImagePanelImage im:imagePanel.images)
				{
				EvImagePlane evim=im.getImage();
				if(evim==null)
					sb.append("Null slice");
				else
					{
					EvStack stack=im.getStack();
					
					Vector3d disp=stack.getDisplacement();
					sb.append(
							"ResX: "+stack.getRes().x + " "+
							"ResY: "+stack.getRes().y + " "+
							"ResZ: "+stack.getRes().z + " "+
							"DX: "+disp.x + " "+
							"DY: "+disp.y + " "+
							"DZ: "+disp.z + " "+
							"Width(px): "+stack.getWidth()+" "+
							"Height(px): "+stack.getHeight() +" "+
							"Pixel format: "+stack.getPixelFormat()
							);
					sb.append("\n");
					}
				}
			String s=sb.toString();
			if(!s.equals(""))
				EvBasicWindow.showInformativeDialog(s);
			}
		else if(e.getSource()==miToolNone)
			setTool(null);
		else if(e.getSource()==bAddChannel)
			{
			addChannel();
//			setShow3Color(bShow3colors.isSelected());
			updateImagePanel();
			}
		else
			updateImagePanel();
		}

	
	
	
	/**
	 * Callback: Key pressed down
	 */
	public void keyPressed(KeyEvent e)
		{
//		System.out.println(e);
//		EV.printStackTrace("kpress");
		
		EvData data=getSelectedData();
		if(KeyBinding.get(KEY_HIDE_MARKINGS).typed(e))
			{
			temporarilyHideMarkings=true;
			repaint();
			}
		else if(KeyBinding.get(KEY_GETCONSOLE).typed(e))
			ConsoleWindow.focusConsole(this, imagePanel);
		else if(e.getKeyCode()==KeyEvent.VK_S && holdModifier1(e))
			{
			try
				{
				EvLog.printLog("Saving "+data.getMetadataName());
				data.saveData();
				}
			catch (IOException e1)
				{
				EvLog.printLog("Failed to save "+data.getMetadataName());
				}
			}
		else if(e.getKeyCode()==KeyEvent.VK_W && holdModifier1(e))
			{
			EvDataGUI.unregisterOpenedData(data);
			EvLog.printLog("Closing "+data.getMetadataName());
			}
		else if(e.getKeyCode()==KeyEvent.VK_0)
			{
			updateImagePanel();
			}
		else if(e.getKeyCode()==KeyEvent.VK_9)
			{
			updateImagePanelNoInvalidate();
			}
		else if(currentTool!=null)
			currentTool.keyPressed(e);
			
		}
	/**
	 * Callback: Key has been released
	 */
	public void keyReleased(KeyEvent e)
		{
//		System.out.println(e);
//		EV.printStackTrace("release");
		if(!ScriptBinding.runScriptKey(e))
			{
			if(KeyBinding.get(KEY_HIDE_MARKINGS).typed(e))
				{
				temporarilyHideMarkings=false;
				repaint();
				}
			else if(currentTool!=null)
				currentTool.keyReleased(e);
			}
		}
	/**
	 * Callback: Keyboard key typed (key down and up again)
	 */
	public void keyTyped(KeyEvent e)
		{
		if(KeyBinding.get(KEY_STEP_BACK).typed(e))
			frameControl.stepBack();
		else if(KeyBinding.get(KEY_STEP_FORWARD).typed(e))
			frameControl.stepForward();
		else if(KeyBinding.get(KEY_STEP_DOWN).typed(e))
			frameControl.stepDown();
		else if(KeyBinding.get(KEY_STEP_UP).typed(e))
			frameControl.stepUp();
		}
	/**
	 * Callback: Mouse button clicked
	 */
	public void mouseClicked(MouseEvent e)
		{
		if(currentTool!=null)
			currentTool.mouseClicked(e,imagePanel);
//		if(SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e))
		imagePanel.requestFocus();
		}
	/**
	 * Callback: Mouse button pressed
	 */
	public void mousePressed(MouseEvent e)
		{
		imagePanel.requestFocus();
		if(currentTool!=null)
			currentTool.mousePressed(e);
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		}
	/**
	 * Callback: Mouse button released
	 */
	public void mouseReleased(MouseEvent e)
		{
		if(currentTool!=null)
			currentTool.mouseReleased(e);
		}
	/**
	 * Callback: Mouse pointer has entered window
	 */
	public void mouseEntered(MouseEvent e)
		{
		mouseInWindow=true;
		}
	/**
	 * Callback: Mouse pointer has left window
	 */
	public void mouseExited(MouseEvent e)
		{
		mouseInWindow=false;
		if(currentTool!=null)
			currentTool.mouseExited(e);
		}
	/**
	 * Callback: Mouse moved
	 */
	public void mouseMoved(MouseEvent e)
		{
		int dx=e.getX()-mouseLastX;
		int dy=e.getY()-mouseLastY;
		mouseLastX=e.getX();
		mouseLastY=e.getY();
		mouseInWindow=true;
		mouseCurX=e.getX();
		mouseCurY=e.getY();
		
		//Handle tool specific feedback
		if(currentTool!=null)
			currentTool.mouseMoved(e,dx,dy);
		
		//Need to update currentHover so always repaint.
		imagePanel.repaint();
		}
	/**
	 * Callback: mouse dragged
	 */
	public void mouseDragged(MouseEvent e)
		{
		mouseInWindow=true;
		int dx=e.getX()-mouseLastDragX;
		int dy=e.getY()-mouseLastDragY;
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		if(SwingUtilities.isRightMouseButton(e))
			{
			imagePanel.pan(dx,dy);
			updateImagePanelNoInvalidate();
			}

		if(currentTool!=null)
			currentTool.mouseDragged(e,dx,dy);
		}
	/**
	 * Callback: Mouse scrolls
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
		{
		//TODO use e.getWheelRotation() only
		//Self-note: linux machine at home (mahogny) uses UNIT_SCROLL
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
			zoom(e.getUnitsToScroll()/5.0);
		else if(e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL)
			zoom(e.getUnitsToScroll()*2);
		}

	
	public void zoom(double val)
		{
		imagePanel.zoom*=Math.pow(10,val/10);
		repaint();
		}
	
	public void windowEventUserLoadedFile(EvData data)
		{
		List<EvChannel> ims=data.getObjects(EvChannel.class);
		if(!ims.isEmpty())
			{
			//EvComboChannel chw=getCurrentChannelWidget().comboChannel;
			
			//TODO try and set the last selected channel!
			//TODO store state of window
			
			//setSelectedObject(ims.get(0), getCurrentChannelWidget().comboChannel.lastSelectChannel);
			}
		}

	public void windowFreeResources(){}
	
	
	public void finalize()
		{
		System.out.println("removing image window");
		}
		

	
	
	
	
	
	
	
	
	/** 
	 * Scale screen vector to world vector 
	 */
	public double scaleS2w(double s)
		{
		return imagePanel.scaleS2w(s);
		}
	
	/**
	 * Scale world to screen vector 
	 */
	public double scaleW2s(double w) 
		{
		return imagePanel.scaleW2s(w);
		}


	
	//New functions, should replace the ones above at some point

	/** Transform world coordinate to screen coordinate */
	public Vector2d transformPointW2S(Vector2d u)
		{
		return imagePanel.transformPointW2S(u);
		}
		
	/** 
	 * Transform screen coordinate to world coordinate 
	 * NOTE: This means panning is not included! 
	 */
	public Vector2d transformPointS2W(Vector2d u)
		{
		return imagePanel.transformPointS2W(u);
		}

	/**
	 * Transform screen vector to world vector.
	 * NOTE: This means panning is not included! 
	 * 
	 */
	public Vector2d transformVectorS2W(Vector2d u)
		{
		return imagePanel.transformVectorS2W(u);
		}

	
	/** Convert world to screen Z coordinate */
	public double w2sz(double z)
		{
		return z;
		}
	
	/** Convert world to screen Z coordinate */
	public double s2wz(double sz) 
		{
		return sz;
		} 

	

	public void addImageWindowRenderer(Viewer2DRenderer r)
		{
		imagePanel.imageWindowRenderers.add(r);
		}


	@SuppressWarnings("unchecked")
	public <E> E getRendererClass(Class<E> cl)
		{
		for(Viewer2DRenderer r:imagePanel.imageWindowRenderers)
			if(cl.isInstance(r))
				return (E)r;
		throw new RuntimeException("No such renderer exists - " + cl);
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
		}


	public void setZ(EvDecimal z)
		{
		frameControl.setZ(z);
		}
	


	@Override
	public String windowHelpTopic()
		{
		return "The 2D viewer";
		}	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvBasicWindow.addBasicWindowExtension(
				new EvBasicWindowExtension()
				{
				public void newBasicWindow(EvBasicWindow w)
					{
					w.addHook(this.getClass(),new Hook());
					}
				class Hook implements EvBasicWindowHook, ActionListener
					{
					public void createMenus(EvBasicWindow w)
						{
						JMenuItem mi=new JMenuItem("2D viewer",BasicIcon.iconImage);
						mi.addActionListener(this);
						w.addMenuWindow(mi);
						}
					
					public void actionPerformed(ActionEvent e) 
						{
						new Viewer2DWindow();
						}
					
					public void buildMenu(EvBasicWindow w){}
					}
				});


		
		Viewer2DWindow.addImageWindowExtension(new Viewer2DWindowExtension()
			{
			public void newImageWindow(Viewer2DWindow w)
				{
				w.addImageWindowTool(new ImageWindowToolChannelDisp(w));
				w.addImageWindowTool(new ImageWindowToolScreenshot(w));
				w.addImageWindowTool(new ImageWindowToolPixelInfo(w));
				w.addImageWindowTool(new ImageWindowToolEditImage(w));


				w.imageWindowHooks.add(new Viewer2DChannelHook());
				
				}
			});
		
		}

	
	
	}


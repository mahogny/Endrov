/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageWindow;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.event.*;
import javax.vecmath.*;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.basicWindow.icon.BasicIcon;
import endrov.consoleWindow.*;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.PersonalConfig;
import endrov.imageWindow.ImageWindowView.ImagePanelImage;
import endrov.imageWindow.tools.ImageWindowToolChannelDisp;
import endrov.imageWindow.tools.ImageWindowToolEditImage;
import endrov.imageWindow.tools.ImageWindowToolPixelInfo;
import endrov.imageWindow.tools.ImageWindowToolScreenshot;
import endrov.imageset.*;
import endrov.keyBinding.*;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;
import endrov.util.SnapBackSlider;
import endrov.util.SnapBackSlider.SnapChangeListener;

/**
 * Image window - Displays imageset with overlays. Data can be edited with tools, filters can be applied.
 *
 * @author Johan Henriksson
 */
public class ImageWindow extends BasicWindow 
			implements ActionListener, MouseListener, MouseMotionListener, KeyListener, ChangeListener, MouseWheelListener, TimedDataWindow, 
			ImageWindowInterface
	{	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;
	/** Registered extensions */
	private static final Vector<ImageWindowExtension> imageWindowExtensions=new Vector<ImageWindowExtension>();
	public static final Vector<ImageWindowRendererExtension> imageWindowRendererExtensions=new Vector<ImageWindowRendererExtension>();

	private static final int KEY_STEP_BACK    =KeyBinding.register(new KeyBinding("Image Window","Step back",'a'));
	private static final int KEY_STEP_FORWARD =KeyBinding.register(new KeyBinding("Image Window","Step forward",'d'));
	private static final int KEY_STEP_UP      =KeyBinding.register(new KeyBinding("Image Window","Step up",'w'));
	private static final int KEY_STEP_DOWN    =KeyBinding.register(new KeyBinding("Image Window","Step down",'s'));
	private static final int KEY_HIDE_MARKINGS=KeyBinding.register(new KeyBinding("Image Window","Hide markings",' '));

	private static ImageIcon iconLabelBrightness=new ImageIcon(FrameControlImage.class.getResource("labelBrightness.png"));
	private static ImageIcon iconLabelContrast=new ImageIcon(FrameControlImage.class.getResource("labelContrast.png"));
	
	
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
		Element e=new Element("imagewindow");
		setXMLbounds(e);
		e.setAttribute("group", ""+frameControl.getGroup());
		String lastChan=channelWidget.get(0).comboChannel.lastSelectChannel;
		if(lastChan==null)
			lastChan="";
		e.setAttribute("lastSelectChannel", lastChan);
		root.addContent(e);
		}


	/** Register an extension to image window */
	public static void addImageWindowExtension(ImageWindowExtension e)
		{
		imageWindowExtensions.add(e);
		}

	/** Register an extension to image window */
	public static void addImageWindowRendererExtension(ImageWindowRendererExtension e)
		{
		imageWindowRendererExtensions.add(e);
		}

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	

	
	/** Hide all markings for now; to quickly show without overlay */
	private boolean temporarilyHideMarkings=false;

	/** Keep track of last imageset for re-centering purposes */
	private Imageset lastImagesetRecenter=null;
	
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
	private final SnapBackSlider sliderZoom2=new SnapBackSlider(JScrollBar.VERTICAL,0,1000);	
	private SnapBackSlider sliderRotate=new SnapBackSlider(JScrollBar.VERTICAL,0,1000);
	
	private final JImageButton bAddChannel=new JImageButton(BasicIcon.iconAdd, "Add a channel");
	
	private final ButtonGroup rChannelGroup=new ButtonGroup();
	private final Vector<ChannelWidget> channelWidget=new Vector<ChannelWidget>();
	private final FrameControlImage frameControl=new FrameControlImage(this);
	
	private final JPanel channelPanel=new JPanel();

	private final JMenu menuImageWindow=new JMenu("ImageWindow");
	private final JCheckBoxMenuItem miToolNone=new JCheckBoxMenuItem("No tool");
	private final JMenuItem miZoomToFit=new JMenuItem("Zoom to fit");
	private final JMenuItem miReset=new JMenuItem("Reset view");
	private final JMenuItem miMiddleSlice=new JMenuItem("Go to middle slice");
	private final JMenuItem miSliceInfo=new JMenuItem("Get slice info");
	private final JCheckBoxMenuItem miShowOverlay=new JCheckBoxMenuItem("Show overlay",true);

	private final JPanel bottomPanelFirstRow=new JPanel(new BorderLayout());

	
	public List<ChannelWidget> getChannels()
		{
		return channelWidget;
		}
	
	
	/**
	 * The image panel extended with more graphics
	 */
	private ImageWindowView imagePanel=new ImageWindowView()
		{
		static final long serialVersionUID=0;
		public void paintComponent(Graphics g)
			{
			super.paintComponent(g);
			if(!isOverlayHidden() && miShowOverlay.isSelected())
				{
				if(checkIfTransformOk())
					{
					for(ImageWindowRenderer r:imageWindowRenderers)
						r.draw(g);
					if(currentTool!=null)
						currentTool.paintComponent(g);
					}
				else
					EvLog.printError("Bad scale of image", null);
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
	
	public Map<String,BufferedImage> getScreenshotOriginal()
		{
		HashMap<String,BufferedImage> bims=new HashMap<String, BufferedImage>(); 
		if(!imagePanel.images.isEmpty())
			{
			for(int i=0;i<channelWidget.size();i++)
				{
				String chname=channelWidget.get(i).getChannelName();
				BufferedImage im=imagePanel.images.get(i).getImage().getPixels().quickReadOnlyAWT();
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
	public class ChannelWidget extends JPanel implements ActionListener, ChangeListener
		{
		static final long serialVersionUID=0;
		
		private final JRadioButton rSelect=new JRadioButton();
		private final EvComboChannel comboChannel=new EvComboChannel(null,false);
		private final JSlider sliderContrast=new JSlider(-10000,10000,0);
		private final JSlider sliderBrightness=new JSlider(-200,200,0);
		private final EvComboColor comboColor=new EvComboColor(false, channelColorList, EvColor.white);
		private final JImageButton bRemoveChannel=new JImageButton(BasicIcon.iconRemove,"Remove channel");
		
		
		
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
					bRemoveChannel));

			comboColor.addActionListener(this);
			sliderContrast.addChangeListener(this);
			sliderBrightness.addChangeListener(this);
			comboChannel.addActionListener(this);
			bRemoveChannel.addActionListener(this);
			}
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==comboChannel)
				{
				frameControl.setChannel(getImageset(), getCurrentChannelName()); //has been moved here
				frameControl.setAll(frameControl.getFrame(), frameControl.getZ());
				updateImagePanel();
				}
			else if(e.getSource()==comboColor)
				updateImagePanel();
			else if(e.getSource()==bRemoveChannel)
				removeChannel(this);
			else
				updateImagePanel();
			}
		
		public void stateChanged(ChangeEvent e)
			{
			updateImagePanel();
			}	
		
		public double getContrast()
			{
			return Math.pow(2,sliderContrast.getValue()/1000.0);
			}
		
		public double getBrightness()
			{
			return sliderBrightness.getValue();
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
			Imageset rec2=comboChannel.getImageset();
			String chname=comboChannel.getChannelName();
			if(rec2!=null && chname!=null)
				return rec2.getChannel(chname);
			else
				return null;
			}
		
		}	

		
	/** Extension: Tool */
	private final Vector<ImageWindowTool> imageWindowTools=new Vector<ImageWindowTool>();
	/** Currently selected tool */
	private ImageWindowTool currentTool;

	public void setTool(ImageWindowTool tool)
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
	
	public ImageWindowTool getTool()
		{
		return currentTool;
		}
	
	
	public void addImageWindowTool(ImageWindowTool tool)
		{
		imageWindowTools.add(tool);
		}
	
		
	/**
	 * Make new window at default location
	 */
	public ImageWindow()
		{
		this(new Rectangle(0,25,800,650));
		}
	/**
	 * Make a new window at given location
	 */
	
	public ImageWindow(Rectangle bounds)
		{
		for(ImageWindowRendererExtension e:imageWindowRendererExtensions)
			e.newImageWindow(this);
		for(ImageWindowExtension e:imageWindowExtensions)
			e.newImageWindow(this);
				
		imagePanel.setFocusable(true);
//		setFocusable(true);  //really useful?
//		addKeyListener(this);  //really useful?
		
		ChangeListener chListenerNoInvalidate=new ChangeListener()
			{public void stateChanged(ChangeEvent e) {updateImagePanelNoInvalidate();}};
		
		//Attach listeners
		imagePanel.addKeyListener(this);  //TODO
		imagePanel.addMouseListener(this);
		imagePanel.addMouseMotionListener(this);
		imagePanel.addMouseWheelListener(this);
		sliderZoom2.addSnapListener(new SnapChangeListener(){
			public void slideChange(SnapBackSlider source, int change){zoom(change/50.0);}
		});
		miShowOverlay.addChangeListener(chListenerNoInvalidate);
		bAddChannel.addActionListener(this);
		
		
		sliderRotate.addSnapListener(new SnapBackSlider.SnapChangeListener(){
		public void slideChange(SnapBackSlider source, int change)
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
		
		setLayout(new BorderLayout());
		add(imagePanel,BorderLayout.CENTER);
		add(channelPanel,BorderLayout.SOUTH);
		add(rightPanel,BorderLayout.EAST);

		
		//setShow3Color(bShow3colors.isSelected());
		buildChannelPanel();
		
		addMenubar(menuImageWindow);
		buildMenu();
		
		attachDragAndDrop(imagePanel);
		
		//Window overall things
		setTitleEvWindow("Image Window");
		for(ChannelWidget w:channelWidget)
			w.comboChannel.updateList();
		packEvWindow();
		frameControl.setChannel(getImageset(), getCurrentChannelName());
		frameControl.setFrame(EvDecimal.ZERO);
		setBoundsEvWindow(bounds);
		setVisibleEvWindow(true);
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
		buildChannelPanel();
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
			buildChannelPanel();
			updateImagePanel();
			}
		}

	/**
	 * Build list of channel widgets
	 */
	private void buildChannelPanel()
		{
		channelPanel.removeAll();
		channelPanel.setLayout(new GridLayout(1+channelWidget.size(),1,0,0));
		channelPanel.add(bottomPanelFirstRow);
		
		for(ChannelWidget w:channelWidget)
			channelPanel.add(w);
		channelPanel.setVisible(false);
		channelPanel.setVisible(true);  //TODO invalidate or something
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
		for(final ImageWindowTool t:imageWindowTools)
			menuItems.add(t.getMenuItem());
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
	
	/** Get current channel or null */
	public EvChannel getSelectedChannel()
		{
		String channelName=getCurrentChannelName();
		if(channelName!=null && getImageset().getChannel(channelName)!=null)
			return getImageset().getChannel(channelName);
		else
			return null;
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
	public Imageset getImageset()
		{
		Imageset im=getCurrentChannelWidget().comboChannel.getImageset();
		if(im==null)
			return new Imageset();
		else
			return im;
		}
	
	/**
	 * Get root object - were all objects to be displayed are located
	 */
	public EvContainer getRootObject()
		{
		Imageset im=getCurrentChannelWidget().comboChannel.getImageset();
		if(im==null)
			return new Imageset();
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
				ImageWindowView.ImagePanelImage pi=new ImageWindowView.ImagePanelImage();
				pi.brightness=cw.getBrightness();//cw.sliderBrightness.getValue();
				pi.contrast=cw.getContrast();//Math.pow(2,cw.sliderContrast.getValue()/1000.0);
				pi.color=cw.getColor();
				
				EvDecimal frame=frameControl.getFrame();
				EvDecimal z=frameControl.getZ();
				frame=ch.closestFrame(frame);
				
				EvStack stack=ch.getStack(frame);
				if(stack==null)
					pi.setImage(stack,null);
				else
					{
					int closestZ=stack.closestZint(z.doubleValue());
					System.out.println("----closest z: "+closestZ+"   depth:"+stack.getDepth());
					if(closestZ!=-1)
						{
						EvImage evim=stack.getInt(closestZ);
						if(evim!=null)
							pi.setImage(stack,evim);
						else
							{
							System.out.println("Image was null. ch:"+cw.getChannelName());
							}
						}
					else
						System.out.println("--For ch:"+cw.getChannelName());
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
		Imageset rec=getImageset();
		if(rec!=lastImagesetRecenter)
			{
			zoomToFit=true;
			lastImagesetRecenter=rec;
			frameControl.stepForward();
			}
		
		//Show new image
		imagePanel.repaint();

		if(zoomToFit)
			{
			imagePanel.zoomToFit();
			}
			
		//Update window title
		setTitleEvWindow("Image Window - "+channelWidget.get(0).comboChannel.getSelectedObjectPath());
		}
	
	
	/**
	 * Called whenever data has been updated
	 */
	public void dataChangedEvent()
		{
		imagePanel.dataChangedEvent();
		for(ChannelWidget w:channelWidget)
			w.comboChannel.updateList();
		frameControl.setChannel(getImageset(), getCurrentChannelName());//hm. does not cause cascade?
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
		frameControl.setChannel(getImageset(), getCurrentChannelName());
		
		if(e.getSource()==miReset)
			{
			for(ChannelWidget w:channelWidget)
				{
				w.sliderBrightness.setValue(0);
				w.sliderContrast.setValue(0);
				}
			setRotation(0);
			updateImagePanel();
			}
		else if(e.getSource()==miMiddleSlice)
			{
			EvChannel ch=getImageset().getChannel(getCurrentChannelName());
			if(ch!=null)
				{
				EvDecimal curFrame=ch.closestFrame(frameControl.getFrame());
				if(ch.getStack(curFrame).getDepth()>0)
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
				EvImage evim=im.getImage();
				if(evim==null)
					sb.append("Null slice");
				else
					{
					EvPixels pixels=evim.getPixels();
					
					EvStack stack=im.getStack();
					
					Vector3d disp=stack.getDisplacement();
					sb.append(
							"ResX: "+stack.getRes().x + " "+
							"ResY: "+stack.getRes().y + " "+
							"DX: "+disp.x + " "+
							"DY: "+disp.y + " "+
							"DZ: "+disp.z + " "+
							"Width(px): "+pixels.getWidth()+" "+
							"Height(px): "+pixels.getHeight()
							);
					sb.append("\n");
					}
				}
			String s=sb.toString();
			if(!s.equals(""))
				BasicWindow.showInformativeDialog(s);
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
			data.saveData();
			EvLog.printLog("Saving "+data.getMetadataName());
			}
		else if(e.getKeyCode()==KeyEvent.VK_W && holdModifier1(e))
			{
			data.unregisterOpenedData();
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
	
	public void loadedFile(EvData data)
		{
		List<Imageset> ims=data.getObjects(Imageset.class);
		if(!ims.isEmpty())
			getCurrentChannelWidget().comboChannel.setSelectedObject(ims.get(0), getCurrentChannelWidget().comboChannel.lastSelectChannel);
		}

	public void freeResources(){}
	
	
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
		
	/** Transform screen coordinate to world coordinate */
	public Vector2d transformPointS2W(Vector2d u)
		{
		return imagePanel.transformPointS2W(u);
		}

	/** Transform screen vector to world vector */
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

	
	//are these useful?
	/*
	public void transformOverlay(Graphics2D g)
		{
		Vector2d trans=imagePanel.transformI2S(new Vector2d(0,0));
		double zoomBinningX=imagePanel.zoom*getStrangeResX();
		double zoomBinningY=imagePanel.zoom*getStrangeResY();
		g.translate(trans.x,trans.y);
		g.scale(zoomBinningX,zoomBinningY);
		g.rotate(imagePanel.rotation);
		}
	public void untransformOverlay(Graphics2D g)
		{
		Vector2d trans=imagePanel.transformI2S(new Vector2d(0,0));
		double zoomBinningX=imagePanel.zoom*getStrangeResX();
		double zoomBinningY=imagePanel.zoom*getStrangeResY();
		g.rotate(-imagePanel.rotation);
		g.scale(1.0/zoomBinningX, 1.0/zoomBinningY);
		g.translate(-trans.x,-trans.y);
		}
	*/
	

	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new ImageWindowBasic());
		
		EV.personalConfigLoaders.put("imagewindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try
					{
					ImageWindow win=new ImageWindow(BasicWindow.getXMLbounds(e));
					win.frameControl.setGroup(e.getAttribute("group").getIntValue());
					win.channelWidget.get(0).comboChannel.lastSelectChannel=e.getAttributeValue("lastSelectChannel");
					}
				catch (Exception e1){e1.printStackTrace();}
				}
			public void savePersonalConfig(Element e){}
			});
		
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				w.addImageWindowTool(new ImageWindowToolChannelDisp(w));
				w.addImageWindowTool(new ImageWindowToolScreenshot(w));
				w.addImageWindowTool(new ImageWindowToolPixelInfo(w));
				w.addImageWindowTool(new ImageWindowToolEditImage(w));
				}
			});
		}
	
	public void addImageWindowRenderer(ImageWindowRenderer r)
		{
		imagePanel.imageWindowRenderers.add(r);
		}


	@SuppressWarnings("unchecked")
	public <E> E getRendererClass(Class<E> cl)
		{
		for(ImageWindowRenderer r:imagePanel.imageWindowRenderers)
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
	
	}


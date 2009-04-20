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
import endrov.data.EvData;
import endrov.ev.*;
import endrov.filter.*;
import endrov.imageset.*;
import endrov.keyBinding.*;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;

/**
 * Image window - Displays imageset with overlays. Data can be edited with tools, filters can be applied.
 *
 * @author Johan Henriksson
 */
public class ImageWindow extends BasicWindow 
			implements ActionListener, MouseListener, MouseMotionListener, KeyListener, ChangeListener, MouseWheelListener
	{	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;
	/** Registered extensions */
	private static final Vector<ImageWindowExtension> imageWindowExtensions=new Vector<ImageWindowExtension>();

	private static final int KEY_STEP_BACK    =KeyBinding.register(new KeyBinding("Image Window","Step back",'a'));
	private static final int KEY_STEP_FORWARD =KeyBinding.register(new KeyBinding("Image Window","Step forward",'d'));
	private static final int KEY_STEP_UP      =KeyBinding.register(new KeyBinding("Image Window","Step up",'w'));
	private static final int KEY_STEP_DOWN    =KeyBinding.register(new KeyBinding("Image Window","Step down",'s'));
	private static final int KEY_HIDE_MARKINGS=KeyBinding.register(new KeyBinding("Image Window","Hide markings",' '));

	private static ImageIcon iconLabelBrightness=new ImageIcon(FrameControlImage.class.getResource("labelBrightness.png"));
	private static ImageIcon iconLabelContrast=new ImageIcon(FrameControlImage.class.getResource("labelContrast.png"));
	private static ImageIcon iconLabel3color=new ImageIcon(FrameControlImage.class.getResource("label3channel.png"));
	
	
	public static int snapDistance=10;
	
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
				w.imageWindowTools.add(new ToolChannelDisp(w));
				w.imageWindowTools.add(new ImageWindowToolScreenshot(w));
				}
			});
		}
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
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
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	/**
	 * The image panel extended with more graphics
	 */
	private ImagePanel imagePanel=new ImagePanel()
		{
		static final long serialVersionUID=0;
		public void paintComponent(Graphics g)
			{
			super.paintComponent(g);
			if(!overlayHidden() && miShowOverlay.isSelected())
				{
				for(ImageWindowRenderer r:imageWindowRenderers)
					r.draw(g);
				if(tool!=null)
					tool.paintComponent(g);
				}
			}
		};
		
	/**
	 * Acquire a screenshot
	 */	
	public BufferedImage getScreenshot()
		{
		BufferedImage bi = new BufferedImage(imagePanel.getWidth(),imagePanel.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		imagePanel.paintComponent(g);
		return bi;
		}
		
	/**
	 * One row of channel settings in the GUI
	 */
	private class ChannelWidget extends JPanel implements ActionListener, ChangeListener
		{
		static final long serialVersionUID=0;
		
		public final JRadioButton rSelect=new JRadioButton();
		public final EvComboChannel comboChannel=new EvComboChannel(null,false);//
		public final JSlider sliderContrast=new JSlider(-4000,4000,0);
		public final JSlider sliderBrightness=new JSlider(-100,100,0);
		public final JButton bFilterSequence=FilterSeq.createFilterSeqButton();

		public final FilterSeq filterSeq=new FilterSeq();
		private SimpleObserver.Listener filterSeqObserver=new SimpleObserver.Listener()
			{public void observerEvent(Object o){dataChangedEvent();}};
		
		
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


			JPanel left=new JPanel(new BorderLayout());
			left.add(rSelect,BorderLayout.WEST);
			left.add(comboChannel,BorderLayout.CENTER);
			left.add(bFilterSequence,BorderLayout.EAST);
			
			add(left);
			add(contrastPanel);
			add(brightnessPanel);

			sliderContrast.addChangeListener(this);
			sliderBrightness.addChangeListener(this);
			comboChannel.addActionListener(this);
			bFilterSequence.addActionListener(this);
			
			filterSeq.observer.addWeakListener(filterSeqObserver);
			}
		
		public void actionPerformed(ActionEvent e)
			{
			frameControl.setChannel(getImageset(), getCurrentChannelName());
			
			if(e.getSource()==comboChannel)
				{
				frameControl.setAll(frameControl.getFrame(), frameControl.getZ());
				updateImagePanel();
				}
			else if(e.getSource()==bFilterSequence)
				{
				new WindowFilterSeq(filterSeq);
				}
			else
				updateImagePanel();
			}
		
		public void stateChanged(ChangeEvent e)
			{
			updateImagePanel();
			}	
		}	

		
	/** Extension: Overlay renderers */
	public final Vector<ImageWindowRenderer> imageWindowRenderers=new Vector<ImageWindowRenderer>();
	/** Extension: Tool */
	public final Vector<ImageWindowTool> imageWindowTools=new Vector<ImageWindowTool>();
	/** Currently selected tool */
	private ImageWindowTool tool;

	public void setTool(ImageWindowTool tool)
		{
		if(this.tool!=null)
			this.tool.unselected();
		this.tool=tool;  
		buildMenu();
		}
	public ImageWindowTool getTool()
		{
		return tool;
		}
	
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

	private boolean show3color=true;

	//GUI components
	private final JSlider sliderZoom=new JSlider(JSlider.VERTICAL, -10000,10000,-1000); //2^n	
	private final JSlider sliderRotate=new JSlider(JSlider.VERTICAL, -10000,10000,0); //2^n	
	private final JToggleButton bShow3colors=new JToggleButton(iconLabel3color);
	
	private final ButtonGroup rChannelGroup=new ButtonGroup();
	private final Vector<ChannelWidget> channelWidget=new Vector<ChannelWidget>();
	public final FrameControlImage frameControl=new FrameControlImage(this);
	
	private final JPanel channelPanel=new JPanel();

	private final JMenu menuImageWindow=new JMenu("ImageWindow");
	public final JMenu menuImage=new JMenu("Image");
	private final JCheckBoxMenuItem miToolNone=new JCheckBoxMenuItem("No tool");
	private final JMenuItem miZoomToFit=new JMenuItem("Zoom to fit");
	private final JMenuItem miReset=new JMenuItem("Reset view");
	private final JMenuItem miMiddleSlice=new JMenuItem("Go to middle slice");
	private final JCheckBoxMenuItem miShowOverlay=new JCheckBoxMenuItem("Show overlay",true);

	private final JPanel bottomPanelFirstRow=new JPanel(new BorderLayout());

	
		
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
		for(ImageWindowExtension e:imageWindowExtensions)
			e.newImageWindow(this);
				
		imagePanel.setFocusable(true);
		setFocusable(true);
		addKeyListener(this);
		
		ChangeListener chListenerNoInvalidate=new ChangeListener()
			{public void stateChanged(ChangeEvent e) {updateImagePanelNoInvalidate();}};
		
		//Attach listeners
		imagePanel.addKeyListener(this);
		imagePanel.addMouseListener(this);
		imagePanel.addMouseMotionListener(this);
		imagePanel.addMouseWheelListener(this);
		sliderZoom.addChangeListener(chListenerNoInvalidate);
		sliderRotate.addChangeListener(chListenerNoInvalidate);
		miShowOverlay.addChangeListener(chListenerNoInvalidate);
		bShow3colors.addActionListener(this);
		
		//Piece GUI together
		JPanel bottomRight=new JPanel(new GridLayout(1,2));
		bottomRight.add(bShow3colors);
		bottomPanelFirstRow.add(frameControl,BorderLayout.CENTER);
		bottomPanelFirstRow.add(bottomRight,BorderLayout.EAST);

		//Build list of channel widgets
		for(int i=0;i<3;i++)
			{
			ChannelWidget chWidget=new ChannelWidget();
			rChannelGroup.add(chWidget.rSelect);
			channelWidget.add(chWidget);
			}
		
		JPanel zoomPanel=new JPanel(new BorderLayout());
		zoomPanel.add(new JLabel(BasicIcon.iconLabelZoom), BorderLayout.NORTH);
		zoomPanel.add(sliderZoom,BorderLayout.CENTER);
		
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

		
		setShow3Color(bShow3colors.isSelected());
		
		addMenubar(menuImageWindow);
		addMenubar(menuImage);
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

	
	public void setShow3Color(boolean b)
		{
		channelWidget.get(0).rSelect.setSelected(true);
		show3color=b;
		buildChannelPanel();
		}
	
	/**
	 * Build list of channel widgets
	 */
	private void buildChannelPanel()
		{
		int numChannel;
		if(show3color)
			numChannel=3;
		else
			numChannel=1;
		channelPanel.removeAll();
		channelPanel.setLayout(new GridLayout(1+numChannel,1,0,0));
		channelPanel.add(bottomPanelFirstRow);
		for(int i=0;i<numChannel;i++)
			channelPanel.add(channelWidget.get(i));
		channelPanel.setVisible(false);
		channelPanel.setVisible(true);
		}
	
	
	/**
	 * Rebuild ImageWindow menu
	 */
	private void buildMenu()
		{
//		System.out.println("build menu");

		EvSwingUtil.tearDownMenu(menuImageWindow);
		miReset.addActionListener(this);
		miMiddleSlice.addActionListener(this);
		miZoomToFit.addActionListener(this);
		miToolNone.addActionListener(this);
		
		
		//Window specific menu items
		menuImageWindow.add(miReset);
		menuImageWindow.add(miMiddleSlice);
		menuImageWindow.add(miZoomToFit);
		menuImageWindow.add(miShowOverlay);
		
		menuImageWindow.addSeparator();
		menuImageWindow.add(miToolNone);
		miToolNone.setSelected(tool==null);
				
		for(final ImageWindowTool t:imageWindowTools)
			if(t==null)
				menuImageWindow.addSeparator();
			else
				menuImageWindow.add(t.getMenuItem());
	
		//List custom tool as well
		if(!imageWindowTools.contains(tool) && !miToolNone.isSelected())
			menuImageWindow.add(tool.getMenuItem());
		
		}
	

	public ChannelWidget getCurrentChannelWidget()
		{
		for(ChannelWidget w:channelWidget)
			if(w.rSelect.isSelected())
				return w;
		return null;
		}
	/** Get name of currently viewed channel */
	public String getCurrentChannelName()
		{
		return getCurrentChannelWidget().comboChannel.getChannel();
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
	/** Get current metadata */
	public EvData getData()
		{
		return getCurrentChannelWidget().comboChannel.getData();
		}
	/** Get current imageset or an empty one */
	public Imageset getImageset()
		{
		Imageset im=getCurrentChannelWidget().comboChannel.getImageset();
		if(im==null)
			return new Imageset();
		else
			return im;
		}
	/** Get the zoom factor not including binning */
	public double getZoom()
		{
		return Math.pow(2,sliderZoom.getValue()/1000.0);
		}
	/** Set the zoom factor not including the binning */
	public void setZoom(double zoom)
		{
		sliderZoom.setValue((int)(1000*Math.log(zoom)/Math.log(2)));
		}
	/** Get rotation of image, in radians */
	public double getRotation()
		{
		return sliderRotate.getValue()*Math.PI/10000.0;
		}
	/** Set rotation of image, in radians */
	public void setRotation(double angle)
		{
		sliderRotate.setValue((int)(angle*10000.0/Math.PI));
		}
	/** Check if overlay should be hidden */
	public boolean overlayHidden()
		{
		return temporarilyHideMarkings;
		}




	

	/** Scale screen vector to world vector 
	 * */
	public double scaleS2w(double s) {return s/(getImageset().resY*getZoom());}
	/** Scale world to screen vector */
	public double scaleW2s(double w) {return w*getImageset().resY*getZoom();}

	
	//New functions, should replace the ones above at some point

	/** Transform world coordinate to screen coordinate */
	public Vector2d transformW2S(Vector2d u)
		{
		return imagePanel.transformI2S(new Vector2d(u.x*getImageset().resX,u.y*getImageset().resY));
		}
	/** Transform screen coordinate to world coordinate */
	public Vector2d transformS2W(Vector2d u)
		{
		Vector2d v=imagePanel.transformS2I(u);
		return new Vector2d(v.x/getImageset().resX, v.y/getImageset().resY);
		}
	
	/** Convert world to screen Z coordinate */
	//public double w2sz(double z) {return z*getImageset().resZ;}
	/** Convert world to screen Z coordinate */
	//public double s2wz(double sz) {return sz/((double)getImageset().resZ);} 

	
	//are these useful?
	public void transformOverlay(Graphics2D g)
		{
		Vector2d trans=imagePanel.transformI2S(new Vector2d(0,0));
		double zoomBinningX=imagePanel.zoom*getImageset().resX;
		double zoomBinningY=imagePanel.zoom*getImageset().resY;
		g.translate(trans.x,trans.y);
		g.scale(zoomBinningX,zoomBinningY);
		g.rotate(imagePanel.rotation);
		}
	public void untransformOverlay(Graphics2D g)
		{
		Vector2d trans=imagePanel.transformI2S(new Vector2d(0,0));
		double zoomBinningX=imagePanel.zoom*getImageset().resX;
		double zoomBinningY=imagePanel.zoom*getImageset().resY;
		g.rotate(-imagePanel.rotation);
		g.scale(1.0/zoomBinningX, 1.0/zoomBinningY);
		g.translate(-trans.x,-trans.y);
		}
	
	/**
	 * Take current settings of sliders and apply it to image
	 */
	public void updateImagePanel()
		{
		//Set images
		imagePanel.images.clear();
		int numChannel;
		if(show3color)
			numChannel=3;
		else
			numChannel=1;
		for(int i=0;i<numChannel;i++)
			{
			Imageset rec2=channelWidget.get(i).comboChannel.getImageset();
			String chname=channelWidget.get(i).comboChannel.getChannel();
			if(rec2!=null && chname!=null)
				{
				EvChannel ch=rec2.getChannel(chname);
				ImagePanel.ImagePanelImage pi=new ImagePanel.ImagePanelImage();
				pi.brightness=channelWidget.get(i).sliderBrightness.getValue();
				pi.contrast=Math.pow(2,channelWidget.get(i).sliderContrast.getValue()/1000.0);
				
				EvDecimal frame=frameControl.getFrame();
				EvDecimal z=frameControl.getZ();
				frame=ch.closestFrame(frame);
				z=ch.closestZ(frame, z);
				
				pi.image=ch.getImageLoader(frame,z);
				FilterSeq fseq=channelWidget.get(i).filterSeq;
				if(!fseq.isIdentity())
					pi.image=fseq.applyReturnImage(pi.image);
				
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
		
//		try			{			throw new Exception("");			}
//		catch (Exception e)			{			e.printStackTrace();			}
		
		
		//Copy settings into image panel
		imagePanel.rotation=getRotation();
		imagePanel.zoom=getZoom();
		
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
			setZoom(imagePanel.zoom);
			}
			
		//Update window title
		setTitleEvWindow("Image Window - "+channelWidget.get(0).comboChannel.getSelectedObjectPath());
		}
	
	
	/**
	 * Called whenever data has been updated
	 */
	public void dataChangedEvent()
		{
		for(ImageWindowRenderer r:imageWindowRenderers)
			r.dataChangedEvent();
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
	
	
/**
 * 
 * 
 * call on filterseq callback
	public void dataChangedEvent()
 */	
	
	
	
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
				if(ch.imageLoader.get(curFrame).size()>0)
					{
					EvDecimal firstSlice=ch.imageLoader.get(curFrame).firstZ();
					EvDecimal lastSlice=ch.imageLoader.get(curFrame).lastZ();
					frameControl.setZ(ch.closestZ(curFrame, firstSlice.add(lastSlice).divide(2)));
					updateImagePanel();
					}
				}
			}
		else if(e.getSource()==miZoomToFit)
			{
			imagePanel.zoomToFit();
			setZoom(imagePanel.zoom);
			}
		else if(e.getSource()==miToolNone)
			setTool(null);
		else if(e.getSource()==bShow3colors)
			{
			setShow3Color(bShow3colors.isSelected());
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
		
	//	if(!ScriptBinding.runScriptKey(e))
			{
		EvData data=getData();
		//Imageset rec=getImageset();
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
			Log.printLog("Saving "+data.getMetadataName());
			}
		else if(e.getKeyCode()==KeyEvent.VK_W && holdModifier1(e))
			{
			data.unregisterOpenedData();
			Log.printLog("Closing "+data.getMetadataName());
			}
		else if(e.getKeyCode()==KeyEvent.VK_0)
			{
			updateImagePanel();
			}
		else if(e.getKeyCode()==KeyEvent.VK_9)
			{
			updateImagePanelNoInvalidate();
			}
		else
			{
			if(tool!=null)
				tool.keyPressed(e);
			}
			}
		}
	/**
	 * Callback: Key has been released
	 */
	public void keyReleased(KeyEvent e)
		{
		if(!ScriptBinding.runScriptKey(e))
			{
		if(KeyBinding.get(KEY_HIDE_MARKINGS).typed(e))
			{
			temporarilyHideMarkings=false;
			repaint();
			}
		else if(tool!=null)
			tool.keyReleased(e);
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
		if(tool!=null)
			tool.mouseClicked(e);
//		if(SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e))
		imagePanel.requestFocus();
		}
	/**
	 * Callback: Mouse button pressed
	 */
	public void mousePressed(MouseEvent e)
		{
		imagePanel.requestFocus();
		if(tool!=null)
			tool.mousePressed(e);
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		}
	/**
	 * Callback: Mouse button released
	 */
	public void mouseReleased(MouseEvent e)
		{
		if(tool!=null)
			tool.mouseReleased(e);
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
		if(tool!=null)
			tool.mouseExited(e);
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
		if(tool!=null)
			tool.mouseMoved(e,dx,dy);
		
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

		if(tool!=null)
			tool.mouseDragged(e,dx,dy);
		}
	/**
	 * Callback: Mouse scrolls
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
		{
		//Self-note: linux machine at home (mahogny) uses UNIT_SCROLL
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
			sliderZoom.setValue(sliderZoom.getValue()+e.getUnitsToScroll()*10);
		else if(e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL)
			sliderZoom.setValue(sliderZoom.getValue()+e.getUnitsToScroll()*2);
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
		
	
	}


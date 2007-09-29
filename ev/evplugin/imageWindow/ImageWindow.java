package evplugin.imageWindow;



import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import evplugin.ev.*;
import evplugin.basicWindow.*;
import evplugin.consoleWindow.*;
import evplugin.imageset.*;
import evplugin.keyBinding.KeyBinding;
import evplugin.keyBinding.ScriptBinding;
import evplugin.metadata.Metadata;
import org.jdom.*;

/**
 * Image window - Displays nuclei, markers etc on top of image and allows new
 * markers to be made.
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
	public static final Vector<ImageWindowExtension> imageWindowExtensions=new Vector<ImageWindowExtension>();


	public static final int KEY_STEP_BACK    =KeyBinding.register(new KeyBinding("Image Window","Step back",'a'));
	public static final int KEY_STEP_FORWARD =KeyBinding.register(new KeyBinding("Image Window","Step forward",'d'));
	public static final int KEY_STEP_UP      =KeyBinding.register(new KeyBinding("Image Window","Step up",'w'));
	public static final int KEY_STEP_DOWN    =KeyBinding.register(new KeyBinding("Image Window","Step down",'s'));
	public static final int KEY_HIDE_MARKINGS=KeyBinding.register(new KeyBinding("Image Window","Hide markings",' '));


	public static void addImageWindowExtension(ImageWindowExtension e)
		{
		imageWindowExtensions.add(e);
		}
	
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
					win.comboChannel.lastSelectChannel=e.getAttributeValue("lastSelectChannel");
					}
				catch (Exception e1)
					{
					e1.printStackTrace();
					}
				
				}
			public void savePersonalConfig(Element e){}
			});
		
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				w.imageWindowTools.add(new ToolChannelDisp(w));
				}
			});
		}
	

	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
		{
		Rectangle r=getBounds();
		Element e=new Element("imagewindow");
		e.setAttribute("x", ""+r.x);
		e.setAttribute("y", ""+r.y);
		e.setAttribute("w", ""+r.width);
		e.setAttribute("h", ""+r.height);
		e.setAttribute("group", ""+frameControl.getGroup());
		e.setAttribute("lastSelectChannel", comboChannel.lastSelectChannel);
		root.addContent(e);
		}

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	public final Vector<ImageWindowRenderer> imageWindowRenderers=new Vector<ImageWindowRenderer>();
	public final Vector<ImageWindowTool> imageWindowTools=new Vector<ImageWindowTool>();
	
	private boolean temporarilyHideMarkings=false;
	
	public boolean hideAllMarkings()
		{
		return temporarilyHideMarkings;
		}
	
	//The image panel extended with more graphics
	private ImagePanel imagePanel=new ImagePanel()
		{
		static final long serialVersionUID=0;
		public void paintComponent(Graphics g)
			{
			super.paintComponent(g);
			if(!hideAllMarkings())
				{
				for(ImageWindowRenderer r:imageWindowRenderers)
					r.draw(g);
				if(tool!=null)
					tool.paintComponent(g);
				}
			}
		};
	
	// Other GUI components
	private final JSlider sliderContrast=new JSlider(-500,500,0);
	private final JSlider sliderBrightness=new JSlider(-100,100,0);
	private final JSlider sliderZoom=new JSlider(-10000,10000,-1000); //2^n	
	public final FrameControlImage frameControl=new FrameControlImage(this);
	public final ChannelCombo comboChannel=new ChannelCombo(null,false);
	
	private JMenu menuImageWindow=new JMenu("ImageWindow");
	public JMenu menuImage=new JMenu("Image");
	private final JCheckBoxMenuItem miToolNone=new JCheckBoxMenuItem("No tool");
	private final JMenuItem miZoomToFit=new JMenuItem("Zoom to fit");
	private final JMenuItem miReset=new JMenuItem("Reset view");
	private final JMenuItem miMiddleSlice=new JMenuItem("Go to middle slice");

	/** Last coordinate of the mouse pointer. Used to detect dragging distance. */
	private int mouseLastDragX=0, mouseLastDragY=0;
	/** Last coordinate of the mouse pointer. Used to detect moving distance. For event technical reasons,
	 * this requires a separate set of variables than dragging (or so it seems) */
	private int mouseLastX=0, mouseLastY=0;
	/** Current mouse coordinate. Used for repainting. */
	public int mouseCurX=0, mouseCurY=0;
	
	/** Currently selected tool */
	private ImageWindowTool tool;
	
	/** Flag if the mouse cursor currently is in the window */
	public boolean mouseInWindow=false;


	
	
	
	
	/** Convert world to screen X coordinate */
	public double w2sx(double x) {return imagePanel.i2sx(x*getImageset().meta.resX);}
	/** Convert world to screen Y coordinate */
	public double w2sy(double y) {return imagePanel.i2sy(y*getImageset().meta.resY);}
	/** Convert world to screen Z coordinate */
	public double w2sz(double z) {return z*getImageset().meta.resZ;}
	/** Convert screen to world X coordinate */
	public double s2wx(double sx) {return imagePanel.s2ix(sx)/(double)getImageset().meta.resX;}
	/** Convert world to screen Y coordinate */
	public double s2wy(double sy) {return imagePanel.s2iy(sy)/(double)getImageset().meta.resY;}
	/** Convert world to screen Z coordinate */
	public double s2wz(double sz) {return sz/(double)getImageset().meta.resZ;} //need a zoom?
	/** Scale screen vector to world vector */
	public double scaleS2w(double s) {return s/(getImageset().meta.resY*getZoom());}
	/** Scale world to screen vector */
	public double scaleW2s(double w) {return w*getImageset().meta.resY*getZoom();}

		
	/**
	 * Make new window at default location
	 */
	public ImageWindow()
		{
		this(new Rectangle(0,25,800,650));
//		this(0,25,800,650);
		}
	/**
	 * Make a new window at some location
	 */
	public ImageWindow(/*int x, int y, int w, int h*/ Rectangle bounds)
		{
		for(ImageWindowExtension e:imageWindowExtensions)
			e.newImageWindow(this);
				
		imagePanel.setFocusable(true);
		setFocusable(true);
		addKeyListener(this);
		
		//Attach listeners
		imagePanel.addKeyListener(this);
		imagePanel.addMouseListener(this);
		imagePanel.addMouseMotionListener(this);
		imagePanel.addMouseWheelListener(this);
		sliderContrast.addChangeListener(this);
		sliderBrightness.addChangeListener(this);
		sliderZoom.addChangeListener(this);
		comboChannel.addActionListener(this);

		//Piece GUI together
		JPanel bottom=new JPanel(new GridLayout(2,1,0,0));
		JPanel bottom2=new JPanel(new GridLayout(1,4));
		bottom.add(frameControl);
		bottom.add(bottom2);

		JPanel contrastPanel=new JPanel(new BorderLayout());
		contrastPanel.add(new JLabel("C: "), BorderLayout.WEST);
		contrastPanel.add(sliderContrast,BorderLayout.CENTER);
		bottom2.add(contrastPanel);

		JPanel brightnessPanel=new JPanel(new BorderLayout());
		brightnessPanel.add(new JLabel("B: "), BorderLayout.WEST);
		brightnessPanel.add(sliderBrightness,BorderLayout.CENTER);
		bottom2.add(brightnessPanel);

		JPanel zoomPanel=new JPanel(new BorderLayout());
		zoomPanel.add(new JLabel("Zoom"), BorderLayout.WEST);
		zoomPanel.add(sliderZoom,BorderLayout.CENTER);
		bottom2.add(zoomPanel);

		bottom2.add(comboChannel);

		setLayout(new BorderLayout());
		add(imagePanel,BorderLayout.CENTER);
		add(bottom,BorderLayout.SOUTH);

		addMenubar(menuImageWindow);
		addMenubar(menuImage);
		buildMenu();
		
		//Window overall things
		setTitle(EV.programName+" Image Window");
		comboChannel.updateChannelList();
		pack();
		updateImagePanel();
		frameControl.setChannel(getImageset(), getCurrentChannelName());
		frameControl.setFrame(0);
		setVisible(true);
		setBounds(bounds);
//		setBounds(x,y,w,h);
		}

	/**
	 * Rebuild ImageWindow menu
	 */
	private void buildMenu()
		{
		BasicWindow.tearDownMenu(menuImageWindow);
		//BasicWindow.tearDownMenu(menuImage);
		miReset.addActionListener(this);
		miMiddleSlice.addActionListener(this);
		miZoomToFit.addActionListener(this);
		miToolNone.addActionListener(this);
		
		
		//Window specific menu items
		menuImageWindow.add(miReset);
		menuImageWindow.add(miMiddleSlice);
		menuImageWindow.add(miZoomToFit);
		
		menuImageWindow.addSeparator();
		menuImageWindow.add(miToolNone);
		miToolNone.setSelected(tool==null);
		
		for(final ImageWindowTool t:imageWindowTools)
			if(t==null)
				menuImageWindow.addSeparator();
			else
				{
				if(t.isToggleable())
					{
					final JCheckBoxMenuItem mit=new JCheckBoxMenuItem(t.toolCaption());
					mit.setEnabled(t.enabled());
					mit.setSelected(tool==t);
					mit.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							tool=t;
							buildMenu();
							}
						});
					menuImageWindow.add(mit);
					}
				else
					{
					final JMenuItem mit=new JMenuItem(t.toolCaption());
					mit.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							t.mouseClicked(null);
							}
						});
					menuImageWindow.add(mit);
					}
				}
		
		
		}
	

	
	public String getCurrentChannelName()
		{
		return comboChannel.getChannel();
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
			sliderBrightness.setValue(0);
			sliderContrast.setValue(0);
			updateImagePanel();
			}
		else if(e.getSource()==miMiddleSlice)
			{
			Imageset.ChannelImages ch=getImageset().getChannel(getCurrentChannelName());
			if(ch!=null)
				{
				int curFrame=ch.closestFrame((int)frameControl.getFrame());
				if(ch.imageLoader.get(curFrame).size()>0)
					{
					int firstSlice=ch.imageLoader.get(curFrame).firstKey();
					int lastSlice=ch.imageLoader.get(curFrame).lastKey();
					frameControl.setZ(ch.closestZ(curFrame, (firstSlice+lastSlice)/2));
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
			{
			tool=null;
			buildMenu();
			}
		else if(e.getSource()==comboChannel)
			{
			frameControl.setAll(frameControl.getFrame(), frameControl.getZ());
			updateImagePanel();
			}
		else
			updateImagePanel();
		}

	
	
	
	/*
	 * (non-Javadoc)
	 * NOTE! new key events are generated when mouse is moved!!
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
		{
		if(!ScriptBinding.runScriptKey(e))
			{
			Imageset rec=comboChannel.getImageset();
			if(KeyBinding.get(KEY_HIDE_MARKINGS).typed(e))
				{
				temporarilyHideMarkings=true;
				repaint();
				}
			else if(KeyBinding.get(KEY_GETCONSOLE).typed(e))
				ConsoleWindow.focusConsole(this, imagePanel);
			else if(e.getKeyCode()==KeyEvent.VK_S && holdModifier1(e))
				{
				if(!(rec instanceof EmptyImageset))
					{
					rec.saveMeta();
					Log.printLog("Saving "+rec.getMetadataName());
					}
				}
			else if(e.getKeyCode()==KeyEvent.VK_W && holdModifier1(e))
				{
				if(!(rec instanceof EmptyImageset))
					{
					Metadata.metadata.remove(rec);
					Log.printLog("Closing "+rec.getMetadataName());
					}
				BasicWindow.updateWindows();
				}
			else
				{
				if(tool!=null)
					tool.keyPressed(e);
				}
			}
		}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e)
		{
		if(KeyBinding.get(KEY_HIDE_MARKINGS).typed(e))
			{
			temporarilyHideMarkings=false;
			repaint();
			}
		else if(tool!=null)
			tool.keyReleased(e);
		}

	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
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

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e)
		{
		if(tool!=null)
			tool.mouseClicked(e);
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
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
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

	/*
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
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
			updateImagePanel();
			}

		if(tool!=null)
			tool.mouseDragged(e,dx,dy);
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
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
		{
		//Self-note: linux machine at home (mahogny) uses UNIT_SCROLL
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
			sliderZoom.setValue(sliderZoom.getValue()+e.getUnitsToScroll()*10);
		else if(e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL)
			sliderZoom.setValue(sliderZoom.getValue()+e.getUnitsToScroll()*2);
		}

	
	/** Get current imageset */
	public Imageset getImageset()
		{
		return comboChannel.getImageset();
		}
		
	/** Get the zoom factor not including binning */
	public double getZoom()
		{
		return Math.pow(2,sliderZoom.getValue()/1000.0);
		}
	
	/** Set the zoom factor not including the binning */
	private void setZoom(double zoom)
		{
		sliderZoom.setValue((int)(1000*Math.log(zoom)/Math.log(2)));
		}


	/**
	 * Get current channel
	 * @return Channel if one is selected or null
	 */
	public Imageset.ChannelImages getSelectedChannel()
		{
		String channelName=getCurrentChannelName();
		if(channelName!=null && getImageset().getChannel(channelName)!=null)
			return getImageset().getChannel(channelName);
		else
			return null;
		}



	/** Keep track of last imageset for re-centering purposes */
	private Imageset lastImagesetRecenter=null;
	
	/**
	 * Take current settings of sliders and apply it to image
	 */
	public void updateImagePanel()
		{
		//Copy settings into image panel
		imagePanel.brightness=sliderBrightness.getValue();
		imagePanel.contrast=Math.pow(2,sliderContrast.getValue()/1000.0);
		imagePanel.zoom=getZoom();
		imagePanel.binning=1;
		int z=frameControl.getZ();
		double frame=frameControl.getFrame();		
		imagePanel.imageLoader=null;

		//Check if recenter needed
		boolean zoomToFit=false;
		Imageset rec=comboChannel.getImageset();
		if(rec!=lastImagesetRecenter)
			{
			lastImagesetRecenter=rec;
			zoomToFit=true;
			}

		Imageset.ChannelImages ch=getSelectedChannel();
		if(ch!=null)
			{
			imagePanel.imageLoader=ch.getImageLoader((int)frame, z);
			imagePanel.binning=ch.getMeta().chBinning;
			imagePanel.dispX=ch.getMeta().dispX;
			imagePanel.dispY=ch.getMeta().dispY;
			}

		//Show new image
		imagePanel.update();

		if(zoomToFit)
			{
			imagePanel.zoomToFit();
			setZoom(imagePanel.zoom);
			}
			
		//Update window title
		String title=EV.programName+" Image Window - "+getImageset().getMetadataName();
		setTitle(title);
		}


	/**
	 * Called whenever data has been updated
	 */
	public void dataChangedEvent()
		{
		for(ImageWindowRenderer r:imageWindowRenderers)
			r.dataChangedEvent();
		comboChannel.updateChannelList();
		frameControl.setChannel(getImageset(), getCurrentChannelName());//hm. does not cause cascade?
		buildMenu();
		updateImagePanel();
		}
	
	
	
	}

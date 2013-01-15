/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.plateWindow;

import java.util.List;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.vecmath.*;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.basicWindow.icon.BasicIcon;
import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.PersonalConfig;
import endrov.flowMeasure.ParticleMeasure;
import endrov.imageWindow.FrameControlImage;
import endrov.imageset.*;
import endrov.plateWindow.scene.Scene2DImage;
import endrov.plateWindow.scene.Scene2DText;
import endrov.plateWindow.scene.Scene2DView;
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
			implements ActionListener, MouseListener, MouseMotionListener, KeyListener, ChangeListener, MouseWheelListener
			
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
	
	private final FrameControlImage frameControl=new FrameControlImage(this, false, false);

	private final JMenu menuPlateWindow=new JMenu("PlateWindow");

	private Scene2DView imagePanel=new Scene2DView();
	
	private ChannelWidget cw=new ChannelWidget();

	/** Last coordinate of the mouse pointer. Used to detect dragging distance. */
	private int mouseLastDragX=0, mouseLastDragY=0;
	/** Last coordinate of the mouse pointer. Used to detect moving distance. For event technical reasons,
	 * this requires a separate set of variables than dragging (or so it seems) */
	private int mouseLastX=0, mouseLastY=0;
	/** Current mouse coordinate. Used for repainting. */
	public int mouseCurX=0, mouseCurY=0;
	/** Flag if the mouse cursor currently is in the window */
	public boolean mouseInWindow=false;

	
	
	/**
	 * Make a new window at given location
	 */
	
	public PlateWindow(Rectangle bounds)
		{
		
		//Attach listeners
		imagePanel.addKeyListener(this);  //TODO
		imagePanel.addMouseListener(this);
		imagePanel.addMouseMotionListener(this);
		imagePanel.addMouseWheelListener(this);
		/*
		sliderZoom2.addSnapListener(new SnapChangeListener(){
			public void slideChange(SnapBackSlider source, int change){zoom(change/50.0);}
		});
*/		
		
				
//		attachDragAndDrop(imagePanel);
		
		//Window overall things
		
		addMenubar(menuPlateWindow);

		
		JComboBox comboTable=new JComboBox();
//		JComboBox comboFeature=new JComboBox();
		EvComboObjectOne<ParticleMeasure> comboFeature=new EvComboObjectOne<ParticleMeasure>(new ParticleMeasure(), true, false);
		
		
		JComboBox comboAggregation=new JComboBox(new String[]{
				"Mean", "Std.Dev", "Histogram"
		});

		JButton bExport=new JButton("Export as CSV");
		
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutLCR(
				null, 
				imagePanel, 
				EvSwingUtil.layoutACB(
						EvSwingUtil.layoutCompactVertical(
								new JLabel("WTF:"),
								comboTable,
								new JLabel("Feature object:"),
								comboFeature,
								new JLabel("View:"),
								comboAggregation),
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
		updateWindowTitle();
		setVisibleEvWindow(true);
		layoutImagePanel();
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
			layoutImagePanel();
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
				layoutImagePanel();
			}
		
		public void fitRange()
			{
			//TODO
			}
		
		public void stateChanged(ChangeEvent e)
			{
			layoutImagePanel();
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


	
	
	/**
	 * Take current settings of sliders and apply it to image
	 */
	public void layoutImagePanel()
		{
		//Set images
		imagePanel.clear();
		
		
		int imageMargin=10;
		int imageSize=100;
		
		int numRow=10;
		int numCol=10;
		
		for(int i=1;i<=numCol;i++)
			{
			char c='A';
			Scene2DText st=new Scene2DText((i-1)*(imageSize+imageMargin)+50, -20, ""+(char)(c+i-1));
			imagePanel.addElem(st);
			}
		
		for(int i=1;i<=numRow;i++)
			{
			Scene2DText st=new Scene2DText(-20, (i-1)*(imageSize+imageMargin)+50, ""+i);
			imagePanel.addElem(st);
			}
		

		for(int row=1;row<=numRow;row++)
			for(int col=1;col<=numCol;col++)
				{

				EvStack stack=new EvStack();  //Displacement from here
				stack.allocate(300, 300, 1, EvPixelsType.INT);
				
				int maxdim=stack.getWidth();
				if(stack.getHeight()>maxdim)
					maxdim=stack.getHeight();
				
				stack.resX=stack.resY=stack.resZ=imageSize/(double)maxdim;

				stack.cs.setMidBases(new Vector3d((col-1)*(imageSize+imageMargin),(row-1)*(imageSize+imageMargin),0), new Vector3d[]{
						new Vector3d(1,0,0),
						new Vector3d(0,1,0),
						new Vector3d(0,0,1)
						});
				
				Scene2DImage imp=new Scene2DImage();
				imp.setImage(stack, 0);
				imp.borderColor=EvColor.green;
				imagePanel.addElem(imp);

				}

		
/*		
		
		

		
	
		EvChannel ch=cw.getChannel();
		
		
		//Imageset rec2=cw.comboChannel.getImageset();
		//String chname=cw.comboChannel.getChannelName();
		if(ch!=null)
		//if(rec2!=null && chname!=null)
			{
			//EvChannel ch=rec2.getChannel(chname);
			
			
			ImageLayoutView.ImagePanelImage pi=new ImageLayoutView.ImagePanelImage();
			pi.brightness=cw.getBrightness();//cw.sliderBrightness.getValue();
			pi.contrast=cw.getContrast();//Math.pow(2,cw.sliderContrast.getValue()/1000.0);
			pi.color=EvColor.white;
			
			EvDecimal frame=frameControl.getFrame();
			EvDecimal z=frameControl.getZ();
			frame=ch.closestFrame(frame);
			
			EvStack stack=ch.getStack(new ProgressHandle(), frame);
			
			//System.out.println("---- got stack "+stack);
			
			if(stack==null)
				pi.setImage(null,0);
			else
				{
				int closestZ=stack.closestZint(z.doubleValue());
				//System.out.println("----closest z: "+closestZ+"   depth:"+stack.getDepth());
				if(closestZ!=-1)
					{
					EvImage evim=stack.getInt(closestZ);
					//System.out.println("--- got stack 2: "+evim+"   "+evim.getPixels(null));
					
					if(evim!=null)
						pi.setImage(stack,closestZ);
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

		*/
		imagePanel.invalidateImages();
		repaintImagePanel();
		}


	
	/**
	 * Update, but assume images are still ok
	 */
	public void repaintImagePanel()
		{				
		//Check if recenter needed
		boolean zoomToFit=false;
		
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
		setTitleEvWindow("Plate Window");
		}
	
	
	/**
	 * Called whenever data has been updated
	 */
	public void dataChangedEvent()
		{
		buildMenu();
		layoutImagePanel();
		}
	
	/**
	 * Upon state changes, update the window
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		layoutImagePanel();
		}	
	
	
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		
		}

	
	
	
	/**
	 * Callback: Key pressed down
	 */
	public void keyPressed(KeyEvent e)
		{
			
		}
	/**
	 * Callback: Key has been released
	 */
	public void keyReleased(KeyEvent e)
		{
		}
	/**
	 * Callback: Keyboard key typed (key down and up again)
	 */
	public void keyTyped(KeyEvent e)
		{
		/*
		if(KeyBinding.get(KEY_STEP_BACK).typed(e))
			frameControl.stepBack();
		else if(KeyBinding.get(KEY_STEP_FORWARD).typed(e))
			frameControl.stepForward();
		else if(KeyBinding.get(KEY_STEP_DOWN).typed(e))
			frameControl.stepDown();
		else if(KeyBinding.get(KEY_STEP_UP).typed(e))
			frameControl.stepUp();
			*/
		}
	/**
	 * Callback: Mouse button clicked
	 */
	public void mouseClicked(MouseEvent e)
		{
		imagePanel.requestFocus();
		}
	/**
	 * Callback: Mouse button pressed
	 */
	public void mousePressed(MouseEvent e)
		{
		imagePanel.requestFocus();
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		}
	/**
	 * Callback: Mouse button released
	 */
	public void mouseReleased(MouseEvent e)
		{
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
//		if(currentTool!=null)
//			currentTool.mouseMoved(e,dx,dy);
		
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
			repaintImagePanel();
			}

//		if(currentTool!=null)
	//		currentTool.mouseDragged(e,dx,dy);
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
		imagePanel.zoom(Math.pow(10,val/10));
		repaint();
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
		
		
		}
	
	}


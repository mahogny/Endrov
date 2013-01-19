/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.liveWindow;




import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.Semaphore;

import javax.swing.*;
import javax.vecmath.Vector2d;

import org.jdom.*;

import endrov.core.EndrovUtil;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.gui.GeneralTool;
import endrov.gui.component.EvComboObject;
import endrov.gui.component.EvHidableSidePaneBelow;
import endrov.gui.window.BasicWindow;
import endrov.gui.window.BasicWindowExtension;
import endrov.gui.window.BasicWindowHook;
import endrov.hardware.*;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.recording.CameraImage;
import endrov.recording.RecordingResource;
import endrov.recording.ResolutionManager;
import endrov.recording.device.HWCamera;
import endrov.roi.GeneralToolROI;
import endrov.roi.ImageRendererROI;
import endrov.roi.ROI;
import endrov.roi.window.GeneralToolDragCreateROI;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;
import endrov.util.JImageToggleButton;
import endrov.util.Vector2i;
import endrov.windowViewer2D.Viewer2DWindow;
import endrov.windowViewer2D.Viewer2DInterface;
import endrov.windowViewer2D.Viewer2DRenderer;
import endrov.windowViewer2D.Viewer2DRendererExtension;

/**
 * Camera live-feed window
 * 
 * @author Johan Henriksson 
 */
public class LiveWindow extends BasicWindow implements ActionListener, Viewer2DInterface
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;
	
	
	public static final ImageIcon iconAutoFocus=new ImageIcon(LiveWindow.class.getResource("jhAutoFocus.png"));
	public static final ImageIcon iconCameraToROI=new ImageIcon(LiveWindow.class.getResource("jhCameraToROI.png"));
	public static final ImageIcon iconEllipseROI=new ImageIcon(LiveWindow.class.getResource("jhEllipse.png"));
	public static final ImageIcon iconFreehandROI=new ImageIcon(LiveWindow.class.getResource("jhFreehand.png"));
	public static final ImageIcon iconGoToROI=new ImageIcon(LiveWindow.class.getResource("jhGoToROI.png"));
	public static final ImageIcon iconLineROI=new ImageIcon(LiveWindow.class.getResource("jhLine.png"));
	public static final ImageIcon iconPointROI=new ImageIcon(LiveWindow.class.getResource("jhPoint.png"));
	public static final ImageIcon iconPolygonROI=new ImageIcon(LiveWindow.class.getResource("jhPolygon.png"));
	public static final ImageIcon iconRectROI=new ImageIcon(LiveWindow.class.getResource("jhRect.png"));
	public static final ImageIcon iconSelectROI=new ImageIcon(LiveWindow.class.getResource("jhSelect.png"));

	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/


	private EvPixels[] lastCameraImage=null;
	private Dimension lastImageSize=null; 
	private ResolutionManager.Resolution lastResolution=null;

	private Vector2d lastImageStagePos=new Vector2d();
	
	private JComboBox cameraCombo;

	//Update timer, busy loop for now. replace later by camera event listener	
	private javax.swing.Timer timer=new javax.swing.Timer(10,this);

	private JCheckBox tAutoRange=new JCheckBox("Auto", true);
	private JButton bSetFullRange=new JButton("Full");
	private LiveHistogramViewRanged histoView=new LiveHistogramViewRanged();
	private JCheckBox tUpdateView=new JCheckBox("Update", true);
	private JCheckBox tLive=new JCheckBox("Live", false);
	private JButton bSnap=new JButton("Snap");
	private EvHidableSidePaneBelow sidepanel;
	private JPanel pHisto=new JPanel(new BorderLayout());

	

	private JButton bAutoFocus=new JImageButton(iconAutoFocus, "Autofocus");
	private JButton bCameraToROI=new JImageButton(iconCameraToROI, "Adapt camera limits to ROI");	
	private JButton bGoToROI=new JImageButton(iconGoToROI, "Move stage to focus on ROI");
	
	private JTextField tStoreName=new JTextField("im");
	private EvComboObject comboStorageLocation=new EvComboObject(new LinkedList<EvObject>(), true, false)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return cont instanceof EvContainer;
			}
		};
	private JButton bStore=new JButton("Store");

		
		
		
	/**
	 * Surface for the image
	 */
	private LiveWindowImageView drawArea=new LiveWindowImageView()
		{
		private static final long serialVersionUID = 1L;
		public int getUpper(){return histoView.upper;}
		public int getLower(){return histoView.lower;}
		//TODO rgb
		public EvPixels[] getImage()
			{
			if(LiveWindow.this.lastCameraImage!=null)
				return LiveWindow.this.lastCameraImage;
			else
				return null;
			}
		
		public Vector2i getOffset()
			{
			double dx=getStageX()-lastImageStagePos.x;
			double dy=getStageY()-lastImageStagePos.y;
			return new Vector2i((int)(dx/getCameraResolution().x), (int)(dy/getCameraResolution().y));
			}
		
		protected void paintComponent(java.awt.Graphics g)
			{
			super.paintComponent(g);
			
			//For ROI debug
			/*
			for(EvObject ob:RecordingResource.getData().metaObject.values())
				{
				ROI roi=(ROI)ob;
				
				Iterator<HWImageScanner> itcam=EvHardware.getDeviceMapCast(HWImageScanner.class).values().iterator();
				HWImageScanner cam=null;
				if(itcam.hasNext())
					cam=itcam.next();
				
				double stageX=RecordingResource.getCurrentStageX();
				double stageY=RecordingResource.getCurrentStageY();

				int w=cam.getWidth();
				int h=cam.getHeight();
				int arr[]=RecordingResource.makeScanningROI(cam, roi, stageX, stageY);
				
				g.setColor(Color.red);
				for(int y=0;y<h;y++)
					for(int x=0;x<w;x++)
						if(arr[y*w+x]!=0)
							g.drawLine(x, y, x, y);
				}
				
				*/
			
			}
		@Override
		public EvDevicePath getCameraPath()
			{
			return getCurrentCameraPath();
			}
		};
	
	

	private Vector<JToggleButton> toolButtons=new Vector<JToggleButton>();
	private JToggleButton bSelectROI=new JImageToggleButton(iconSelectROI, "Select ROI");

	
	
	

	private Semaphore sem=new Semaphore(0);
	private boolean stopSnapThread=false;

	
	private Thread snapThread=new Thread()
		{
		public void run()
			{
			for(;;)
				{
				
				try
					{
					sem.acquire();
					}
				catch (InterruptedException e)
					{
					e.printStackTrace();
					}
				
				if(stopSnapThread)
					return;
				
				
				synchronized (RecordingResource.acquisitionLock)
					{
					//this does not work later. have to synchronize all calls for an image
					//so all targets gets it.
					HWCamera cam=getCurrentCamera();
					//EvDevicePath camname=(EvDevicePath)cameraCombo.getSelectedItem();
					if(cam!=null)
						{
						//long curTime=System.currentTimeMillis();
						//HWCamera cam=(HWCamera)EvHardware.getDevice(camname);
						Vector2d curStagePos=new Vector2d(getStageX(),getStageY());
						CameraImage cim=cam.snap();
						lastImageStagePos=curStagePos;
						lastCameraImage=cim.getPixels();
						lastResolution=getCameraResolution();

						//Update range if needed
						if(lastCameraImage!=null && tAutoRange.isSelected())
							histoView.calcAutoRange(lastCameraImage);
						
						//System.out.println("Acquiring live took - setimagehisto ms: "+(System.currentTimeMillis()-curTime));

						int numBits=getNumCameraBits();
						histoView.setImage(lastCameraImage, numBits);
						//System.out.println("Acquiring live took ms: "+(System.currentTimeMillis()-curTime));
						
						//Update size of this window if camera area size changes
						if(lastCameraImage!=null)
							{
							Dimension newDim=new Dimension(lastCameraImage[0].getWidth(), lastCameraImage[0].getHeight());
							if(lastImageSize==null || !lastImageSize.equals(newDim))
								{
								Rectangle rect=drawArea.getBounds();
								Dimension oldDim=new Dimension(rect.width,rect.height);
								
								Rectangle bounds=getBoundsEvWindow();
								setBoundsEvWindow(new Rectangle(
										bounds.x,bounds.y,
										(int)(bounds.getWidth()+(newDim.getWidth()-oldDim.getWidth())),
										(int)(bounds.getHeight()+(newDim.getHeight()-oldDim.getHeight()))
										));
								}
							lastImageSize=newDim;
							}

						}
					}
				/*
				//Do not snap if some acquisition has blocked the camera
				if(RecordingResource.isLiveCameraBlocked())
					{
					try{Thread.sleep(10);}
					catch (InterruptedException e){}
					continue;
					}*/
				

				//Update image
				try
					{
					SwingUtilities.invokeAndWait(new Runnable() 
						{
						public void run()
							{
							drawArea.repaint();
							}
						});
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				
				}
			
			
			}
		};
	
	
	
	public void setTool(GeneralTool tool)
		{
		//TODO?
		drawArea.currentTool=tool;
		}

	public void unsetTool()
		{
		drawArea.currentTool=null;
		
		//Make sure all tool buttons are unselected
		for(JToggleButton bb:toolButtons)
			{
			//bb.removeActionListener(this);
			bb.setSelected(false);
			//bb.addActionListener(this);
			}
		}
	
	public LiveWindow()
		{
		this(new Rectangle(400,300));
		}
	
	
	public LiveWindow(Rectangle bounds)
		{
		toolButtons.addAll(Arrays.asList(/*bEllipseROI,bFreehandROI,bLineROI,bPointROI,bPolygonROI,bRectROI,*/bSelectROI));

		bSelectROI.setSelected(true);
		setTool(new GeneralToolROI(LiveWindow.this));
		
		bSelectROI.addActionListener(new ActionListener()
			{public void actionPerformed(ActionEvent e)
				{
				if(((JToggleButton)e.getSource()).isSelected())
					{
					//ImageRendererROI renderer=getRendererClass(ImageRendererROI.class);
					setTool(new GeneralToolROI(LiveWindow.this));
					
					//setTool(new GeneralToolDragCreateROI(CamWindow.this,rt.makeInstance(),renderer));
					}
				}});
		
		
		for(final ROI.ROIType rt:ROI.getTypes())
			{
			if(rt.canPlace() && !rt.isCompound())
				{
				JToggleButton miNewROIthis;
				
				//toolButtons.addAll(Arrays.asList(bEllipseROI,bFreehandROI,bLineROI,bPointROI,bPolygonROI,bRectROI,bSelectROI));

				
				if(rt.getIcon()==null)
					miNewROIthis=new JToggleButton(rt.name());
				else
					miNewROIthis=new JImageToggleButton(rt.getIcon(),rt.name());
				miNewROIthis.addActionListener(new ActionListener()
					{public void actionPerformed(ActionEvent e)
						{
						if(((JToggleButton)e.getSource()).isSelected())
							{
							ImageRendererROI renderer=getRendererClass(ImageRendererROI.class);
							setTool(new GeneralToolDragCreateROI(LiveWindow.this,rt.makeInstance(),renderer));
							}
						}});
				
				//TODO would be best if it was sorted
				toolButtons.add(miNewROIthis);
				//BasicWindow.addMenuItemSorted(miNew, miNewROIthis);
				}
			}

		
		/*
		private JToggleButton bEllipseROI=new JImageToggleButton(iconEllipseROI, "Create ellipse ROI");
		private JToggleButton bFreehandROI=new JImageToggleButton(iconFreehandROI, "Create freehand ROI");
		private JToggleButton bLineROI=new JImageToggleButton(iconLineROI, "Create line ROI");
		private JToggleButton bPointROI=new JImageToggleButton(iconEllipseROI, "Create point ROI");
		private JToggleButton bPolygonROI=new JImageToggleButton(iconPolygonROI, "Create polygon ROI");
		private JToggleButton bRectROI=new JImageToggleButton(iconRectROI, "Create rectangle ROI");
		*/



		
		///////////////////////
		
		
		
		drawArea.setToolButtons(toolButtons.toArray(new JToggleButton[0]));
		
		
		
		for(Viewer2DRendererExtension e:Viewer2DWindow.imageWindowRendererExtensions)
			e.newImageWindow(this);
		
		cameraCombo=new JComboBox(new Vector<EvDevicePath>(EvHardware.getDeviceMap(HWCamera.class).keySet()));
		
		tLive.setToolTipText("Continuously take pictures");
		bSnap.setToolTipText("Manually take a picture and update. Does not save image.");
		//tHistoView.setToolTipText("Show histogram controls");
		tAutoRange.setToolTipText("Automatically adjust visible range");
		bSetFullRange.setToolTipText("Set visible range of all of camera range");

		bCameraToROI.addActionListener(this);
		tLive.addActionListener(this);
		bSnap.addActionListener(this);
		//tHistoView.addActionListener(this);
		tAutoRange.addActionListener(this);
		bSetFullRange.addActionListener(this);
		histoView.addActionListener(this);
		bAutoFocus.addActionListener(this);
		bGoToROI.addActionListener(this);
		bStore.addActionListener(this);
		
		//pHisto.setBorder(BorderFactory.createTitledBorder("Range adjustment"));
		pHisto.add(
				EvSwingUtil.layoutCompactVertical(tAutoRange, bSetFullRange),
				BorderLayout.WEST);
		pHisto.add(histoView, BorderLayout.CENTER);
		
		List<JComponent> blistleft=new LinkedList<JComponent>();
		blistleft.addAll(toolButtons);
		blistleft.add(bCameraToROI);
		blistleft.add(bGoToROI);
		blistleft.add(bAutoFocus);
		JComponent pLeft=EvSwingUtil.layoutACB(
				EvSwingUtil.layoutEvenVertical(
						blistleft.toArray(new JComponent[0])
						/*
						bSelectROI,	bEllipseROI, bFreehandROI, bLineROI, bPointROI, bPolygonROI, bRectROI,
						bCameraToROI,
						bGoToROI,
						bAutoFocus*/
						),
						null,
						null
				);

		JComponent pAcquire=EvSwingUtil.layoutLCR(null, 
				EvSwingUtil.layoutEvenHorizontal(
						EvSwingUtil.withLabel("Store in: ", comboStorageLocation),
						EvSwingUtil.withLabel("Name: ", tStoreName)),
				bStore);
		
		
		JPanel pCenter=new JPanel(new BorderLayout());
		pCenter.add(EvSwingUtil.layoutCompactHorizontal(cameraCombo, bSnap, tLive, tUpdateView)
				,BorderLayout.SOUTH);
		pCenter.add(drawArea,BorderLayout.CENTER);
		
		sidepanel=new EvHidableSidePaneBelow(pCenter, 
				EvSwingUtil.layoutACB(
						null, 
						EvSwingUtil.withTitledBorder("View settings", pHisto), 
						EvSwingUtil.withTitledBorder("Store image", pAcquire)),
				true);
		sidepanel.addActionListener(this);
		
		setLayout(new BorderLayout());
		add(sidepanel,BorderLayout.CENTER);
		add(pLeft,BorderLayout.WEST);
		
		for(JToggleButton b:toolButtons)
			b.addActionListener(this);
		
		//Window overall things
		setTitleEvWindow("Live view");
		packEvWindow();
		setBoundsEvWindow(bounds);
		setVisibleEvWindow(true);
		timer.start();
		//setResizable(false);
		
		snapThread.start();
		
		}
	
	
		
	
	/**
	 * Find out how many bits the camera is
	 */
	public Integer getNumCameraBits()
		{
		return 8;
		}
	
	/**
	 * Handle GUI interaction
	 */
	public void actionPerformed(ActionEvent e) 
		{
		if(e.getSource()==timer && tLive.isSelected())
			snapCamera();
		else if(e.getSource()==bSnap)
			snapCamera();
		else if(e.getSource()==tAutoRange)
			{
			if(lastCameraImage!=null)
				histoView.calcAutoRange(lastCameraImage);
			histoView.repaint();
			drawArea.repaint();
			}
		else if(e.getSource()==bSetFullRange)
			{
			histoView.lower=0;
			histoView.upper=(int)Math.pow(2, getNumCameraBits())-1;
			drawArea.repaint();
			histoView.repaint();
			}
		else if(e.getSource()==bAutoFocus)
			autofocusAction();
		else if(e.getSource()==bGoToROI)
			moveStageFocusROI();
		else if(e.getSource()==bCameraToROI)
			showErrorDialog("Not implemented yet");
		else if(e.getSource()==histoView)
			{
			drawArea.repaint();
			}
		else if(e.getSource()==sidepanel)
			{
			Rectangle bounds=getBoundsEvWindow();
			int dh=pHisto.getBounds().height;
			if(!sidepanel.isPanelVisible())
				dh=-dh;
			setBoundsEvWindow(new Rectangle(
					bounds.x,bounds.y,
					(int)(bounds.getWidth()),
					(int)(bounds.getHeight()+dh)
					));
			}
		else if(e.getSource()==bStore)
			storeImage();
		else
			for(JToggleButton b:toolButtons)
				if(e.getSource()==b)
					{
					
					//Make sure all other tool buttons are unselected
					for(JToggleButton bb:toolButtons)
						{
						if(bb!=b)
							{
							//bb.removeActionListener(this);
							bb.setSelected(false);
							//bb.addActionListener(this);
							}
						}
					
					}
		
		}
	
	
	private Imageset findUnusedImageName(EvContainer con, String name)
		{
		String attempt;
		int i=0;
		do
			{
			attempt=name+EndrovUtil.pad(i, 8);
			i++;
			} while(con.metaObject.containsKey(attempt));
		Imageset im=new Imageset();
		con.metaObject.put(attempt, im);
		return im;
		}
	
	/**
	 * Store the current image
	 */
	private void storeImage()
		{
		EvPixels p[]=drawArea.getImage();
		EvContainer con=comboStorageLocation.getSelectedObject();
		String name=tStoreName.getText();
		if(con==null)
			showErrorDialog("No place to store selected");
		else if(p==null)
			showErrorDialog("No image to save");
		else
			{
			Imageset im=findUnusedImageName(con, name);
			EvChannel ch=im.getCreateChannel("live"); //pick a better name if possible
			
			EvStack stack=new EvStack();
			stack.putInt(0, new EvImage(p[0]));
			stack.resX=lastResolution.x;
			stack.resY=lastResolution.y;
			stack.resZ=1;
			ch.putStack(EvDecimal.ZERO, stack);
			}
		}
		
	/**
	 * Get the currently selected camera
	 */
	private HWCamera getCurrentCamera()
		{
		EvDevicePath camname=(EvDevicePath)cameraCombo.getSelectedItem();
		if(camname!=null)
			return (HWCamera)EvHardware.getDevice(camname);
		else
			return null;
		}

	private EvDevicePath getCurrentCameraPath()
		{
		return (EvDevicePath)cameraCombo.getSelectedItem();
		}
	
	/**
	 * Take one picture from the camera	
	 */
	private void snapCamera()
		{
		//This will allow the snapping thread to do one run
		sem.drainPermits();
		sem.release();
		}
		

	
	
	public void dataChangedEvent()
		{
		comboStorageLocation.updateList();
		}

	public void eventUserLoadedFile(EvData data){}

	public void windowSavePersonalSettings(Element e)
		{
		} 
	public void freeResources()
		{
		timer.stop();
		
		//Stop snapping thread 
		stopSnapThread=true;
		sem.release();
		}
	

	
	
	
	
	
	public void addImageWindowRenderer(Viewer2DRenderer renderer)
		{
		drawArea.imageWindowRenderers.add(renderer);
		}


	public EvDecimal getFrame()
		{
		return EvDecimal.ZERO;
		}

	public EvDecimal getZ()
		{
		return EvDecimal.ZERO; //Unclear what is the best. 3D rois?
		}

	@SuppressWarnings("unchecked")
	public <E> E getRendererClass(Class<E> cl)
		{
		for(Viewer2DRenderer r:drawArea.imageWindowRenderers)
			if(cl.isInstance(r))
				return (E)r;
		throw new RuntimeException("No such renderer exists - " + cl);
		}

	
	
	
	public EvContainer getRootObject()
		{
		return RecordingResource.getData();
		}

	public double getRotation()
		{
		//Never any rotation
		return 0;
		}

	
	/**
	 * Resolution [um/px]. Never null
	 */
	public ResolutionManager.Resolution getCameraResolution()
		{
		return ResolutionManager.getCurrentResolutionNotNull(getCurrentCameraPath());
		}
	
	public double getStageX() // um
		{
		return RecordingResource.getCurrentStageX();
		}

	public double getStageY() // um
		{
		return RecordingResource.getCurrentStageY();
		}

	public double s2wz(double sz)
		{
		return sz;
		}

	public double scaleS2w(double s)
		{
		return s*getCameraResolution().x;
		}

	public double scaleW2s(double w)
		{
		return w/getCameraResolution().x;
		}

	public Vector2d transformPointS2W(Vector2d v)
		{
		return new Vector2d(v.x*getCameraResolution().x-getStageX(), v.y*getCameraResolution().y-getStageY()); 
		}

	public Vector2d transformPointW2S(Vector2d v)
		{
		return new Vector2d((v.x+getStageX())/getCameraResolution().x, (v.y+getStageY())/getCameraResolution().y);
		}

	public double w2sz(double z)
		{
		return z; //TODO
		}

	public String getCurrentChannelName()
		{
		return "";
		}

	public void updateImagePanel()
		{
		drawArea.repaint();
		}
	
	
	/**
	 * Autofocus, with whatever device there is
	 */
	public static void autofocusAction()
		{
		try
			{
			RecordingResource.autofocus();
			}
		catch (Exception e1)
			{
			showErrorDialog("Error: "+e1.getMessage());
			}
		}
	
	/**
	 * Move the stage such that one ROI is in focus
	 */
	public void moveStageFocusROI()
		{
		Set<ROI> rois=new HashSet<ROI>(ROI.getSelected());
		
		if(rois.size()!=1)
			showErrorDialog("Select 1 ROI first");
		else
			{
			ROI roi=rois.iterator().next();
			
			double x=roi.getPlacementHandle1().getX();
			double y=roi.getPlacementHandle2().getY();
			//Best would be to be able to get a bounding box
			
			Map<String, Double> pos=new HashMap<String, Double>();
			pos.put("X",x);
			pos.put("Y",y);
			RecordingResource.setStagePos(pos);
			
			//TODO move to center. must take into account camera etc in that case 
			
			//TODO
			//Probably useful in a wider context - put in resource
			}
			
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
				JMenuItem mi=new JMenuItem("Live view",new ImageIcon(getClass().getResource("tangoCamera.png")));
				mi.addActionListener(this);
				BasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
				}

			public void actionPerformed(ActionEvent e) 
				{
				new LiveWindow();
				}

			public void buildMenu(BasicWindow w){}
			}
			});
		
		}
	
	
	
	}

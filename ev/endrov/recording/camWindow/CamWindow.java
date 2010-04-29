/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.camWindow;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.vecmath.Vector2d;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.hardware.*;
import endrov.imageWindow.GeneralTool;
import endrov.imageWindow.ImageWindow;
import endrov.imageWindow.ImageWindowInterface;
import endrov.imageWindow.ImageWindowRenderer;
import endrov.imageWindow.ImageWindowRendererExtension;
import endrov.imageset.EvPixels;
import endrov.recording.CameraImage;
import endrov.recording.HWCamera;
import endrov.recording.RecordingResource;
import endrov.roi.GeneralToolROI;
import endrov.roi.ImageRendererROI;
import endrov.roi.ROI;
import endrov.roi.window.GeneralToolDragCreateROI;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;
import endrov.util.JImageToggleButton;

/**
 * Camera live-feed window
 * @author Johan Henriksson 
 */
public class CamWindow extends BasicWindow implements ActionListener, ImageWindowInterface
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;
	
	
	public static final ImageIcon iconAutoFocus=new ImageIcon(CamWindow.class.getResource("jhAutoFocus.png"));
	public static final ImageIcon iconCameraToROI=new ImageIcon(CamWindow.class.getResource("jhCameraToROI.png"));
	public static final ImageIcon iconEllipseROI=new ImageIcon(CamWindow.class.getResource("jhEllipse.png"));
	public static final ImageIcon iconFreehandROI=new ImageIcon(CamWindow.class.getResource("jhFreehand.png"));
	public static final ImageIcon iconGoToROI=new ImageIcon(CamWindow.class.getResource("jhGoToROI.png"));
	public static final ImageIcon iconLineROI=new ImageIcon(CamWindow.class.getResource("jhLine.png"));
	public static final ImageIcon iconPointROI=new ImageIcon(CamWindow.class.getResource("jhPoint.png"));
	public static final ImageIcon iconPolygonROI=new ImageIcon(CamWindow.class.getResource("jhPolygon.png"));
	public static final ImageIcon iconRectROI=new ImageIcon(CamWindow.class.getResource("jhRect.png"));
	public static final ImageIcon iconSelectROI=new ImageIcon(CamWindow.class.getResource("jhSelect.png"));

	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/


	private EvPixels[] lastCameraImage=null;
	private Dimension lastImageSize=null; 

	private JComboBox cameraCombo;

	//Update timer, busy loop for now. replace later by camera event listener	
	private javax.swing.Timer timer=new javax.swing.Timer(10,this);

	private JCheckBox tAutoRange=new JCheckBox("Auto", true);
	private JButton bSetFullRange=new JButton("Full");
	private CameraHistogramViewRanged histoView=new CameraHistogramViewRanged();
	private JCheckBox tUpdateView=new JCheckBox("Update", true);
	private JCheckBox tLive=new JCheckBox("Live", false);
	private JButton bSnap=new JButton("Snap");
	private EvHidableSidePaneBelow sidepanel;
	private JPanel pHisto=new JPanel(new BorderLayout());

	

	private JButton bAutoFocus=new JImageButton(iconAutoFocus, "Autofocus");
	private JButton bCameraToROI=new JImageButton(iconCameraToROI, "Adapt camera limits to ROI");	
	private JButton bGoToROI=new JImageButton(iconGoToROI, "Move stage to focus on ROI");
	
	
	/**
	 * Surface for the image
	 */
	private CamWindowImageView drawArea=new CamWindowImageView()
		{
		private static final long serialVersionUID = 1L;
		public int getUpper(){return histoView.upper;}
		public int getLower(){return histoView.lower;}
		//TODO rgb
		public EvPixels[] getImage()
			{
			if(CamWindow.this.lastCameraImage!=null)
				return CamWindow.this.lastCameraImage;
			else
				return null;
			}
		};
	
	

	private Vector<JToggleButton> toolButtons=new Vector<JToggleButton>();
	private JToggleButton bSelectROI=new JImageToggleButton(iconSelectROI, "Select ROI");

	
	
	
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
	
	public CamWindow()
		{
		this(new Rectangle(400,300));
		}
	
	
	public CamWindow(Rectangle bounds)
		{
		toolButtons.addAll(Arrays.asList(/*bEllipseROI,bFreehandROI,bLineROI,bPointROI,bPolygonROI,bRectROI,*/bSelectROI));

		
		bSelectROI.addActionListener(new ActionListener()
			{public void actionPerformed(ActionEvent e)
				{
				if(((JToggleButton)e.getSource()).isSelected())
					{
					//ImageRendererROI renderer=getRendererClass(ImageRendererROI.class);
					setTool(new GeneralToolROI(CamWindow.this));
					
					//setTool(new GeneralToolDragCreateROI(CamWindow.this,rt.makeInstance(),renderer));
					System.out.println("Hello!!!???");
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
							setTool(new GeneralToolDragCreateROI(CamWindow.this,rt.makeInstance(),renderer));
							System.out.println("Hello!!!");
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
		
		
		
		
		
		
		
		for(ImageWindowRendererExtension e:ImageWindow.imageWindowRendererExtensions)
			e.newImageWindow(this);
		
		cameraCombo=new JComboBox(new Vector<EvDevicePath>(EvHardware.getDeviceMap(HWCamera.class).keySet()));
		
		tLive.setToolTipText("Continuously take pictures");
		bSnap.setToolTipText("Manually take a picture and update. Does not save image.");
		//tHistoView.setToolTipText("Show histogram controls");
		tAutoRange.setToolTipText("Automatically adjust visible range");
		bSetFullRange.setToolTipText("Set visible range of all of camera range");

		tLive.addActionListener(this);
		bSnap.addActionListener(this);
		//tHistoView.addActionListener(this);
		tAutoRange.addActionListener(this);
		bSetFullRange.addActionListener(this);
		histoView.addActionListener(this);
		bAutoFocus.addActionListener(this);
		
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
		
		
		JPanel pCenter=new JPanel(new BorderLayout());
		pCenter.add(EvSwingUtil.layoutCompactHorizontal(cameraCombo, bSnap, tLive, tUpdateView)
				,BorderLayout.SOUTH);
		pCenter.add(drawArea,BorderLayout.CENTER);
		
		sidepanel=new EvHidableSidePaneBelow(pCenter, pHisto, true);
		sidepanel.addActionListener(this);
		
		setLayout(new BorderLayout());
		add(sidepanel,BorderLayout.CENTER);
		add(pLeft,BorderLayout.WEST);
		
		for(JToggleButton b:toolButtons)
			b.addActionListener(this);
		
		//Window overall things
		setTitleEvWindow("Camera Control");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		timer.start();
		//setResizable(false);
		
		
		
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
		
		
	/**
	 * Take one picture from the camera	
	 */
	private void snapCamera()
		{
		//this does not work later. have to synchronize all calls for an image
		//so all targets gets it.
		
		EvDevicePath camname=(EvDevicePath)cameraCombo.getSelectedItem();
		if(camname!=null)
			{
			HWCamera cam=(HWCamera)EvHardware.getDevice(camname);
			CameraImage cim=cam.snap();
			lastCameraImage=cim.getPixels();

			//Update range if needed
			if(lastCameraImage!=null && tAutoRange.isSelected())
				histoView.calcAutoRange(lastCameraImage);

			int numBits=getNumCameraBits();
			histoView.setImage(lastCameraImage, numBits);
			
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

			//Update image
			drawArea.repaint();
			}
		
		}
		
		
	
	
	
	public void dataChangedEvent()
		{
		}

	public void loadedFile(EvData data){}

	public void windowSavePersonalSettings(Element e)
		{
		} 
	public void freeResources()
		{
		timer.stop();
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
				JMenuItem mi=new JMenuItem("Live Camera",new ImageIcon(getClass().getResource("tangoCamera.png")));
				mi.addActionListener(this);
				BasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
				}

			public void actionPerformed(ActionEvent e) 
				{
				new CamWindow();
				}

			public void buildMenu(BasicWindow w){}
			}
			});
		
		}
	
	

	public static void main(String[] args)
		{
		
		new CamWindow();
		
		}

	
	
	
	
	public void addImageWindowRenderer(ImageWindowRenderer renderer)
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
		for(ImageWindowRenderer r:drawArea.imageWindowRenderers)
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

	
	public double getCameraResolution() // px/um
		{
		return 1;
		}
	
	public double getStageX() // um
		{
		return 0;
		}

	public double getStageY() // um
		{
		return 0;
		}

	public double s2wz(double sz)
		{
		return sz;
		}

	public double scaleS2w(double s)
		{
		return s/getCameraResolution();
		}

	public double scaleW2s(double w)
		{
		return w*getCameraResolution();
		}

	public Vector2d transformS2W(Vector2d v)
		{
		return new Vector2d(v.x/getCameraResolution()-getStageX(), v.y/getCameraResolution()-getStageY()); //TODO offset by stage
		}

	public Vector2d transformW2S(Vector2d v)
		{
		return new Vector2d(v.x*getCameraResolution(), v.y*getCameraResolution()); //TODO offset by stage
		}

	public double w2sz(double z)
		{
		return z; //TODO
		}

	public String getCurrentChannelName()
		{
		return "cam";
		}

	public void updateImagePanel()
		{
		drawArea.repaint();
		}
	}

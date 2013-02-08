/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.windowPlateOverview;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.vecmath.Vector2d;

import org.jdom.*;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.gui.EvSwingUtil;
import endrov.gui.GeneralTool;
import endrov.gui.component.EvHidableSidePaneBelow;
import endrov.gui.component.JImageButton;
import endrov.gui.component.JImageToggleButton;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.hardware.*;
import endrov.recording.CameraImage;
import endrov.recording.RecordingResource;
import endrov.recording.StoredStagePosition;
import endrov.recording.StoredStagePositionAxis;
import endrov.recording.RecordingResource.PositionListListener;
import endrov.recording.ResolutionManager;
import endrov.recording.ResolutionManager.Resolution;
import endrov.recording.device.HWCamera;
import endrov.recording.device.HWStage;
import endrov.recording.liveWindow.LiveHistogramViewRanged;
import endrov.roi.GeneralToolROI;
import endrov.roi.ImageRendererROI;
import endrov.roi.ROI;
import endrov.roi.window.GeneralToolDragCreateROI;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.math.EvDecimal;
import endrov.windowViewer2D.Viewer2DWindow;
import endrov.windowViewer2D.Viewer2DInterface;
import endrov.windowViewer2D.Viewer2DRenderer;
import endrov.windowViewer2D.Viewer2DRendererExtension;

/**
 * Presents an overview image
 * 
 * @author Kim Nordlöf, Erik Vernersson
 */
public class PlateOverviewWindow extends EvBasicWindow implements ActionListener,
		Viewer2DInterface, PositionListListener
	{
	/******************************************************************************************************
	 * Static *
	 *****************************************************************************************************/
	static final long serialVersionUID = 0;

	public static final ImageIcon iconAutoFocus = new ImageIcon(
			PlateOverviewWindow.class.getResource("jhAutoFocus.png"));
	public static final ImageIcon iconCameraToROI = new ImageIcon(
			PlateOverviewWindow.class.getResource("jhCameraToROI.png"));
	public static final ImageIcon iconGoToROI = new ImageIcon(
			PlateOverviewWindow.class.getResource("jhGoToROI.png"));
	public static final ImageIcon iconRectROI = new ImageIcon(
			PlateOverviewWindow.class.getResource("jhRect.png"));
	public static final ImageIcon iconSelectROI = new ImageIcon(
			PlateOverviewWindow.class.getResource("jhSelect.png"));
	public static final ImageIcon iconCreatePos = new ImageIcon(
			PlateOverviewWindow.class.getResource("jhCreatePOS.png"));

	/******************************************************************************************************
	 * Instance *
	 *****************************************************************************************************/

	private EvPixels[] lastCameraImage = null;
	private Vector2d overviewImageOffset = new Vector2d(0, 0);

	private int overviewImgWidth = 0;
	private int overviewImgHeight = 0;

	private JComboBox<EvDevicePath> cameraCombo;

	private JCheckBox tAutoRange = new JCheckBox("Auto", true);
	private JButton bSetFullRange = new JButton("Full");
	private LiveHistogramViewRanged histoView = new LiveHistogramViewRanged();
	private JCheckBox tUpdateView = new JCheckBox("Update", true);
	private JButton bSnap = new JButton("Snap");
	private EvHidableSidePaneBelow sidepanel;
	private JPanel pHisto = new JPanel(new BorderLayout());
	private JButton bResetView = new JButton("Reset");

	private JButton bAutoFocus = new JImageButton(iconAutoFocus, "Autofocus");
	// private JButton bCameraToROI=new JImageButton(iconCameraToROI,
	// "Adapt camera limits to ROI");
	private JButton bGoToROI = new JImageButton(iconGoToROI,
			"Move stage to focus on ROI");
	private JButton bCreatePos = new JImageButton(iconCreatePos,
			"Create positions from ROI");

	/**
	 * Surface for the image
	 */
	private PlateOverviewWindowImageView drawArea = new PlateOverviewWindowImageView()
		{
			private static final long serialVersionUID = 1L;

			public int getUpper()
				{
				return histoView.upper;
				}

			public int getLower()
				{
				return histoView.lower;
				}

			public long getCameraWidth()
				{
				return getCurrentCamera().getCamWidth();
				}

			public long getCameraHeight()
				{
				return getCurrentCamera().getCamHeight();
				}

			@Override
			public Vector2d getOffset()
				{
				return overviewImageOffset;
				}

			protected void paintComponent(java.awt.Graphics g)
				{
				super.paintComponent(g);
				}

			@Override
			public EvDevicePath getCameraPath()
				{
				return getCurrentCameraPath();
				}

			@Override
			public Resolution getResolution()
				{
				return new ResolutionManager.Resolution(getCameraResolution().x,
						getCameraResolution().y);
				}

		};

	private Vector<JToggleButton> toolButtons = new Vector<JToggleButton>();
	private JToggleButton bSelectROI = new JImageToggleButton(iconSelectROI,
			"Select ROI");

	public void setTool(GeneralTool tool)
		{
		// TODO?
		drawArea.currentTool = tool;
		}

	public void unsetTool()
		{
		drawArea.currentTool = null;

		// Make sure all tool buttons are unselected
		for (JToggleButton bb : toolButtons)
			{

			bb.setSelected(false);

			}
		}


	public PlateOverviewWindow()
		{
		toolButtons.addAll(Arrays.asList(/*
																			 * bEllipseROI,bFreehandROI,bLineROI,bPointROI
																			 * ,bPolygonROI,bRectROI,
																			 */bSelectROI));

		bSelectROI.setSelected(true);
		setTool(new GeneralToolROI(PlateOverviewWindow.this));

		bSelectROI.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					if (((JToggleButton) e.getSource()).isSelected())
						{
						// ImageRendererROI
						// renderer=getRendererClass(ImageRendererROI.class);
						setTool(new GeneralToolROI(PlateOverviewWindow.this));

						// setTool(new
						// GeneralToolDragCreateROI(CamWindow.this,rt.makeInstance(),renderer));
						}
					}
			});

		for (final ROI.ROIType rt : ROI.getTypes())
			{
			if (rt.canPlace()&&!rt.isCompound())
				{
				JToggleButton miNewROIthis;

				// toolButtons.addAll(Arrays.asList(bEllipseROI,bFreehandROI,bLineROI,bPointROI,bPolygonROI,bRectROI,bSelectROI));

				if (rt.getIcon()==null)
					miNewROIthis = new JToggleButton(rt.name());
				else
					miNewROIthis = new JImageToggleButton(rt.getIcon(), rt.name());
				miNewROIthis.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
							{
							if (((JToggleButton) e.getSource()).isSelected())
								{
								ImageRendererROI renderer = getRendererClass(ImageRendererROI.class);
								setTool(new GeneralToolDragCreateROI(PlateOverviewWindow.this, rt
										.makeInstance(), renderer));
								}
							}
					});

				// TODO would be best if it was sorted
				toolButtons.add(miNewROIthis);
				// BasicWindow.addMenuItemSorted(miNew, miNewROIthis);
				}
			}

		// /////////////////////

		drawArea.setToolButtons(toolButtons.toArray(new JToggleButton[0]));

		for (Viewer2DRendererExtension e : Viewer2DWindow.imageWindowRendererExtensions)
			e.newImageWindow(this);

		cameraCombo = new JComboBox<EvDevicePath>(new Vector<EvDevicePath>(EvHardware
				.getDeviceMap(HWCamera.class).keySet()));

		bSnap
				.setToolTipText("Manually take a picture and update. Does not save image.");
		// tHistoView.setToolTipText("Show histogram controls");
		tAutoRange.setToolTipText("Automatically adjust visible range");
		bSetFullRange.setToolTipText("Set visible range of all of camera range");

		// bCameraToROI.addActionListener(this);
		bSnap.addActionListener(this);
		// tHistoView.addActionListener(this);
		tAutoRange.addActionListener(this);
		bSetFullRange.addActionListener(this);
		histoView.addActionListener(this);
		bAutoFocus.addActionListener(this);
		bGoToROI.addActionListener(this);
		bResetView.addActionListener(this);
		bCreatePos.addActionListener(this);

		// pHisto.setBorder(BorderFactory.createTitledBorder("Range adjustment"));
		pHisto.add(EvSwingUtil.layoutCompactVertical(tAutoRange, bSetFullRange),
				BorderLayout.WEST);
		pHisto.add(histoView, BorderLayout.CENTER);

		List<JComponent> blistleft = new LinkedList<JComponent>();
		blistleft.addAll(toolButtons);
		// blistleft.add(bCameraToROI);
		blistleft.add(bGoToROI);
		blistleft.add(bAutoFocus);
		blistleft.add(bCreatePos);
		JComponent pLeft = EvSwingUtil.layoutACB(
				EvSwingUtil.layoutEvenVertical(blistleft.toArray(new JComponent[0])
				/*
				 * bSelectROI, bEllipseROI, bFreehandROI, bLineROI, bPointROI,
				 * bPolygonROI, bRectROI, bCameraToROI, bGoToROI, bAutoFocus
				 */
				), null, null);

		JPanel pCenter = new JPanel(new BorderLayout());
		pCenter.add(EvSwingUtil.layoutCompactHorizontal(cameraCombo, bResetView,
				bSnap, tUpdateView), BorderLayout.SOUTH);
		pCenter.add(drawArea, BorderLayout.CENTER);

		sidepanel = new EvHidableSidePaneBelow(pCenter, pHisto, true);
		sidepanel.addActionListener(this);

		setLayout(new BorderLayout());
		add(sidepanel, BorderLayout.CENTER);
		add(pLeft, BorderLayout.WEST);

		for (JToggleButton b : toolButtons)
			b.addActionListener(this);

		// Window overall things
		setTitleEvWindow("Plate overview");
		packEvWindow();
		setBoundsEvWindow(new Rectangle(800, 600));
		setVisibleEvWindow(true);

		RecordingResource.posListListeners.addWeakListener(this);
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
		if (e.getSource()==bSnap)
			snapCamera();
		else if (e.getSource()==bResetView)
			{
			resetView();
			}
		else if (e.getSource()==tAutoRange)
			{
			if (lastCameraImage!=null)
				histoView.calcAutoRange(lastCameraImage);
			histoView.repaint();
			drawArea.repaint();
			}
		else if (e.getSource()==bSetFullRange)
			{
			histoView.lower = 0;
			histoView.upper = (int) Math.pow(2, getNumCameraBits())-1;
			drawArea.repaint();
			histoView.repaint();
			}
		else if (e.getSource()==bAutoFocus)
			autofocusAction();
		else if (e.getSource()==bGoToROI)
			moveStageFocusROI();
		// else if(e.getSource()==bCameraToROI){
		// showErrorDialog("Not implemented yet");
		// }

		else if (e.getSource()==bCreatePos)
			{

			HWStage xStage = null;
			int xAxisNum = 0;
			HWStage yStage = null;
			int yAxisNum = 0;
			for (HWStage stage : EvHardware.getDeviceMapCast(HWStage.class).values())
				{
				int aname = stage.getNumAxis();
				for (int i = 0; i<aname; i++)
					if (stage.getAxisName()[i].equals("X"))
						{
						xStage = stage;
						xAxisNum = i;
						}
					else if (stage.getAxisName()[i].equals("Y"))
						{
						yStage = stage;
						yAxisNum = i;
						}
				}

			Set<ROI> rois = new HashSet<ROI>(ROI.getSelected());

			if (rois.size()!=1)
				showErrorDialog("Select 1 ROI first");
			else if (xStage==null||yStage==null)
				showErrorDialog("Couldn't find one or several stages");
			else
				{
				ROI roi = rois.iterator().next();
				double xUpper = roi.getPlacementHandle1().getX();
				double yUpper = roi.getPlacementHandle1().getY();
				double xLower = roi.getPlacementHandle2().getX();
				double yLower = roi.getPlacementHandle2().getY();
				HWCamera cam = getCurrentCamera();
				double noOfImagesX = Math.ceil(Math.abs(xUpper-xLower)/cam.getCamWidth());
				double noOfImagesY = Math.ceil(Math.abs(yUpper-yLower)/cam.getCamHeight());

				for (int i = 0; i<(int) noOfImagesY; i++)
					{
					for (int j = 0; j<(int) noOfImagesX; j++)
						{
						StoredStagePositionAxis[] posInfo = new StoredStagePositionAxis[2];

						posInfo[0] = new StoredStagePositionAxis(xStage, xAxisNum,
								(xUpper+cam.getCamWidth()*j));
						posInfo[1] = new StoredStagePositionAxis(yStage, yAxisNum,
								(yUpper+cam.getCamHeight()*i));

						String newName = RecordingResource.getUnusedPosName();

						StoredStagePosition newPos = new StoredStagePosition(posInfo, newName);
						RecordingResource.posList.add(newPos);
						}
					}
				RecordingResource.posListUpdated();

				}

			}
		else if (e.getSource()==histoView)
			{
			drawArea.repaint();
			}
		else if (e.getSource()==sidepanel)
			{
			Rectangle bounds = getBoundsEvWindow();
			int dh = pHisto.getBounds().height;
			if (!sidepanel.isPanelVisible())
				dh = -dh;
			setBoundsEvWindow(new Rectangle(bounds.x, bounds.y,
					(int) (bounds.getWidth()), (int) (bounds.getHeight()+dh)));
			}
		else
			for (JToggleButton b : toolButtons)
				if (e.getSource()==b)
					{

					// Make sure all other tool buttons are unselected
					for (JToggleButton bb : toolButtons)
						{
						if (bb!=b)
							{
							// bb.removeActionListener(this);
							bb.setSelected(false);
							// bb.addActionListener(this);
							}
						}

					}

		}

	private HWCamera getCurrentCamera()
		{
		EvDevicePath camname = (EvDevicePath) cameraCombo.getSelectedItem();
		if (camname!=null)
			return (HWCamera) EvHardware.getDevice(camname);
		else
			return null;
		}

	private EvDevicePath getCurrentCameraPath()
		{
		return (EvDevicePath) cameraCombo.getSelectedItem();
		}

	/**
	 * Take one picture from the camera
	 */
	private void snapCamera()
		{
		HWCamera cam = getCurrentCamera();

		if (cam!=null)
			{

			if (drawArea.overviewImage==null)
				{
				drawArea.overviewImage = new EvPixels(EvPixelsType.INT,
						(int) cam.getCamWidth(), (int) cam.getCamHeight());
				}

			CameraImage cim = cam.snap();

			lastCameraImage = cim.getPixels();
			EvPixels cameraImage = lastCameraImage[0];
			EvPixels overviewImage = drawArea.overviewImage;

			Vector2d cameraImgPos = new Vector2d(0, 0);
			Vector2d overviewImgPos = new Vector2d(0, 0);

			ResolutionManager.Resolution res = new ResolutionManager.Resolution(
					getCameraResolution().x, getCameraResolution().y);

			if (-getStageX()/res.x>overviewImageOffset.x)
				{// when the cameraImage position is farther left then the current
					// offset
				overviewImgPos.x = -getStageX()/res.x-overviewImageOffset.x;// offset to
																																		// make room
																																		// for new
																																		// image
				overviewImageOffset.x = -getStageX()/res.x;// offset is updated
				overviewImgWidth = overviewImage.getWidth()+(int) overviewImgPos.x;// extend
																																						// the
																																						// overviewImage

				}
			else
				{
				if (overviewImage.getWidth()>=getStageX()/res.x+cameraImage.getWidth()
						+overviewImageOffset.x)
					{// when the cameraImage position fits inside the overviewImage
					overviewImgWidth = (int) (overviewImage.getWidth());// overviewImage
																															// width is
																															// unchanged
					}
				else
					{// when the cameraImage position is farther right then the
						// overviewImage
					overviewImgWidth = (int) (getStageX()/res.x+cameraImage.getWidth()+overviewImageOffset.x);// extend
																																																		// the
																																																		// overviewImage
					}
				}
			// same as before but for the Y-axis
			if (-getStageY()/res.y>overviewImageOffset.y)
				{
				overviewImgPos.y = -getStageY()/res.y-overviewImageOffset.y;
				overviewImageOffset.y = -getStageY()/res.y;
				overviewImgHeight = overviewImage.getHeight()+(int) overviewImgPos.y;
				}
			else
				{
				if (overviewImage.getHeight()>=getStageY()/res.y
						+cameraImage.getHeight()+overviewImageOffset.y)
					{
					overviewImgHeight = (int) (overviewImage.getHeight());
					}
				else
					{
					overviewImgHeight = (int) (getStageY()/res.y+cameraImage.getHeight()+overviewImageOffset.y);
					}
				}

			cameraImgPos = new Vector2d(overviewImageOffset.x+getStageX()/res.x,
					overviewImageOffset.y+getStageY()/res.y);//

			// add the new image with the old
			EvPixels newOverviewImage = new EvPixels(EvPixelsType.INT,
					overviewImgWidth, overviewImgHeight);

			int[] newOverviewArray = newOverviewImage.convertToInt(true)
					.getArrayInt();
			int[] overviewArray = overviewImage.convertToInt(true).getArrayInt();
			int[] cameraArray = cameraImage.convertToInt(true).getArrayInt();

			for (int y = 0; y<overviewImage.getHeight(); y++)
				{
				for (int x = 0; x<overviewImage.getWidth(); x++)
					{
					newOverviewArray[(y+(int) overviewImgPos.y)
							*newOverviewImage.getWidth()+(x+(int) overviewImgPos.x)] = overviewArray[y
							*overviewImage.getWidth()+x];
					}
				}
			for (int y = 0; y<cameraImage.getHeight(); y++)
				{
				for (int x = 0; x<cameraImage.getWidth(); x++)
					{
					newOverviewArray[(y+(int) cameraImgPos.y)*newOverviewImage.getWidth()
							+(x+(int) cameraImgPos.x)] = cameraArray[y*cameraImage.getWidth()
							+x];
					}
				}

			drawArea.overviewImage = newOverviewImage;
			}
		drawArea.repaint();
		}

	public void resetView()
		{
		lastCameraImage = null;
		overviewImageOffset = new Vector2d(0, 0);
		overviewImgWidth = 0;
		overviewImgHeight = 0;
		drawArea.resetCameraPos();

		HWCamera cam = getCurrentCamera();
		if (cam!=null)
			{
			drawArea.overviewImage = new EvPixels(EvPixelsType.INT,
					(int) cam.getCamWidth(), (int) cam.getCamHeight());
			}
		repaint();
		}

	public void dataChangedEvent()
		{
		}

	public void windowEventUserLoadedFile(EvData data)
		{
		}

	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}

	public void windowFreeResources()
		{
		resetView();
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
		return EvDecimal.ZERO; // Unclear what is the best. 3D rois?
		}

	@SuppressWarnings("unchecked")
	public <E> E getRendererClass(Class<E> cl)
		{
		for (Viewer2DRenderer r : drawArea.imageWindowRenderers)
			if (cl.isInstance(r))
				return (E) r;
		throw new RuntimeException("No such renderer exists - "+cl);
		}

	public EvContainer getRootObject()
		{
		return RecordingResource.getData();
		}

	public double getRotation()
		{
		// Never any rotation
		return 0;
		}

	/**
	 * [um/px]
	 */
	public ResolutionManager.Resolution getCameraResolution()
		{
		return ResolutionManager
				.getCurrentResolutionNotNull(getCurrentCameraPath());
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
		return new Vector2d((v.x*getCameraResolution().x-drawArea.getCameraPos().x)
				/drawArea.getScale()-overviewImageOffset.x, (v.y
				*getCameraResolution().y-drawArea.getCameraPos().y)
				/drawArea.getScale()-overviewImageOffset.y);
		}

	public Vector2d transformPointW2S(Vector2d v)
		{
		return new Vector2d((v.x+overviewImageOffset.x+drawArea.getCameraPos().x),
				(v.y+overviewImageOffset.y+drawArea.getCameraPos().y));
		}

	public double w2sz(double z)
		{
		return z; // TODO
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
		Set<ROI> rois = new HashSet<ROI>(ROI.getSelected());

		if (rois.size()!=1)
			showErrorDialog("Select 1 ROI first");
		else
			{
			ROI roi = rois.iterator().next();

			double x = roi.getPlacementHandle1().getX();
			double y = roi.getPlacementHandle2().getY();
			// Best would be to be able to get a bounding box

			Map<String, Double> pos = new HashMap<String, Double>();
			pos.put("x", x);
			pos.put("y", y);
			RecordingResource.setStagePos(pos);

			// TODO move to center. must take into account camera etc in that case

			// TODO
			// Probably useful in a wider context - put in resource
			}

		}

	

	public void positionsUpdated()
		{
		repaint();

		}

	@Override
	public String windowHelpTopic()
		{
		return "The plate overview window";
		}
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()
		{
		}

	static
		{
		EvBasicWindow.addBasicWindowExtension(new EvBasicWindowExtension()
			{
			public void newBasicWindow(EvBasicWindow w)
				{
				w.addHook(this.getClass(), new Hook());
				}

			class Hook implements EvBasicWindowHook, ActionListener
				{
				public void createMenus(EvBasicWindow w)
					{
					JMenuItem mi = new JMenuItem("Plate overview", new ImageIcon(getClass()
							.getResource("fugueBinocular.png")));
					mi.addActionListener(this);
					EvBasicWindow.addMenuItemSorted(
							w.getCreateMenuWindowCategory("Recording"), mi);
					}

				public void actionPerformed(ActionEvent e)
					{
					new PlateOverviewWindow();
					}

				public void buildMenu(EvBasicWindow w)
					{
					}
				}
			});

		}

	}

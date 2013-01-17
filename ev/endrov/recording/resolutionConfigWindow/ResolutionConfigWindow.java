package endrov.recording.resolutionConfigWindow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;


import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.BasicWindowExtension;
import endrov.basicWindow.BasicWindowHook;
import endrov.data.EvData;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardwareConfigGroup.State;
import endrov.imageset.EvPixels;
import endrov.recording.CameraImage;
import endrov.recording.RecordingResource;
import endrov.recording.ResolutionManager;
import endrov.recording.ResolutionManager.ResolutionState;
import endrov.recording.device.HWCamera;
import endrov.recording.widgets.RecWidgetComboDevice;
import endrov.recording.widgets.RecWidgetSelectProperties;
import endrov.util.EvSwingUtil;

/**
 * Configuring resolutions
 * 
 * @author Johan Henriksson
 * @author Kim Nordlöf, Erik Vernersson
 */
public class ResolutionConfigWindow extends BasicWindow implements
		ActionListener
	{
	private static final long serialVersionUID = 1L;

	private EvPixels[] lastCameraImage = null;

	private RecWidgetSelectProperties wProperties = new RecWidgetSelectProperties();
	private JList listCalibrations = new JList(new DefaultListModel());

	private JButton bDetect = new JButton("Detect");
	private JButton bEnter = new JButton("Enter manually");
	private JButton bDelete = new JButton("Delete");

	private RecWidgetComboDevice cCaptureDevice = new RecWidgetComboDevice()
		{
			private static final long serialVersionUID = 1L;

			protected boolean includeDevice(EvDevicePath path, EvDevice device)
				{
				return device instanceof HWCamera;
				}
		};

	private static class ListItem
		{
		public String name;
		public HWCamera cam;

		public ListItem(String name, HWCamera cam)
			{
			this.name = name;
			this.cam = cam;
			}

		public String toString()
			{
			return name;
			}
		}

	/**
	 * Create window
	 */
	public ResolutionConfigWindow()
		{
		setLayout(new BorderLayout());

		JScrollPane scrollList = new JScrollPane(listCalibrations,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add(EvSwingUtil.withLabel("Capture device: ", cCaptureDevice),
				BorderLayout.NORTH);
		add(EvSwingUtil.layoutEvenVertical(wProperties, scrollList),
				BorderLayout.CENTER);
		add(EvSwingUtil.layoutEvenHorizontal(bDetect, bEnter, bDelete),
				BorderLayout.SOUTH);

		generateList();

		bDetect.addActionListener(this);
		bEnter.addActionListener(this);
		bDelete.addActionListener(this);

		// Window overall things
		setTitleEvWindow("Configure resolution");
		packEvWindow();
		setVisibleEvWindow(true);
		}

	private void generateList()
		{
		DefaultListModel model = (DefaultListModel) listCalibrations.getModel();
		model.removeAllElements();
		for (HWCamera cam : ResolutionManager.resolutions.keySet())
			for (String name : ResolutionManager.resolutions.get(cam).keySet())
				model.addElement(new ListItem(name, cam));
		}

	/**
	 * Handle button presses
	 */
	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource()==bDelete)
			{
			ListItem item = (ListItem) listCalibrations.getSelectedValue();
			if (item!=null)
				{
				ResolutionManager.resolutions.get(item.cam).remove(item.name);
				generateList();
				}
			}

		
		EvDevicePath campath = (EvDevicePath) cCaptureDevice.getSelectedDevice();
		if (campath!=null)
			{
			if (e.getSource()==bEnter)
				{
				enterResolution(campath);
				}
			else if (e.getSource()==bDetect)
				{
				detectResolution(campath);
				}

			
			}

		}

	/**
	 * Enter the current resolution manually
	 */
	private void enterResolution(EvDevicePath campath)
		{
		
		
		try
			{
			// Get resolution
			String sResX = JOptionPane.showInputDialog("Resolution X [um/px]?");
			if (sResX==null)
				return;
			double resX = Double.parseDouble(sResX);
			String sResY = JOptionPane.showInputDialog("Resolution Y [um/px]?");
			if (sResY==null)
				return;
			double resY = Double.parseDouble(sResY);

			String name = ResolutionManager.getUnusedResName(campath);
			name = JOptionPane.showInputDialog("Name of resolution?", name);
			if (name==null)
				return;

			// Create the resolution state
			ResolutionState rstate = new ResolutionState();
			rstate.cameraRes = new ResolutionManager.Resolution(resX, resY);
			rstate.state = State.recordCurrent(wProperties.getSelectedProperties());
			ResolutionManager.getCreateResolutionStatesMap(campath).put(name,
					rstate);

			generateList();
			}
		catch (NumberFormatException e1)
			{
			BasicWindow.showErrorDialog("Invalid number");
			return;
			}

		}
	
	
	/**
	 * Auto-detect the resolution by moving the stage and measuring the offset in the camera
	 */
	private void detectResolution(EvDevicePath campath)
		{
		HWCamera cam=(HWCamera)campath.getDevice();
		
		CameraImage cim = cam.snap();
		lastCameraImage = cim.getPixels();
		EvPixels imageA = lastCameraImage[0];

		int cameraDisplacment = 50;

		double newX = RecordingResource.getCurrentStageX()-cameraDisplacment;
		double newY = RecordingResource.getCurrentStageY()-cameraDisplacment;

		Map<String, Double> pos = new HashMap<String, Double>();
		pos.put("X", newX);
		pos.put("Y", newY);
		RecordingResource.setStagePos(pos);

		cim = cam.snap();
		lastCameraImage = cim.getPixels();
		EvPixels imageB = lastCameraImage[0];

		double[] corrV = ImageDisplacementCorrelation.displacement(imageA, imageB);

		// [um/px]
		double resX, resY;
		resX = cameraDisplacment/corrV[0];
		resY = cameraDisplacment/corrV[1];

		String name = "Detected Resolution";
		
		// Create the resolution state
		ResolutionState rstate = new ResolutionState();
		rstate.cameraRes = new ResolutionManager.Resolution(resX, resY);
		rstate.state = State.recordCurrent(wProperties.getSelectedProperties());
		ResolutionManager.getCreateResolutionStatesMap(campath).put(name,	rstate);
		generateList();
		
		showInformativeDialog("Resolution detected: "+resX+" "+resY);
		}


	@Override
	public void dataChangedEvent()
		{
		// cCaptureDevice.updateOptions();
		}

	@Override
	public void windowSavePersonalSettings(Element root)
		{
		}

	@Override
	public void loadedFile(EvData data)
		{
		}

	@Override
	public void freeResources()
		{
		}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()
		{
		}

	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
				public void newBasicWindow(BasicWindow w)
					{
					w.basicWindowExtensionHook.put(this.getClass(), new Hook());
					}

				class Hook implements BasicWindowHook, ActionListener
					{
					public void createMenus(BasicWindow w)
						{
						JMenuItem mi = new JMenuItem("Configure resolution", new ImageIcon(
								getClass().getResource("jhResolutionConfigWindow.png")));
						mi.addActionListener(this);
						BasicWindow.addMenuItemSorted(
								w.getCreateMenuWindowCategory("Recording"), mi);
						}

					public void actionPerformed(ActionEvent e)
						{
						new ResolutionConfigWindow();
						}

					public void buildMenu(BasicWindow w)
						{
						}
					}
			});

		}

	}

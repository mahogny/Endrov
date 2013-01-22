package endrov.recording.resolutionConfigWindow;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


import org.jdom.Element;

import endrov.data.EvData;
import endrov.gui.EvSwingUtil;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardwareConfigGroup.State;
import endrov.recording.CameraImage;
import endrov.recording.RecordingResource;
import endrov.recording.ResolutionManager;
import endrov.recording.ResolutionManager.ResolutionState;
import endrov.recording.device.HWCamera;
import endrov.recording.widgets.RecWidgetComboDevice;
import endrov.recording.widgets.RecWidgetSelectProperties;
import endrov.typeImageset.EvPixels;

/**
 * Configuring resolutions
 * 
 * @author Johan Henriksson
 * @author Kim Nordlöf, Erik Vernersson
 */
public class ResolutionConfigWindow extends EvBasicWindow implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	private EvPixels[] lastCameraImage = null;

	private RecWidgetSelectProperties wProperties = new RecWidgetSelectProperties();

	private JTable tableCalibrations = new JTable();
	
	
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

	/**
	 * Create window
	 */
	public ResolutionConfigWindow()
		{
		setLayout(new BorderLayout());

		JScrollPane scrollList = new JScrollPane(tableCalibrations, //listCalibrations,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add(EvSwingUtil.withLabel("Capture device: ", cCaptureDevice),
				BorderLayout.NORTH);
		add(EvSwingUtil.layoutEvenVertical(wProperties, scrollList),
				BorderLayout.CENTER);
		add(EvSwingUtil.layoutEvenHorizontal(bDetect, bEnter, bDelete),
				BorderLayout.SOUTH);

		updateListOfResolutions();

		bDetect.addActionListener(this);
		bEnter.addActionListener(this);
		bDelete.addActionListener(this);

		
		// Window overall things
		setTitleEvWindow("Configure resolution");
		packEvWindow();
		setBoundsEvWindow(new Rectangle(500, 400));
		setVisibleEvWindow(true);
		}

	
	
	private void updateListOfResolutions()
		{
		DefaultTableModel model=new DefaultTableModel(new String[][]{}, new String[]{"Name","ResX","ResY","Camera","State"})
			{
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column)
				{
				return false;
				}
			};
		for (EvDevicePath campath : ResolutionManager.resolutions.keySet())
			{
			for (String name : ResolutionManager.resolutions.get(campath).keySet())
				{
				ResolutionManager.ResolutionState state=ResolutionManager.resolutions.get(campath).get(name);
				String[] row=new String[]{
						name,
						""+state.cameraRes.x,
						""+state.cameraRes.y,
						campath.toString(),
						state.state.toString()
				};
				model.addRow(row);
				}
			}
		
		tableCalibrations.setModel(model);
		}

	/**
	 * Handle button presses
	 */
	public void actionPerformed(ActionEvent e)
		{
		
		if (e.getSource()==bDelete)
			{
			int row=tableCalibrations.getSelectedRow();
			if(row!=-1)
				{
				DefaultTableModel model=(DefaultTableModel)tableCalibrations.getModel();
				String name=(String)model.getValueAt(row, 0);
				String campath=(String)model.getValueAt(row, 3);
				ResolutionManager.resolutions.get(new EvDevicePath(campath)).remove(name);
				updateListOfResolutions();
				}
			}

		
		EvDevicePath campath = (EvDevicePath) cCaptureDevice.getSelectedDevice();
		if (campath==null)
			showErrorDialog("No camera selected");
		else
			{
			if (e.getSource()==bEnter)
				enterResolution(campath);
			else if (e.getSource()==bDetect)
				detectResolution(campath);
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
			ResolutionManager.getCreateResolutionStatesMap(campath).put(name,	rstate);

			updateListOfResolutions();
			}
		catch (NumberFormatException e1)
			{
			EvBasicWindow.showErrorDialog("Invalid number");
			return;
			}

		}
	
	
	/**
	 * Auto-detect the resolution by moving the stage and measuring the offset in the camera
	 */
	private void detectResolution(EvDevicePath campath)
		{
		try
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

			String name = ResolutionManager.getUnusedResName(campath);
			
			// Create the resolution state
			ResolutionState rstate = new ResolutionState();
			rstate.cameraRes = new ResolutionManager.Resolution(resX, resY);
			rstate.state = State.recordCurrent(wProperties.getSelectedProperties());
			ResolutionManager.getCreateResolutionStatesMap(campath).put(name,	rstate);
			updateListOfResolutions();
			}
		catch (Exception e)
			{
			showErrorDialog("Error: "+e.getMessage());
			e.printStackTrace();
			}
		
		
		}


	@Override
	public void dataChangedEvent()
		{
		// cCaptureDevice.updateOptions();
		}

	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}
	public void windowEventUserLoadedFile(EvData data){}

	@Override
	public void windowFreeResources()
		{
		}


	@Override
	public String windowHelpTopic()
		{
		return "Configurating microscope resolutions";
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
					w.basicWindowExtensionHook.put(this.getClass(), new Hook());
					}

				class Hook implements EvBasicWindowHook, ActionListener
					{
					public void createMenus(EvBasicWindow w)
						{
						JMenuItem mi = new JMenuItem("Configure resolution", new ImageIcon(
								getClass().getResource("jhResolutionConfigWindow.png")));
						mi.addActionListener(this);
						EvBasicWindow.addMenuItemSorted(
								w.getCreateMenuWindowCategory("Recording"), mi);
						}

					public void actionPerformed(ActionEvent e)
						{
						new ResolutionConfigWindow();
						}

					public void buildMenu(EvBasicWindow w)
						{
						}
					}
			});

		}

	}

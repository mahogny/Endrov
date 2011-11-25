package endrov.recording.resolutionConfigWindow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import endrov.basicWindow.BasicWindow;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardwareConfigGroup;
import endrov.recording.device.HWCamera;
import endrov.recording.widgets.RecWidgetComboDevice;
import endrov.recording.widgets.RecWidgetSelectProperties;
import endrov.util.EvSwingUtil;

/**
 * Configuring resolutions
 * 
 * @author Johan Henriksson
 *
 */
public class ResolutionConfigWindow extends JFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	
	RecWidgetSelectProperties wProperties=new RecWidgetSelectProperties();

	private JButton bOk=new JButton("OK");
	private JButton bCancel=new JButton("Cancel");

	private JTextField tName=new JTextField("");

	
	private RecWidgetComboDevice cCaptureDevice=new RecWidgetComboDevice()
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
		
		bOk.addActionListener(this);
		bCancel.addActionListener(this);
		
		setLayout(new BorderLayout());
		add(EvSwingUtil.withLabel("Name: ", tName), BorderLayout.NORTH);
		add(wProperties, BorderLayout.CENTER);
		add(EvSwingUtil.layoutEvenHorizontal(bOk, bCancel),
				BorderLayout.SOUTH);
		pack();
		setVisible(true);
		}


	/**
	 * Handle button presses
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bOk)
			{
			String name=tName.getText();
			if(!name.equals(""))
				{
				EvHardwareConfigGroup group=new EvHardwareConfigGroup();
				group.propsToInclude.addAll(wProperties.getSelectedProperties());
				
				EvHardwareConfigGroup.putConfigGroup(name, group);
				
				dispose();
				}
			else
				BasicWindow.showErrorDialog("No name specified for the group");			
			
			}
		else if(e.getSource()==bCancel)
			{
			dispose();
			}
		
		}
	

	}

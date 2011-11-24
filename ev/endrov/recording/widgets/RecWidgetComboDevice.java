package endrov.recording.widgets;

import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import endrov.hardware.EvDevice;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardware;

public abstract class RecWidgetComboDevice extends JComboBox
	{
	private static final long serialVersionUID = 1L;

	public RecWidgetComboDevice()
		{
		updateOptions();
		}
	
	
	private void updateOptions()
		{
		DefaultComboBoxModel model=(DefaultComboBoxModel)getModel();
		model.removeAllElements();
		for(Map.Entry<EvDevicePath, EvDevice> e:EvHardware.getDeviceMap().entrySet())
			{
			if(includeDevice(e.getKey(), e.getValue()))
				model.addElement(e.getKey());
			}
		}
	
	public EvDevicePath getSelectedDevice()
		{
		return (EvDevicePath)getSelectedItem();
		}
	
	protected abstract boolean includeDevice(EvDevicePath path, EvDevice device);
	}

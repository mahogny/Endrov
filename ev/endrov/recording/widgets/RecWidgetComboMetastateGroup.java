package endrov.recording.widgets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import endrov.hardware.EvHardwareConfigGroup;

/**
 * Combobox for choosing groups of metastates
 * 
 * @author Johan Henriksson
 *
 */
public class RecWidgetComboMetastateGroup extends JComboBox
	{
	private static final long serialVersionUID = 1L;

	public RecWidgetComboMetastateGroup()
		{
		makeLayout();
		}

	public void makeLayout()
		{
		DefaultComboBoxModel modelState=(DefaultComboBoxModel)getModel();
		modelState.removeAllElements();
		for(String groupName:EvHardwareConfigGroup.groups.keySet())
			modelState.addElement(groupName);
		}
	
	public EvHardwareConfigGroup getConfigGroup()
		{
		String name=(String)getSelectedItem();
		return EvHardwareConfigGroup.groups.get(name);
		}
	
	}

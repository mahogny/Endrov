package endrov.recording.widgets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import endrov.hardware.EvHardwareConfigGroup;

/**
 * Combobox for choosing metastate 
 * 
 * @author Johan Henriksson
 *
 */
public class RecWidgetComboMetastate extends JComboBox
	{
	private static final long serialVersionUID = 1L;

	public String currentMetastate=null;
	
	public RecWidgetComboMetastate()
		{
		makeLayout();
		}

	public void makeLayout()
		{
		DefaultComboBoxModel modelState=(DefaultComboBoxModel)getModel();
		modelState.removeAllElements();
		
		EvHardwareConfigGroup g=EvHardwareConfigGroup.groups.get(currentMetastate);
		if(g!=null)
			{
			for(String s:g.states.keySet())
				modelState.addElement(s);
			}
		
		}
	
	public EvHardwareConfigGroup getConfigGroup()
		{
		String name=(String)getSelectedItem();
		return EvHardwareConfigGroup.groups.get(name);
		}
	
	}

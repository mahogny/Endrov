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
public class RecWidgetComboConfigGroupStates extends JComboBox<String>
	{
	private static final long serialVersionUID = 1L;

	public String currentMetastate=null;
	
	public RecWidgetComboConfigGroupStates()
		{
		//makeLayout();
		}

	public void setMetastateGroup(String s)
		{
		currentMetastate=s;
		makeLayout();
		}
	
	
	
	public void makeLayout()
		{
		DefaultComboBoxModel<String> modelState=(DefaultComboBoxModel<String>)getModel();
		modelState.removeAllElements();
		
		if(currentMetastate!=null)
			{
			EvHardwareConfigGroup g=EvHardwareConfigGroup.getConfigGroup(currentMetastate);
			if(g!=null)
				for(String s:g.getStateNames())
					modelState.addElement(s);
			repaint();
			}
		}
	
	public EvHardwareConfigGroup getConfigGroup()
		{
		String name=getConfigGroupName();
		return EvHardwareConfigGroup.getConfigGroup(name);
		}
	
	public String getConfigGroupName()
		{
		return (String)getSelectedItem();
		}
	
	}

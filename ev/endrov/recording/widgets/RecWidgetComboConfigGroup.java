package endrov.recording.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.WeakHashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import endrov.hardware.EvHardwareConfigGroup;

/**
 * Combobox for choosing groups of metastates
 * 
 * @author Johan Henriksson
 *
 */
public class RecWidgetComboConfigGroup extends JComboBox//<String>
	{
	private static final long serialVersionUID = 1L;

	private WeakHashMap<RecWidgetComboConfigGroupStates, Object> listeners=new WeakHashMap<RecWidgetComboConfigGroupStates, Object>();

	public RecWidgetComboConfigGroup()
		{
		makeLayout();
		
		addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				String s=(String)getSelectedItem();
				for(RecWidgetComboConfigGroupStates l:listeners.keySet())
					l.setMetastateGroup(s);
				}
			});
		
		}

	
	
	public void makeLayout()
		{
		DefaultComboBoxModel modelState=(DefaultComboBoxModel/*<String>*/)getModel();
		modelState.removeAllElements();
		for(String groupName:EvHardwareConfigGroup.getConfigGroups().keySet())
			modelState.addElement(groupName);
		repaint();
		}
	
	public EvHardwareConfigGroup getConfigGroup()
		{
		return EvHardwareConfigGroup.getConfigGroup(getConfigGroupName());
		}
	
	public String getConfigGroupName()
		{
		return (String)getSelectedItem();
		}

	
	
	public void registerWeakMetastateGroup(RecWidgetComboConfigGroupStates e)
		{
		listeners.put(e,null);
		e.setMetastateGroup((String)getSelectedItem());
		}
	
	}

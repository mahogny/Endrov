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
public class RecWidgetComboMetastateGroup extends JComboBox
	{
	private static final long serialVersionUID = 1L;

	private WeakHashMap<RecWidgetComboMetastate, Object> listeners=new WeakHashMap<RecWidgetComboMetastate, Object>();

	public RecWidgetComboMetastateGroup()
		{
		makeLayout();
		
		addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				String s=(String)getSelectedItem();
				for(RecWidgetComboMetastate l:listeners.keySet())
					l.setMetastateGroup(s);
				}
			});
		
		}

	
	
	public void makeLayout()
		{
		DefaultComboBoxModel modelState=(DefaultComboBoxModel)getModel();
		modelState.removeAllElements();
		for(String groupName:EvHardwareConfigGroup.groups.keySet())
			modelState.addElement(groupName);
		repaint();
		}
	
	public EvHardwareConfigGroup getConfigGroup()
		{
		String name=getConfigGroupName();
		return EvHardwareConfigGroup.groups.get(name);
		}
	
	public String getConfigGroupName()
		{
		return (String)getSelectedItem();
		}

	
	
	public void registerWeakMetastateGroup(RecWidgetComboMetastate e)
		{
		listeners.put(e,null);
		e.setMetastateGroup((String)getSelectedItem());
		}
	
	}
